package conversions.creators;


import java.io.File;
import java.util.ArrayList;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.entermedia.Asset;
import org.openedit.entermedia.MediaArchive;
import org.openedit.entermedia.creator.BaseCreator
import org.openedit.entermedia.creator.ConvertInstructions
import org.openedit.entermedia.creator.ConvertResult
import org.openedit.entermedia.creator.MediaCreator

import com.openedit.page.Page;
import com.openedit.util.ExecResult;
import com.openedit.util.PathUtilities;
import org.openedit.Data;


//apt-get install libavcodec-extra-53

public class ffmpegCreator extends BaseCreator implements MediaCreator
{
	private static final Log log = LogFactory.getLog(this.class);

	public boolean canReadIn(MediaArchive inArchive, String inInput)
	{
		//reads all video formats
		return true;//"flv".equals(inOutput) || mpeg; //This has a bunch of types
	}
	

	public ConvertResult convert(MediaArchive inArchive, Asset inAsset, Page converted, ConvertInstructions inStructions)
	{
		ConvertResult result = new ConvertResult();
		
		if(!inStructions.isForce() && converted.length() > 0 )
		{
			result.setOk(true);
			result.setComplete(true);
			return result;
		}
		
		Page inputpage = inArchive.findOriginalMediaByType("video",inAsset);
		
		if( inputpage == null || !inputpage.exists())
		{
			//no such original
			log.info("Original does not exist: " + inAsset.getSourcePath());
			result.setOk(false);
			return result;
		}
		
		//deal with custom codec
		String videocodec = inAsset.get("videocodec");
		if( videocodec != null && videocodec.contains("G2M") )
		{
			//need to make an alternative input file?
			List comm = new ArrayList();
			comm.add(inputpage.getContentItem().getAbsolutePath());
			Page tmp = getPageManager().getPage(converted.getContentItem().getPath() + ".mkv");
			if( !tmp.exists() )
			{
				comm.add(tmp.getContentItem().getAbsolutePath());
				boolean ok =  runExec("mencodermkv", comm);
				if( ok )
				{
					inputpage = tmp;
				}
			}
		}
		
		String abspath = inputpage.getContentItem().getAbsolutePath();
		
		if (inStructions.isForce() || !converted.exists() || converted.getContentItem().getLength() == 0)
		{
			String inputExt = PathUtilities.extractPageType(inputpage.getContentItem().getAbsolutePath());
			String outputExt = inStructions.getOutputExtension();
			
				ArrayList<String> comm = new ArrayList<String>();
				comm.add("-i");
				comm.add(abspath);
				comm.add("-y");

				//audio
				setValue("acodec","libfaac",inStructions,comm);  //libfaac  libmp3lame libvo_aacenc				

				if( inStructions.get("fpre") == null ) //legacy?
				{
					setValue("ab","96k",inStructions,comm);
					setValue("ar","44100",inStructions,comm);
					setValue("ac","1",inStructions,comm);
				}
				else
				{
					comm.add("-fpre");
					comm.add(inStructions.get("fpre"));
				}
				
				//video
				setValue("vcodec","libx264",inStructions,comm);				
				setValue("preset",null,inStructions,comm);
				setValue("vpre",null,inStructions,comm);  //legacy?
				setValue("crf","28",inStructions,comm); //legacy?
				setValue("framerate",null,inStructions,comm);
				
				//One-pass CRF (Constant Rate Factor) using the slow preset. One-pass CRF is good for general encoding and is what I use most often. Adjust -crf to change the quality. Lower numbers mean higher quality and a larger output file size. A sane range is 18 to 28.
				//ffmpeg -i input.avi -acodec libfaac -ab 128k -ac 2 -vcodec libx264 -vpre slow -crf 22 -threads 0 output.mp4
				
				//comm.add("-aspect");
				//comm.add("640:480");
				setValue("threads","2",inStructions,comm); //legacy?
				setValue("b:v",null,inStructions,comm); //legacy?
				setValue("b:a",null,inStructions,comm); //legacy?
				setValue("b",null,inStructions,comm); //legacy?
				setValue("qscale",null,inStructions,comm); //legacy?
				//setValue("qscale",null,inStructions,comm); //legacy?

				if( inStructions.get("setpts") != null ) //what is this?!?
				{
					comm.add("setpts=" + inStructions.get("setpts") + "*PTS");  //one block?
				}
				//add calculations to fix letterbox problems
				//http://howto-pages.org/ffmpeg/
				int width = inStructions.intValue("prefwidth",640);
				int height = inStructions.intValue("prefheight",360);
				
//				if( inStructions.getMaxScaledSize() != null )
//				{
//					width = inStructions.getMaxScaledSize().width;
//					height = inStructions.getMaxScaledSize().height;
//				}
				int aw = inAsset.getInt("width");
				int ah = inAsset.getInt("height");
				if( aw > width || ah > height)
				{
					float ratio = (float)aw / (float)ah;
					float ratiodest = (float)width / (float)height;
					if( ratiodest > ratio ) //is dest wider than the input
					{
						//original video has a wider ratio so we need to adjust height in proportion
						float change = (float)height / (float)ah;
						width = Math.round((float)aw * change);
					}
					else if ( ratiodest < ratio)
					{
						//too wide, need to padd top
						float change = (float)width / (float)aw;
						height = Math.round((float)ah * change);
					}
					else
					{
						//no math needed
					}
					//must be even
					if( ( width % 2 ) != 0 )
					{
						width++;
					}
					if( ( height % 2 ) != 0 )
					{
						height++;
					}
				}
				comm.add("-s");
				comm.add(width + "x"  + height);
				
				
				//640x360 853x480 704x480 = 480p
/*
 Here is a two pass mp4 convertion with mp3 audio
 The second pass lets the bit rate be more constant for buffering downloads
 
				  #ffmpeg -i smb_m48020080421.mov  -vcodec mpeg4  -pass 1 -vtag xvid -r 25 -b 2000k -acodec libmp3lame -s vga -ab 96k -ar 44100  -ac 1   bigmono2k960output2p.mp4
				#ffmpeg -i smb_m48020080421.mov  -vcodec mpeg4  -pass 2 -vtag xvid -r 25 -b 2000k -acodec libmp3lame -s vga -ab 96k -ar 44100  -ac 1   bigmono2k960output2p.mp4

Here is a simple PCM audio format for low CPU devices
//				ffmpeg -i smb_m48020080421.mov  -vcodec mpeg4   -vtag xvid -r 25 -b 2000k -acodec pcm_s16le -s vga  -ar 44100  -ac 1   pcmmono2k960output2p.avi
*/
				String outpath = null;
				boolean h264 = outputExt.equalsIgnoreCase("mp4") || outputExt.equalsIgnoreCase("m4v");
				
				if( h264)
				{
					outpath = converted.getContentItem().getAbsolutePath() + "tmp.mp4";
					File tmp = new File(outpath);
					tmp.deleteOnExit();
					if( tmp.exists())
					{
						long old = tmp.lastModified();
						if( System.currentTimeMillis() - old < (1000 * 60 * 60)   ) //something is processing this
						{
							log.info("Video still being processed, skipping 2nd request");
							return result;
						}
					}
				}
				else
				{
					outpath = converted.getContentItem().getAbsolutePath();
				}
				comm.add(outpath);
				new File( outpath).getParentFile().mkdirs();
				//Check the mod time of the video. If it is 0 and over an hour old then delete it?
				
				boolean ok =  runExec("avconv", comm);
				result.setOk(ok);
				if( !ok )
				{
					ExecResult execresult = getExec().runExec("avconv", comm, true);
					String output = execresult.getStandardError();
					result.setError(output);
					return result;
				}
				if(ok && h264)
				{
					comm = new ArrayList();
					comm.add(converted.getContentItem().getAbsolutePath()  + "tmp.mp4");
					comm.add(converted.getContentItem().getAbsolutePath());
					ok =  runExec("qt-faststart", comm);
					result.setOk(ok);
					Page old = getPageManager().getPage(converted.getContentItem().getPath() + "tmp.mp4");
					old.getContentItem().setMakeVersion(false);
					getPageManager().removePage(old);
				}
				result.setComplete(true);
		}
		else
		{
			log.info("FFMPEG Conversion not required, already complete. ");
			result.setOk(true);
			result.setComplete(true);
		}
		return result;
	}
	
	protected void setValue(String inName, String inDefault,ConvertInstructions inStructions, List comm)
	{
		String value = inStructions.get(inName);
		if( value != null || inDefault != null)
		{
			comm.add("-" + inName );
			if( value != null)
			{
				comm.add(value);
			}
			else if( inDefault != null)
			{
				comm.add(inDefault);
			}
		}
	
	}
	
	public ConvertResult applyWaterMark(MediaArchive inArchive, File inConverted, File inWatermarked, ConvertInstructions inStructions)
	{
		return null;
	}
	
	public String createConvertPath(ConvertInstructions inStructions)
	{
		String path = inStructions.getAssetSourcePath() + "video." + inStructions.getOutputExtension();
		
		return path;
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
		
		path.append("/video." + inStructions.getOutputExtension());
		inStructions.setOutputPath(path.toString());
		return path.toString();
	}
	
}