package publishing.publishers;

import java.io.File;
import java.util.Iterator;

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.openedit.Data
import org.openedit.data.Searcher
import org.openedit.entermedia.Asset
import org.openedit.entermedia.MediaArchive
import org.openedit.entermedia.publishing.*

import com.openedit.page.Page
import com.openedit.util.FileUtils

public class filecopypublisher extends basepublisher implements Publisher
{
	private static final Log log = LogFactory.getLog(filecopypublisher.class);
	
	
	
	
	public PublishResult publish(MediaArchive mediaArchive,Asset inAsset, Data inPublishRequest,  Data inDestination, Data inPreset)
	{
		PublishResult result = new PublishResult();
		
		Page inputpage = findInputPage(mediaArchive,inAsset,inPreset);
		String destinationpath = inDestination.get("url");
		if(!destinationpath.endsWith("/"))
		{
			destinationpath = destinationpath + "/";
		}
		String exportname = inPublishRequest.get("exportname");
		String guid = inPreset.get("guid");
		
		FileUtils utils = new FileUtils();
		File destination = new File(destinationpath);
		File source = new File(inputpage.getContentItem().getAbsolutePath());
		File finalfile = new File(destination, exportname);
		utils.copyFiles(source, finalfile);
		result.setComplete(true);
		
		log.info("published ${finalfile}");
		return result;
	}
	
}