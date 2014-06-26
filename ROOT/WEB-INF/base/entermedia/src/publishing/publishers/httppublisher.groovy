package publishing.publishers;

import java.io.File;

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.openedit.Data
import org.openedit.data.Searcher
import org.openedit.entermedia.Asset
import org.openedit.entermedia.MediaArchive
import org.openedit.entermedia.publishing.*

import com.openedit.page.Page
import com.openedit.util.FileUtils

public class httppublisher extends basepublisher implements Publisher
{
	private static final Log log = LogFactory.getLog(httppublisher.class);
	
	public PublishResult publish(MediaArchive mediaArchive,Asset inAsset, Data inPublishRequest,  Data inDestination, Data inPreset)
	{
		PublishResult result = new PublishResult();
		
		Page inputpage = findInputPage(mediaArchive,inAsset,inPreset);
		if( inputpage.exists() )
		{
			result.setComplete(true);
		}		
		return result;
	}
	
}