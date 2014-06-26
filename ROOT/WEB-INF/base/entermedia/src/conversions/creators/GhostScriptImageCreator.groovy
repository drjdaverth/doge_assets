package conversions.creators;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.entermedia.Asset;
import org.openedit.entermedia.MediaArchive;
import org.openedit.entermedia.creator.*

import com.openedit.OpenEditException;
import com.openedit.page.Page;

public class GhostScriptImageCreator extends imagemagickCreator
{
	private static final Log log = LogFactory.getLog(GhostScriptImageCreator.class);
	public boolean canReadIn(MediaArchive inArchive, String inInput)
	{
		//is this list correct?
		//We only need to do this on Windows. 
		if( isOnWindows() )
		{
			return "pdf".equals(inInput) || "eps".equals(inInput) || "ps".equals(inInput);
		}
		return false;
	}
	
	public ConvertResult convert(MediaArchive inArchive, Asset inAsset, Page inPageOut, ConvertInstructions inStructions)
	{
		
		ConvertResult result = new ConvertResult();
		result.setOk(false);
		
		Page input = inArchive.findOriginalMediaByType("image",inAsset);

		
		if( input == null)
		{
			input = inArchive.findOriginalMediaByType("document",inAsset);
			
		}
		if( input == null)
		{
			return result;
		}
		
		File inOut = new File( inPageOut.getContentItem().getAbsolutePath());
		
		// if this is an eps output from an ai input then use ghostscript
		List<String> com = createCommand(inOut, inStructions);
		if( com == null)
		{
			return result;
		}
		
		//check input width
		int width = inAsset.getInt("width");
		int height = inAsset.getInt("height");
		boolean setsize = false;
		if( width > 0 && height > 0  && inStructions.getMaxScaledSize() != null)
		{
			double outputw = inStructions.getMaxScaledSize().getWidth();
			double outputh = inStructions.getMaxScaledSize().getHeight();
			if( width < outputw || height < outputh)
			{
				//-r600x600" -g108x117 need to work together. resolution is normal but then we need to grow size
				float density = ((float)outputw / (float)width) * 72f;
				float densityh = ((float)outputh / (float)height) * 72f;
				//take the smaller of the two
				density = Math.max(density,densityh);
				density = Math.max(72f,density);
				String val = String.valueOf( Math.round(density) );
				com.add("-r"  + val +"x" + val);
				
				//if we go newrez/72 * original size is the geometry
				float newwidth = (density / 72f) * width;
				float newheight = (density / 72f) * height;
				com.add("-g"  + Math.round(newwidth) +"x" + Math.round(newheight));
				setsize = true;
			}
		}
		if( !setsize )
		{
			log.info("asset width and height not set " + inAsset.getId());
			return result;
		}
		String outout = inOut.getAbsolutePath();
		
		//outout = outout.replaceAll("\\\\\\\\", "\\");
		
		com.add("-sOutputFile=" + outout );
		com.add(input.getContentItem().getAbsolutePath());
		// com.add(" -");
		// -q -dBATCH -dSAFER -dMaxBitmap=500000000 -dNOPAUSE -dAlignToPixels=0

		inOut.getParentFile().mkdirs();
		long start = System.currentTimeMillis();
		//log.info("Running " + com + " on " + Thread.currentThread());
		if (runExec("ghostscript", com))
		{
			log.info("Resize complete in:" + (System.currentTimeMillis() - start) + " output " + inOut.getAbsolutePath());
			result.setOk(true);
		}
		else
		{
			log.error("Could not convert " + input.getPath());
		}
		return result;
	}


	protected List<String> createCommand(File inOut, ConvertInstructions inStructions)
	{
		List<String> com = new ArrayList<String>();
		com.add("-q");
		com.add("-dQUIET");
		com.add("-dPARANOIDSAFER");
		com.add("-dBATCH");
		com.add("-dNOPAUSE");
		com.add("-dNOPROMPT");
		com.add("-dMaxBitmap=500000000");
		com.add("-dAlignToPixels=0");
		com.add("-dGridFitTT=0");
		com.add("-dNOPROMPT");

		String mime = inStructions.getOutputExtension();
		if (mime.equals("pdf"))
		{
			com.add("-sDEVICE=pdfwrite");
		}
		else if (mime.equals("jpg"))
		{
			com.add("-sDEVICE=jpeg");
		}
		else if (mime.equals("png"))
		{
			com.add("-sDEVICE=png16m");
		}
		else if (mime.equals("tif"))
		{
			com.add("-sDEVICE=tiff32nc");
		}
		else if (mime.equals("eps"))
		{
			com.add("-sDEVICE=epswrite");
		}
		else
		{
			return null;
		}
		com.add("-dTextAlphaBits=4");
		com.add("-dGraphicsAlphaBits=4");

		
		//"gs" -q -dQUIET -dPARANOIDSAFER -dBATCH -dNOPAUSE -dNOPROMPT -dMaxBitmap=500000000 -dAlignToPixels=0 -dGridFitTT=0 "-sDEVICE=jpeg" -dTextAlphaBits=4 -dGraphicsAlphaBits=4 "-r4431x4431" -g800x862 -dFirstPage=1 -dLastPage=1 -sOutputFile=/home/cburkey/workspace/entermedia/webapp/media/catalogs/junk3/assets/users/admin/hand7.eps/image800x600.jpg /home/cburkey/workspace/entermedia/webapp/WEB-INF/data/media/catalogs/junk3/originals/users/admin/hand7.eps

		
		// New version of Image Magick are 0 based. Windows is 1 based
		int page = inStructions.getPageNumber();
		//page--;
		page = Math.max(1, page);
		com.add("-dFirstPage=" + page);
		com.add("-dLastPage=" + page);
		return com;
	}

	public boolean resizeImage(File inFile, File inOutFile, ConvertInstructions inStructions) throws OpenEditException
	{
		// if this is an eps output from an ai input then use ghostscript
		List<String> com = createCommand(inOutFile, inStructions);
		//com.add("-dDEVICEWIDTHPOINTS=" + inStructions.getMaxScaledSize().width);
		com.add("-sOutputFile=\"" + inOutFile.getAbsolutePath() + "\"");
		com.add("\"" + inFile.getAbsolutePath() + "\"");
		// -q -dBATCH -dSAFER -dMaxBitmap=500000000 -dNOPAUSE -dAlignToPixels=0
		

		
		
		inOutFile.getParentFile().mkdirs();
		long start = System.currentTimeMillis();
		// com.add(" -");
		log.info("Running " + com);
		if (runExec("ghostscript", com))
		{
			log.info("Resize complete in:" + (System.currentTimeMillis() - start) + " output " + inOutFile.getAbsolutePath());
			return true;
		}
		else
		{
			log.error("Could not convert " + inOutFile.getPath());
			return false;
		}
	}

}
