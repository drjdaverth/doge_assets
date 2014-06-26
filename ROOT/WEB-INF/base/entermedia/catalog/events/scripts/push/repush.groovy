package push;
import java.text.SimpleDateFormat

import org.openedit.data.Searcher
import org.openedit.entermedia.Asset
import org.openedit.entermedia.MediaArchive
import org.openedit.*;

import com.openedit.hittracker.*;

public void init()
{
		MediaArchive mediaArchive = context.getPageValue("mediaarchive");//Search for all files looking for videos
		Searcher targetsearcher = mediaArchive.getAssetSearcher();
		SearchQuery q = targetsearcher.createSearchQuery();
		
		q.addMatches("category", "index");
		
		//q.addMatches("pushstatus", "complete");
		//q.addBefore("pusheddate", new SimpleDateFormat("MM/dd/yyyy").parse("07/16/2012") );
		//q.addMatches("importstatus", "error");
//                q.addNot("editstatus","7");
//        q.addMatches("editstatus","7");
		q.addSortBy("id");
		HitTracker assets = targetsearcher.search(q);

		assets.setHitsPerPage(10000);

		int count = 0;
		log.info("Starting ${assets.size()} with ${q}"); 
		List assetsToSave = new ArrayList();
		assets.each
		{
			Data hit =  it;
			count++;
			if( hit.get("pushstatus") != "complete" || hit.get("importstatus") != "complete" )
			{
				Asset asset = mediaArchive.getAssetBySourcePath(hit.getSourcePath());
				if( asset != null )
				{
	//	 			String pushstatus = asset.get("pushstatus");
	//				if( pushstatus == "error" )
	//				{
						boolean save = false;
						if( asset.get("pusheddate" ) != null )
						{
							asset.setProperty("pushstatus","complete");
							save = true;
						}
	//				}
	//				if( asset.get("editstatus") == "7")
	//				{
	//					asset.setProperty("importstatus","complete");
	//					assetsToSave.add(asset)
	//				}
					if(assetsToSave.size() == 500)
					{
							mediaArchive.saveAssets assetsToSave;
							assetsToSave.clear();
							log.info("checked ${count} records." );
					}
				}
			}
		}

		mediaArchive.saveAssets assetsToSave;
		log.info("checked ${count} records." );

}

init();