package publishing.publishers;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.openedit.Data
import org.openedit.data.Searcher
import org.openedit.entermedia.Asset
import org.openedit.entermedia.MediaArchive
import org.openedit.entermedia.publishing.*

import com.openedit.hittracker.SearchQuery;

import java.net.URL;
import java.awt.Dimension;

import com.openedit.page.Page
import com.openedit.util.FileUtils
import com.openedit.util.RequestUtils
import com.openedit.users.UserManager
import com.openedit.users.User

import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import org.apache.commons.io.IOUtils
import org.apache.commons.net.io.Util;
import org.openedit.entermedia.creator.ConversionUtil;
import org.openedit.util.DateStorageUtil;



public class fatwirepublisher extends basepublisher implements Publisher
{
	private static final Log log = LogFactory.getLog(fatwirepublisher.class);
	
	public PublishResult publish(MediaArchive mediaArchive, Asset inAsset, Data inPublishRequest, Data inDestination, Data inPreset)
	{
		//setup result object
		PublishResult result = new PublishResult();
		
		String exportname = inPublishRequest.get("exportname");
		String urlHome = inPublishRequest.get("homeurl");
		String username =  inPublishRequest.get("username");
		String outputfile = inPublishRequest.get("convertpresetoutputfile");
		
		//use default Type and Subtypes since we are dealing with images
		String defaultType = "Image_C";
		String defaultSubtype = "Image";
		String regionid = inPublishRequest.get("regionid");
		if (regionid == null){
			Searcher fatwireregionsearch = mediaArchive.getSearcherManager().getSearcher(mediaArchive.getCatalogId(), "fatwireregion");
			Data defaultfr = fatwireregionsearch.searchByField("default", "true");
			if (defaultfr!=null){
				regionid = defaultfr.getId();
			}
		}
		//if region is still null, then revert to old way of publishing
		if (regionid == null){
			defaultType = defaultSubtype = null;
		}
		
		
		Searcher presetsearch = mediaArchive.getSearcherManager().getSearcher(mediaArchive.getCatalogId(), "convertpreset");
		String presetid = inPublishRequest.get("presetid");
		
		Dimension dimension = null;
		if (presetid!=null)
		{
			//use the dimension defined by the preset
			ConversionUtil cutil = (ConversionUtil) mediaArchive.getModuleManager().getBean( "conversionUtil");
			dimension = cutil.getConvertPresetDimension(mediaArchive.getCatalogId(), presetid);
		}
		
		
		if (outputfile == null || outputfile.isEmpty())
		{
			Data d = (Data) presetsearch.searchById(presetid);
			outputfile = d.get("outputfile");
			if (outputfile != null && outputfile.isEmpty()) outputfile = null;
		}
		
		
		Data thumbpreset = presetsearch.searchById("thumbimage");//get thumbnail data
		
		UserManager usermanager = (UserManager) mediaArchive.getModuleManager().getBean("userManager");
		User inUser = usermanager.getUser(username);
		String copyrightstatus = inAsset.get("copyrightstatus");
		Searcher searcher = mediaArchive.getSearcherManager().getSearcher(mediaArchive.getCatalogId(), "copyrightstatus");
		String usage = null;
		if (copyrightstatus!=null)
		{
			Data data = searcher.searchById(copyrightstatus);
			usage = data.get("name");
		}
		//failsafe
		if (exportname == null || urlHome == null || username == null || outputfile == null)
		{
			log.info("internal error: unable to publish to fatwire (exportname=${exportname} urlHome=${urlHome} username=${username} outputfile=${outputfile}");
			result.setComplete(true);
			result.setErrorMessage("Error publishing to FatWire: variables have not been set");
			return result;
		}
		//this does the actual publishing
		Object fatwireManager = mediaArchive.getModuleManager().getBean( "fatwireManager");
		try {
			fatwireManager.setMediaArchive(mediaArchive);
			
			Object assetBean = null;
			if (regionid!=null && defaultType!=null && defaultSubtype!=null )
			{
				//pushAsset(inAsset, regionId, defaultType, defaultSubtype, inUser, inUrlHome, inUsage, exportName, outputFile);
				assetBean = fatwireManager.pushAsset(inAsset, regionid, defaultType, defaultSubtype, inUser, urlHome, usage, exportname, outputfile, dimension);
			} 
			else
			{
				assetBean = fatwireManager.pushAsset(inAsset, inUser, urlHome, usage, exportname, outputfile, dimension);
			}
			if (assetBean != null)
			{
				String newId = assetBean.getId();
				inPublishRequest.setProperty("trackingnumber",newId);
				inPublishRequest.setProperty("date",DateStorageUtil.getStorageUtil().formatForStorage(new Date()));
				inPublishRequest.setProperty("regionid",regionid);
				
				log.info("response from publishing request to FatWire: newId ${newId}");
				
				//ftp images to fatwire server
				Searcher publishdestinationsearch = mediaArchive.getSearcherManager().getSearcher(mediaArchive.getCatalogId(), "publishdestination");
				SearchQuery fatwirequery = publishdestinationsearch.createSearchQuery().append("name", "FatWire");
				Data fatwireData = publishdestinationsearch.searchByQuery(fatwirequery);
				
				String ftpServer = fatwireData.get("ftpserver");
				String ftpUsername = fatwireData.get("ftpusername");
				User ftpUser = usermanager.getUser(ftpUsername);
				String ftpPwd = usermanager.decryptPassword(ftpUser);
				
				Page original = findInputPage(mediaArchive,inAsset,inPreset);
//				Page thumb = findInputPage(mediaArchive,inAsset,thumbpreset);
				
				log.info("preparing to ftp, image ${original}");
				
				ArrayList<String> images = new ArrayList<String>();
				ArrayList<Page> pages = new ArrayList<Page>();
				
				Iterator itr = assetBean.getAttributes().iterator();
				while(itr.hasNext())
				{
					Object att = itr.next();
//					if (att.getName() != null && att.getName().equals("thumbnailurl"))
//					{
//						Object attdata = att.getData();
//						String to = (attdata!=null ? attdata.getStringValue() : null);
//						if (to!=null)
//						{
//							if (to.startsWith("/image/EM/"))
//							{
//								to = to.substring("/image/EM/".length());
//							}
//							pages.add(thumb);
//							images.add(to);
//						}
//					}
//					else
					if (att.getName() != null && att.getName().equals("imageurl"))
					{
						Object attdata = att.getData();
						String to = (attdata!=null ? attdata.getStringValue() : null);
						if (to!=null)
						{
							if (to.startsWith("/image/EM/"))
							{
								to = to.substring("/image/EM/".length());
							}
							pages.add(original);
							images.add(to);
							break;
						}
					}
				}
				
				ftpPublish(ftpServer, ftpUsername, ftpPwd, pages, images, result);
				result.setComplete(true);
			}
			else 
			{
				log.info("Error publishing asset: asset bean is NUll");
				result.setComplete(true);
				result.setErrorMessage("Error publishing to FatWire: unable to publish asset");
			}
		}
		catch (IOException e)
		{
			log.error(e.getMessage(), e);
			result.setComplete(true);
			result.setErrorMessage(e.getMessage());
		}
		catch (Exception e)
		{
			log.error(e.getMessage(), e);
			result.setComplete(true);
			result.setErrorMessage(e.getMessage());
		}
		return result;
	}
	
