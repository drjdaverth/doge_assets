package conversions.creators;

import java.io.File;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.Data;
import org.openedit.entermedia.Asset;
import org.openedit.entermedia.MediaArchive;
import org.openedit.entermedia.creator.*

import com.coremedia.iso.boxes.BitRateBox;
import com.openedit.page.Page;
import com.openedit.util.ExecResult;
import com.openedit.util.FileUtils;
import com.openedit.util.PathUtilities;

public class audioCreator extends BaseCreator implements MediaCreator
{
	private static final Log log = LogFactory.getLog(audioCreator.class);

	public boolean canReadIn(MediaArchive inArchive, String inOutputType)
	{
		return true;
	}
	

	public ConvertResult convert(MediaArchive inArchive, Asset inAsset, Page converted, ConvertInstructions inStructions)
	{
		ConvertResult result = new ConvertResult();

		if(!inStructions.isForce() && converted.length() > 1 )
		{
			result.setOk(true);
			result.setComplete(true);
			return result;
		}
		
		Page inputpage = inArchive.findOriginalMediaByType("audio",inAsset);

		if( inputpage == null || !inputpage.exists())
		{
			//no such original
			log.info("Original does not exist: " + inAsset.getSourcePath());
			result.setOk(false);
			
			return result;
		}
		
		
		/*
		 * 
	<property id="flac" rendertype="audio"  synctags="false">Flac</property>
	<property id="m4a" rendertype="audio"  synctags="false">M4A</property>
	<property id="aac" rendertype="audio"  synctags="false">aac</property>
		 */
		if (inStructions.isForce() || !converted.exists() || converted.getContentItem().getLength() == 0)
		{
			String inputExt = PathUtilities.extractPageType(inputpage.getContentItem().getAbsolutePath());
			String outputExt = inStructions.getOutputExtension();
			String useoriginalmediawhenpossible = inStructions.getProperty("useoriginalmediawhenpossible");
			if( Boolean.parseBoolean(useoriginalmediawhenpossible) && outputExt != null && outputExt.equals(inputExt))
			{
				createFallBackContent(inputpage, converted);
				result.setOk(true);
			}
			else
			{
				String inOutputType = inStructions.getOutputExtension();
				if( "wma".equalsIgnoreCase(inputExt) || "aac".equalsIgnoreCase(inputExt) || "m4a".equalsIgnoreCase(inputExt) || "flac".equalsIgnoreCase(inputExt) || "ogg".equalsIgnoreCase(inputExt))
				{
					String abspath = inputpage.getContentItem().getAbsolutePath();
					runFfmpeg(abspath, converted, inStructions, result);
				}
				else
				{
					runLame(inputpage, converted, inStructions, result);
				}
			}
		}
		if( result.isOk() )
		{
			result.setComplete(true);
		}

		return result;
	}
	private void runLame(Page input, Page output, ConvertInstructions inStructions, ConvertResult result)
	{
		String inputExt = PathUtilities.extractPageType(input.getContentItem().getAbsolutePath());
		long start = System.currentTimeMillis();
		
		//InputStream inputstream = null;
		try
		{
			//inputstream = input.getInputStream();
			List args = new ArrayList();
			String bitRate = inStructions.getProperty("bitrate");
			if(bitRate == null)
			{
				bitRate = "96";
			}
			args.add("-b");
			args.add(bitRate);
			
			//11.025 kHz 22.050 kHz or 44.100 kHz.
			String resample = inStructions.getProperty("resample");
			if( resample == null)
			{
				resample = "22.05";
			}
			args.add("--resample");
			args.add(resample);
			
			if( inputExt == "mp2" )
			{
				args.add("--mp2input");
			}
					
			if( inputExt == "mp3" )
			{
				args.add("--mp3input");
			}
			args.add("--silent");
			//args.add("-");
			if( isOnWindows())
			{
				args.add("\"" + input.getContentItem().getAbsolutePath() + "\"");
				args.add("\"" + output.getContentItem().getAbsolutePath() + "\"");
			}
			else
			{
				args.add( input.getContentItem().getAbsolutePath() );
				args.add( output.getContentItem().getAbsolutePath() );
			}
			//make sure this folder exists
			new File( output.getContentItem().getAbsolutePath() ).getParentFile().mkdirs();
			
			ExecResult res = getExec().runExec("lame", args);
			result.setOk( res.isRunOk());
		}
		catch (Exception ex)
		{
			StringWriter out = new StringWriter();
			ex.printStackTrace(new PrintWriter(out));
			log.error(out.toString());
			result.setError(out.toString());
			result.setOk(false);
		}
//		finally
//		{
//			FileUtils.safeClose(inputstream);
//		}
		String message = "mp3 created";
		if (!result.isOk())
		{
			message = "mp3 creation failed";
		}
		log.info( message + " in " + (System.currentTimeMillis() - start)/1000L + " seconds" );
	}
	
	private void runFfmpeg(String abspath, Page output, ConvertInstructions inStructions, ConvertResult result) 
	{
		long start = System.currentTimeMillis();
		
		ArrayList<String> comm = new ArrayList<String>();
		comm.add("-i");
		comm.add(abspath);
		comm.add("-y");
		//audio
		comm.add("-acodec");
		comm.add("libmp3lame");
		//comm.add("libfaac"); //libfaac  libmp3lame
		comm.add("-ab");
		String bitRate = inStructions.getProperty("bitrate");
		if( bitRate == null )
		{
			bitRate = "96";
		}
		comm.add( bitRate + "k");
		//					comm.add("-ar");
		//					comm.add("44100");
		comm.add("-ac");
		comm.add("1"); //mono

		String outpath = null;

		outpath = output.getContentItem().getAbsolutePath();
		comm.add(outpath);
		new File( outpath).getParentFile().mkdirs();
		//Check the mod time of the video. If it is 0 and over an hour old then delete it?

		boolean ok =  runExec("ffmpeg", comm);
		result.setOk(ok);
		log.info( "ok: ${ok} in " + (System.currentTimeMillis() - start)/1000L + " seconds" );
	}
	public ConvertResult applyWaterMark(MediaArchive inArchive, File inConverted, File inWatermarked, ConvertInstructions inStructions)
	{
		return null;
	}
	
//	public String createConvertPath(ConvertInstructions inStructions)
//	{
//		String path = inStructions.getAssetSourcePath() + "audio." + inStructions.getOutputExtension();
//		
//		return path;
//	}
	public String populateOutputPath(MediaArchive inArchive, ConvertInstructions inStructions)
	{
		StringBuffer path = new StringBuffer();
		String prefix = inStructions.getProperty("pathprefix");
		if( prefix != null)
		{
			path.append(prefix);
		}
		else
		{
			path.append("/WEB-INF/data");
			path.append(inArchive.getCatalogHome());
			path.append("/generated/");
		}
		path.append(inStructions.getAssetSourcePath());
		
		path.append("/audio." + inStructions.getOutputExtension());
		inStructions.setOutputPath(path.toString());
		return path.toString();
	}


	

	
}