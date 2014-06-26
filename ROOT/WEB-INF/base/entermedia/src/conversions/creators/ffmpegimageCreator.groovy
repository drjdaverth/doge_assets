/*
 * Created on Sep 20, 2005
 */
package conversions.creators;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.Data;
import org.openedit.entermedia.Asset;
import org.openedit.entermedia.MediaArchive;
import org.openedit.entermedia.creator.*

import com.openedit.page.Page;

public class ffmpegimageCreator extends BaseImageCreator
{
	private static final Log log = LogFactory.getLog(ffmpegimageCreator.class);
	protected String fieldCommandName = "avconv"; // ffmpeg -itsoffset 10

	// -deinterlace -i $TRACK -y
	// -vframes 1 -f mjpeg
	// $OUTPUT


	public String getCommandName()
	{
		return fieldCommandName;
	}

	public void setCommandName(String inCommandName)
	{
		fieldCommandName = inCommandName;
	}

	public boolean canReadIn(MediaArchive inArchive, String inFileFormatInput)
	{
		//TODO: Add a bunch of video formats
		if( inFileFormatInput == null)
		{
			return false;
		}
		String lcfileformat = inFileFormatInput.toLowerCase();
		
		//This should also read in WMV... ?
		String type = inArchive.getMediaRenderType(inFileFormatInput);
		if( "video".equals(type))
		{
			return true;
		}
		return false;
		//return "flv".equals(lcfileformat) || "avi".equals(lcfileformat) || (lcfileformat.startsWith("m") && !lcfileformat.equals("mp3"));
	}

	public ConvertResult convert(MediaArchive inArchive, Asset inAsset, Page inOutFile, ConvertInstructions inStructions)
	{
		ConvertResult result = new ConvertResult();
		
		if(!inStructions.isForce() && inOutFile.length() > 0 )
		{
			result.setOk(true);
			result.setComplete(true);
			return result;
		}
		result.setOk(true);

		// We are going to take frames from the converted flv video
//		ConvertInstructions ci = new ConvertInstructions();
//		ci.setAssetSourcePath(inAsset.getSourcePath());
//		ci.setOutputExtension("flv");
//		inArchive.getCreatorManager().getMediaCreatorByOutputFormat("flv").populateOutputPath(inArchive, ci);
		Page input = getPageManager().getPage("/WEB-INF/data" + inArchive.getCatalogHome() + "/generated/" + inAsset.getSourcePath() + "/video.mp4");
		
//		Page input = getPageManager().getPage(ci.getOutputPath());
		
		// Or the original file, if the flv does not exist
		if( !input.exists() || input.length() == 0)
		{
			result.setOk(false);
            log.info("Input not ready yet" + input.getPath() );
			return result;
		}

		String offset = inStructions.getProperty("timeoffset");
		if( offset == null)
		{
			offset = "2";
		}
		try
		{
			offset = String.valueOf(Integer.parseInt(offset));
		}
		catch( Exception e )
		{
			offset = "0";
		}
		
		List<String> com = new ArrayList<String>();

		int jumpoff = Integer.parseInt(offset);
		if( jumpoff > 2 )
		{
			com.add("-ss");
			com.add(String.valueOf( jumpoff - 2 ) );
			offset = "2";
		}

		//com.add("-deinterlace");
		com.add("-i");
		com.add(input.getContentItem().getAbsolutePath()); // TODO: Might need [0] to pick the
		// first image only
		com.add("-y");
		com.add("-vframes");
		com.add("1");
		com.add("-f");
		com.add("mjpeg");

		com.add("-ss");
		com.add(offset);


		// -s 640x480
		// com.add("-s");
		// com.add( (int)inStructions.getMaxScaledSize().getWidth() + "x" +
		// (int)inStructions.getMaxScaledSize().getHeight() + ">" );
		
		String outputpath = inOutFile.getContentItem().getAbsolutePath();
		new File(outputpath).getParentFile().mkdirs();
		com.add(outputpath);
		result.setOutputPath(null);
		long start = System.currentTimeMillis();
		if (runExec(getCommandName(), com))
		{
			log.info("Resize complete in:" + (System.currentTimeMillis() - start) + " " + inOutFile.getName());
			result.setComplete(true);
			result.setOutputPath(inOutFile.getContentItem().getAbsolutePath());
		}
		else
		{
			if(!inOutFile.exists() || inOutFile.length() == 0)
			{
				log.info("Thumnail creation failed " + outputpath);
				result.setOk(false);
				result.setError("creation failed" );
			}
		}

		return result;
	}
	
	public String createConvertPath(ConvertInstructions inStructions)
	{
		String frame = inStructions.getProperty("frame");
		if( frame == null )
		{
			frame="0";
		}
		String path = inStructions.getAssetSourcePath() + "frame" + frame + ".jpg";
		return path;
	}

	
}