	public void ftpPublish(String servername, String username, String password, ArrayList<Page> from, ArrayList<String> to, PublishResult result)
	{
		log.info("ftpPublish ${servername} ${username} ${to}");
		
		FTPClient ftp = new FTPClient();
		ftp.connect(servername,21);
		ftp.enterLocalPassiveMode();
		int reply = ftp.getReplyCode();
		String replymsg = ftp.getReplyString().trim();
		log.info("ftp client reply="+reply+", message="+replymsg+", is positive code? "+FTPReply.isPositiveCompletion(reply));
		if(!FTPReply.isPositiveCompletion(reply))
		{
			result.setErrorMessage(replymsg);
			ftp.disconnect();
			return;
		}	
		ftp.login(username, password);
		reply = ftp.getReplyCode();
		replymsg = ftp.getReplyString().trim();
		log.info("ftp client reply="+reply+", message="+replymsg.trim()+", is positive code? "+FTPReply.isPositiveCompletion(reply));
		if(!FTPReply.isPositiveCompletion(reply))
		{
			result.setErrorMessage(replymsg);
			ftp.disconnect();
			return;
		}
		ftp.setFileTransferMode(FTPClient.BINARY_FILE_TYPE);
		
		for (int i=0; i < from.size(); i++){
//			File file = new File(from.get(i));
			
			Page page = from.get(i);
			long filelen = page.length();
			String export = to.get(i);
			
			ftp.setFileType(FTPClient.BINARY_FILE_TYPE);
			
			OutputStream os = null;
//			FileInputStream fis = null;
			try
			{
//				fis = new FileInputStream(file);
				os  = ftp.storeFileStream(export);
				long copylen = Util.copyStream(page.getInputStream(), os);
				
				log.info("file export ${export} size="+filelen+", copy size="+copylen);
			}
			finally
			{
				try
				{
					if (page.getInputStream()!=null) page.getInputStream().close();
				}
				catch (Exception e){}
				try
				{
					if (os!=null) os.close();
				} catch (Exception e){}
				try
				{
					ftp.completePendingCommand();
				} catch (Exception e){
					log.error(e.getMessage(), e);
					
					try{
						ftp.disconnect();
					}catch (Exception e2){}
					
					throw e;
				}
			}
			
			reply = ftp.getReplyCode();
			replymsg = ftp.getReplyString().trim();
			boolean ispositive = FTPReply.isPositiveCompletion(reply);
			log.info("ftp client following file copy of ${export} reply="+reply+", message="+replymsg+", ispositive="+ispositive);
			
			if (!ispositive)
			{
				result.setErrorMessage(replymsg);
				ftp.disconnect();
				return;
			}
		}
		if(ftp.isConnected())
		{
			ftp.disconnect();
		}
	}
}