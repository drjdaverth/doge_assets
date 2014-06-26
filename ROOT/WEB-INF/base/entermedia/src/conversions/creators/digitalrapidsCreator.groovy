package conversions.creators;

import org.openedit.Data;
import org.openedit.entermedia.Asset;
import org.openedit.entermedia.MediaArchive;
import org.openedit.entermedia.creator.ConvertInstructions;
import org.openedit.entermedia.creator.ConvertResult;
import java.io.File;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openedit.entermedia.Asset;
import org.openedit.entermedia.MediaArchive;
import org.openedit.entermedia.creator.BaseCreator 
import org.openedit.entermedia.creator.ConvertInstructions 
import org.openedit.entermedia.creator.ConvertResult 
import org.openedit.entermedia.creator.MediaCreator 
import com.openedit.OpenEditException 
import com.openedit.page.Page;
import com.openedit.util.FileUtils;
import org.openedit.Data;
import com.openedit.util.PathUtilities;

public class digitalrapidsCreator extends BaseCreator
{
	private static final Log log = LogFactory.getLog(this.class);

	protected FileUtils fieldFileUtils;
	
	
	public boolean canReadIn(MediaArchive inArchive, String inInput)
	{
		return true;//"flv".equals(inOutput) || mpeg; //This has a bunch of types
	}
	

	public ConvertResult convert(MediaArchive inArchive, Asset inAsset, Page converted, ConvertInstructions inStructions)
	{
		ConvertResult result = new ConvertResult();
		http://uhfhdtvantenna.blogspot.com/
		Page inputpage = inArchive.findOriginalMediaByType("video",inAsset);
		if( inputpage == null || !inputpage.exists())
		{
			//no such original
			log.info("Input does not exist: " + inAsset.getSourcePath());
			result.setOk(false);
			return result;
		}
		
            if (!inStructions.isForce() && converted.exists() && converted.getContentItem().getLength() != 0)
            {
                            result.setComplete(true);
                            result.setOk(true);
                            log.info("Generated file already existed: ${converted}");
                            return result;
            }


            String inputfolderholding = inStructions.getProperty("inputfolderholding");
            if( inputfolderholding == null)
            {
                    throw new OpenEditException("No inputfolderholding property set");
            }
            File folder = new File(inputfolderholding);
            folder.mkdirs();

            String outputholdingfile = inputfolderholding + "/" + inputpage.getName();

            FileOutputStream output = new FileOutputStream(outputholdingfile);
            InputStream inputstream  = inputpage.getInputStream();
            try
            {
                    log.info("Copy file to : " +  outputholdingfile);
                    getFileUtils().copyFiles(inputstream, output);
            }
            finally
            {
                    getFileUtils().safeClose(inputstream);
                    getFileUtils().safeClose(output);
            }

            //Now move the file from holding to input
            String inputfolder = inStructions.getProperty("inputfolder");
            if( inputfolder == null)
            {
                    throw new OpenEditException("No inputfolder property set");
            }

            File infolder = new File(inputfolder);
            infolder.mkdirs();

            String finalinputfile = inputfolder + "/" +  inputpage.getName();
            getFileUtils().move(outputholdingfile, finalinputfile);

            result.setOk(true);
            result.setComplete(false);
            return result;
	}
	
	
	public ConvertResult updateStatus(MediaArchive inArchive,Data inTask, Asset inAsset,ConvertInstructions inStructions )
	{
		//check the completed folder
		String completefolder = inStructions.getProperty("completefolder");
		if( completefolder == null)
		{
			throw new OpenEditException("No completefolder property set");
		}
		ConvertResult result = new ConvertResult();
		result.setOk(false);
		
		File completefile = new File(completefolder, inAsset.getName() );
		if(completefile.exists())
		{
			String outputfolder = inStructions.getProperty("outputfolder");
			if( outputfolder == null)
			{
				throw new OpenEditException("No outputfolder property set");
			}
			
			String pagename = PathUtilities.extractPageName(inAsset.getName());
			String extension = inStructions.getProperty("outputextension");
			
			File tempname = new File( outputfolder, pagename + "." + extension);
			
			
			
			if( tempname.exists() )
			{
				String catalogid = inArchive.getCatalogId();
				//TODO: use task variable for proxy filename
				Page proxy = getPageManager().getPage("/WEB-INF/data/" + catalogid + "/generated/" + inAsset.getSourcePath() + "/video.mp4");
				log.info("moving proxy to " + proxy);
				getFileUtils().move(new File( tempname.getAbsolutePath()), new File( proxy.getContentItem().getAbsolutePath() ) , true );
				result.setComplete(true);
				result.setOk(true);
				return result;
			}
		}
		
		//we got here so it's not complete, let's check if it's failed
		String failfolder = inStructions.getProperty("outputfolderfailed");
		File failedfile = new File(failfolder, inAsset.getName() );
		if(failedfile.exists())
		{
			//uh, oh
			result.setError("Digital Rapids put file in error folder: " + inAsset.getName());
			result.setComplete(false);
			result.setOk(false);
		}
		
		return result;
	}

	
	public ConvertResult applyWaterMark(MediaArchive inArchive, File inConverted, File inWatermarked, ConvertInstructions inStructions)
	{
		return null;
	}
	
	public String createConvertPath(ConvertInstructions inStructions)
	{
		String path = inStructions.getAssetSourcePath();
		
		return path;
	}
	public String populateOutputPath(MediaArchive inArchive, ConvertInstructions inStructions)
	{
		Asset asset = inArchive.getAssetBySourcePath(inStructions.getAssetSourcePath());
		Page inputpage = inArchive.getOriginalDocument(asset);
		inStructions.setOutputPath(inputpage.getPath());
		return inStructions.getOutputPath();
	}
	protected FileUtils getFileUtils()
	{
		if (fieldFileUtils == null)
		{
			fieldFileUtils = new FileUtils();
		}
		return fieldFileUtils;
	}
	
}