package conversions.creators;

import groovy.xml.MarkupBuilder

import java.io.*

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.openedit.Data
import org.openedit.data.Searcher
import org.openedit.entermedia.Asset
import org.openedit.entermedia.MediaArchive
import org.openedit.entermedia.creator.BaseCreator
import org.openedit.entermedia.creator.ConvertInstructions
import org.openedit.entermedia.creator.ConvertResult

import com.openedit.page.Page
import com.openedit.util.FileUtils


class rhozetCreator extends BaseCreator {

	private static final Log log = LogFactory.getLog(rhozetCreator.class);


	public boolean canReadIn(MediaArchive inArchive, String inInput) {
		return true;//"flv".equals(inOutput) || mpeg; //This has a bunch of types
	}


	public ConvertResult convert(MediaArchive inArchive, Asset inAsset, Page converted, ConvertInstructions inStructions) {
		ConvertResult result = new ConvertResult();
		try
		{
			Page inputpage = inArchive.findOriginalMediaByType("video",inAsset);
	
			if( inputpage == null || !inputpage.exists()) {
				//no such original
				result.setError("Original does not exist: " + inAsset.getSourcePath());
				result.setOk(false);
				return result;
			}
			String abspath = inputpage.getContentItem().getAbsolutePath();
			String inputfilename = inputpage.getName();
			if (!inStructions.isForce() && converted.exists() && converted.getContentItem().getLength() != 0) 
			{
					result.setComplete(true);
					result.setOk(true);
					log.info("Generated file already existed: ${converted}");
					return result;
			}
	
			String folder = getPageManager().getPage(converted.getParentPath()).getContentItem().getAbsolutePath();
	
			String filename = converted.getName();
			String mediaroot = "v:\\Media\\originals\\" + inAsset.getSourcePath();
			mediaroot = mediaroot.replace("/","\\");
			File folderfile = new File(folder);
			folderfile.mkdirs();
	
			def writer = new StringWriter();
			def xml = new MarkupBuilder(writer)
			//
			//<cnpsXML CarbonAPIVer="1.2" TaskType="JobQueue">
			//<Sources>
			//<Module_0 Filename="example.avi" />
			//</Sources>
			//<Destinations>
			//<Module_0 PresetGUID="{A7264AEF-FF57-42E0-BBAD-CCF546CD515F}">
			//</Destinations> 	
			//</cnpsXML>
			Page outputpage = getPageManager().getPage(inArchive.getCatalogId() + "/rhozet/" + inAsset.getSourcePath() +"/");
			File outputfolder = new File(outputpage.getContentItem().getAbsolutePath());
			outputfolder.mkdirs();
			xml.cnpsXML(TaskType:'JobQueue', carbonAPIVer:'1.2') {
				Sources {
					Module_0(Filename:"${mediaroot}")
				}
				Destinations() {
					Module_0(PresetGUID:inStructions.getProperty("guid")) {
						PostconversionTasks({
							FileCopy_0(TargetFolder:"v:\\Media\\generated\\" + inAsset.getSourcePath().replace("/", "\\") +"\\", TargetFile:filename)
							//FileCopy_0(TargetFolder:"v:\\Media\\generated\\", TargetFile:filename)
	
						})
					}
				}
			}
	
			log.info("sending" + writer.toString());
	
			//callServer returns an xml node
			def reply = callServer(writer.toString());
	
			if (reply != null)
			{
				String success = reply.@Success;
				String jobGuid = reply.@GUID;
	
				if (success) {
					result.setProperty("externalid", jobGuid);
					result.setOk(true);
					result.setComplete(false);
					log.info( "Job successfully started with guid:${jobGuid}");
				}
				else {
					result.setOk(false);
					result.setError("ERROR: Problem starting job");
				}
			}
			else {
				result.setOk(false);
			}
		} 
		catch( Throwable ex)
		{
			result.setOk(false);
			log.error(ex);
			result.setError(ex.toString());
		}

		return result;
	}
	public ConvertResult updateStatus(MediaArchive inArchive,Data inTask, Asset inAsset,ConvertInstructions inStructions )
	{
		ConvertResult result = new ConvertResult();

		String jobGuid = inTask.get("externalid");
		if( jobGuid == null)
		{
			result.setError("No External Id  GUI set on task");
			return result;
		}
		result.setProperty("externalid", jobGuid);

		def writer = new StringWriter();
		def xml = new MarkupBuilder(writer)
		//
		//<cnpsXML CarbonAPIVer="1.2" TaskType="JobQueue">
		//<Sources>
		//<Module_0 Filename="example.avi" />
		//</Sources>
		//<Destinations>
		//<Module_0 PresetGUID="{A7264AEF-FF57-42E0-BBAD-CCF546CD515F}">
		//</Destinations>
		//</cnpsXML>

		xml.cnpsXML(TaskType:'JobCommand', carbonAPIVer:'1.2')
		{
			JobCommand(Command:'QueryInfo', GUID:jobGuid) {}
		}
		log.info("sending" + writer.toString());

		try
		{
			//callServer returns an xml node
			def reply = callServer(writer.toString());
	
			if (reply != null)
			{
				String success = reply.@Success;
				if (success) 
				{
					def jobInfo = reply.JobInfo;
					String status = jobInfo.@Status;
					status = status.replace('[', '');
					status = status.replace(']', '');
	
					/* Possible status values: Preparing, Queued, Starting
					 * Started, Stopping, Stopped, Pausing, Paused, Resuming,
					 * Completed, Error, and Invalid
					 */
	
					result.setOk(true);
					result.setProperty("status", status);
					log.info "Status:  ${status}";
					if (status.equalsIgnoreCase("Completed")) 
					{
						result.setComplete(true);
	
						Searcher presetsearcher = inArchive.getSearcherManager().getSearcher (inArchive.getCatalogId(), "convertpreset");
						String presetid = inTask.get("presetid");
						Data preset = presetsearcher.searchById(presetid);
						String outputfile = preset.get("outputfile");
	
						Page source= inArchive.getPageManager().getPage("/" + inArchive.getCatalogId() + "/rhozet/" + inTask.getSourcePath() + "/" + outputfile);
						Page destination = inArchive.getPageManager().getPage("/WEB-INF/data/${inArchive.getCatalogId()}/generated/${inTask.getSourcePath()}/${outputfile}");
						log.info "source : ${source.getPath()}";
						log.info "destination : ${destination.getPath()}";
						if(!destination.exists() && source.exists()){
							log.info "copying file back";
							inArchive.getPageManager().movePage(source, destination);
						} else{
							log.info "Source file was not present or destination already existed";
						}
					}
					else 
					{
						result.setComplete(false);
					}
					if(status.equalsIgnoreCase("Error") ||  status.equalsIgnoreCase("Invalid"))
					{
						String text = "Rhozet reported error: " + jobInfo.@Error;
						log.info( text );
						result.setError(text);
						result.setComplete(false);
						result.setOk(false);
					}
	
					log.info "Job status successfully updated";
				}
				else 
				{
					log.info "ERROR: Problem updating status";
				}
			}
			else 
			{
				log.info("invalid or no response from rhozet");
				result.setOk(false);
				result.setComplete(false);
			}
		}
		catch(Throwable ex)
		{
			log.info("no response from rhozet ${ex}");
			result.setOk(false);
			result.setComplete(false);
			result.setError("no response from rhozet ${ex}");
		}
		return result;
	}



