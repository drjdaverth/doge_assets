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

public class attachmentpublisher extends filecopypublisher implements Publisher
{
	private static final Log log = LogFactory.getLog(attachmentpublisher.class);
	
	public PublishResult publish(MediaArchive mediaArchive,Asset inAsset, Data inPublishRequest,  Data inDestination, Data inPreset)
	{
		//make the asset folder based
		mediaArchive.getAssetEditor().makeFolderAsset(inAsset, null);
		//modify the destination url
		inDestination.setProperty("url", "webapp/WEB-INF/data/" + mediaArchive.getCatalogId() + "/originals/" + inAsset.getSourcePath() + "/");
		return super.publish(mediaArchive, inAsset, inPublishRequest, inDestination, inPreset);
	}

}