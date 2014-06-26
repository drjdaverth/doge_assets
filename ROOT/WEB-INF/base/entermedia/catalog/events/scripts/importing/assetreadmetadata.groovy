package importing;

import java.rmi.server.LoaderHandler;

import com.openedit.page.Page

import org.openedit.data.Searcher
import org.openedit.entermedia.Asset
import org.openedit.entermedia.MediaArchive
import org.openedit.*;

import com.openedit.WebPageRequest;
import com.openedit.hittracker.*;

import org.openedit.entermedia.scanner.MetaDataReader;

public void init()
{
		MediaArchive archive = context.getPageValue("mediaarchive");
		String assetid = context.findValue("assetid");
		log.info("Reading metadata for asset $assetid");
		Searcher searcher = archive.getAssetSearcher();
		
		Asset asset = archive.getAsset("$assetid");
		if (asset!=null){
			MetaDataReader reader = moduleManager.getBean("metaDataReader");
			Page content = archive.getOriginalDocument( asset );
			reader.populateAsset(archive, content.getContentItem(), asset);
			archive.saveAsset(asset, null);
			log.info("metadata reading complete");
		} else {
			log.info("unable to find $assetid, aborting");
		}
}

init();