	public String populateOutputPath(MediaArchive inArchive, ConvertInstructions inStructions) {
		StringBuffer path = new StringBuffer();
		String prefix = inStructions.getProperty("pathprefix");
		if( prefix != null) {
			path.append(prefix);
		}
		else {
			path.append("/WEB-INF/data");
			path.append(inArchive.getCatalogHome());
			path.append("/generated/");
		}
		path.append(inStructions.getAssetSourcePath());

		path.append("/video." + inStructions.getOutputExtension());
		inStructions.setOutputPath(path.toString());
		return path.toString();
	}

	def callServer(String somexml) 
	{
		byte[] somexmlUTF8 = somexml.getBytes("UTF8");
		
		// Create packet
		StringBuilder buffer = new StringBuilder();
		buffer.append("CarbonAPIXML1");
		buffer.append(" ");
		buffer.append(somexmlUTF8.length);
		buffer.append(" ");
		byte[] bufferUTF8 = buffer.toString().getBytes("UTF8");
		
		Socket s = new Socket("67.224.98.203", 1120);
		s.setSoTimeout(20000);

		OutputStream output = s.getOutputStream();
		output.write(bufferUTF8);
		output.write(somexmlUTF8);
		
		DataInputStream reader = new DataInputStream( new BufferedInputStream( s.getInputStream() ) );
		StringBuffer response = new StringBuffer();
		int spacecounter = 0;
		while( spacecounter < 2)
		{
			char c = reader.read();
			if( c == ' ')
			{
				spacecounter++;
			}
			response.append(c);
		} 	
		String [] token=response.toString().split(" ");
		int dataLength=Integer.parseInt(token[1]);
		int byteCounter=0;
		while( byteCounter < dataLength)
		{
			char c = reader.read();
			byteCounter+=1;
			response.append(c);
		}
		FileUtils.safeClose(reader);
		FileUtils.safeClose(output);
		
		if (response.length() > 0) 
		{
			String xmlResponse = response.substring(response.indexOf('<'));
			log.info("reponse text: ${xmlResponse}");
			try{
				def root = new XmlParser().parseText(xmlResponse);
				return root;
			} catch(Exception e){
				log.error(e);
				return null;
			}
		}
		else {
			log.info "ERROR: No Response";
			return null;
		}
	}
}
