package conversions.creators;

import java.io.File;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.Data;
import org.openedit.entermedia.Asset;
import org.openedit.entermedia.MediaArchive;
import org.openedit.entermedia.creator.*;
import com.openedit.OpenEditException;
import com.openedit.page.Page;
import com.openedit.util.ExecResult;
import com.openedit.util.FileUtils;
import com.openedit.util.PathUtilities;

public class lameMp3CreatorOLD extends BaseCreator
{
	private static final Log log = LogFactory.getLog(lameMp3CreatorOLD.class);
	
	
	public boolean canReadIn(MediaArchive inArchive, String inOutputType)
	{
		return "au".equalsIgnoreCase(inOutputType) || "wav".equalsIgnoreCase(inOutputType) || "mp3".equalsIgnoreCase(inOutputType);
	}

	
	public ConvertResult convert(MediaArchive inArchive, Asset inAsset, Page converted, ConvertInstructions inStructions) throws OpenEditException
	{		
		ConvertResult result = new ConvertResult();

		Page input = inArchive.findOriginalMediaByType("audio",inAsset);
		if(input == null || !input.exists())
		{
			throw new OpenEditException("Input File: " + inAsset.getSourcePath() + " was not found.");
		}
		long start = System.currentTimeMillis();
		// If both are MP3 the just make virtual file
		String inputExt = PathUtilities.extractPageType(input.getContentItem().getAbsolutePath());
		String outputExt = inStructions.getOutputExtension();
		
		String allways = inStructions.getProperty("alwayscreate");
		if( outputExt != null && outputExt.equals(inputExt) && allways != "false")
		{
			createFallBackContent(input, converted);
			result.setOk(true);
		}
		else
		{
			InputStream inputstream = null;
			try
			{
				inputstream = input.getInputStream();
				List args = new ArrayList();
				String bitRate = inStructions.getProperty("bitrate");
				if(bitRate == null)
				{
					bitRate = "64";
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
				args.add("-");
				if( isOnWindows())
				{
					args.add("\"" + converted.getContentItem().getAbsolutePath() + "\"");
				}
				else
				{
					args.add( converted.getContentItem().getAbsolutePath() );
				}
				//make sure this folder exists
				new File( converted.getContentItem().getAbsolutePath() ).getParentFile().mkdirs();
				
				 ExecResult res = getExec().runExec("lame", args, inputstream);
				result.setOk( res.isRunOk());
			}
			catch (Exception ex)
			{
				StringWriter out = new StringWriter();
				ex.printStackTrace(new PrintWriter(out));
				log.error(out.toString());
				result.setError(out.toString());
				result.setOk(false);
				return result;
			}
			finally
			{
				FileUtils.safeClose(inputstream);
			}
		}
		String message = "mp3 created";
		if (!result.isOk())
		{
			message = "mp3 creation failed";
		}
		log.info( message + " in " + (System.currentTimeMillis() - start)/1000L + " seconds" );
		return result;
	}

	
	public ConvertResult applyWaterMark(MediaArchive inArchive,
			File inConverted, File inWatermarked,
			ConvertInstructions inStructions)
	{
		throw new OpenEditException("Not implemented");
	}


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

		// If both the input and output extensions are MP3 then use the original file
//		if( "mp3".equals(inInStructions.getInputExtension()) && "mp3".equals(inInStructions.getOutputExtension()))
//		{
//			path = inInStructions.getInputPath();
//		}
		return path.toString();
	}


}
