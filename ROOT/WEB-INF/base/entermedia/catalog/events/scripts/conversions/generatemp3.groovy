import com.openedit.page.Page
import org.openedit.data.Searcher
import org.openedit.entermedia.Asset
import org.openedit.entermedia.MediaArchive
import org.openedit.*;

import com.openedit.WebPageRequest;
import com.openedit.hittracker.*;

public void init()
{
		MediaArchive archive = context.getPageValue("mediaarchive");//Search for all files looking for videos
		Searcher searcher = archive.getAssetSearcher();
		SearchQuery query = searcher.createSearchQuery();
		query.addMatches("fileformat", "mp3");
		query.addSortBy("id");
		Collection assets = searcher.search(query);
		List assetsToSave = new ArrayList();
		for (Data hit in assets)
		{
			Asset asset = archive.getAssetBySourcePath(hit.get("sourcepath"));
			archive.removeGeneratedImages(asset);
			asset.setProperty("importstatus", "imported");
			asset.setProperty("previewstatus", "converting");
			asset.setProperty("pushstatus", "notallconverted");
			assetsToSave.add(asset);
			if(assetsToSave.size() == 100)
			{
				archive.saveAssets( assetsToSave );
				assetsToSave.clear();
			}
		}
		archive.saveAssets assetsToSave;
		
		pageManager.clearCache();
		
		log.info("now kick off import event");
		archive.fireMediaEvent("importing/assetsimported", user);
}

init();