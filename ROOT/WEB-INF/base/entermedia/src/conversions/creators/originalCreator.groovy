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
import com.openedit.util.PathUtilities;
import org.openedit.Data;


public class originalCreator extends BaseCreator implements MediaCreator
{
	private static final Log log = LogFactory.getLog(this.class);

	public boolean canReadIn(MediaArchive inArchive, String inInput)
	{
		return true;//"flv".equals(inOutput) || mpeg; //This has a bunch of types
	}
	

	public ConvertResult convert(MediaArchive inArchive, Asset inAsset, Page converted, ConvertInstructions inStructions)
	{
		ConvertResult result = new ConvertResult();

		Page inputpage = inArchive.getOriginalDocument(inAsset);

		if( inputpage == null || !inputpage.exists())
		{
			//no such original
			log.info("Original does not exist: " + inAsset.getSourcePath());
			result.setOk(false);
			return result;
		}
		result.setOk(true);
		result.setComplete(true);
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
	
}