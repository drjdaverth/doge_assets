import org.openedit.Data 

import org.openedit.Data;



import org.apache.commons.io.FileDeleteStrategy.ForceFileDeleteStrategy;

import org.apache.commons.io.FileDeleteStrategy.ForceFileDeleteStrategy;

import org.openedit.entermedia.modules.*;
import org.openedit.entermedia.edit.*;
import org.openedit.*;
import com.openedit.page.*;
import org.openedit.entermedia.*;
import org.openedit.data.Searcher;
import com.openedit.hittracker.*;
import org.openedit.entermedia.creator.*


public void init()
{
	MediaArchive mediaArchive = context.getPageValue("mediaarchive");//Search for all files looking for videos
	Searcher targetsearcher = mediaArchive.getAssetSearcher();
	Searcher relatedassetsearcher = mediaArchive.getSearcherManager().getSearcher(mediaArchive.getCatalogId(), "relatedasset");
	
	SearchQuery q = targetsearcher.createSearchQuery();
	String ids = context.getRequestParameter("assetids");
	if( ids != null)
	{
		q.setAndTogether(false);
		String[] assetids = ids.split(",");
		for (int i = 0; i < assetids.length; i++)
		{
			q.addMatches("id",assetids[i]);
		}
	}
	else
	{
		q.addMatches("category", "index");
	}

	HitTracker assets = targetsearcher.search(q);
	assets.setHitsName("hits");
	context.setRequestParameter("hitssessionid", assets.getSessionId());
	context.putSessionValue(assets.getSessionId(), assets);
	for(Data hit in assets){
		Asset target = mediaArchive.getAsset (hit.getId());
		if(target.getRelatedAssets() != null){
			for(RelatedAsset related in target.getRelatedAssets()){
				Data newline = relatedassetsearcher.createNewData();
				newline.setProperty "targetid", related.getId();
				newline.setProperty "targetcatalogid", related.getRelatedToCatalogId();
				newline.setProperty "assetid", related.getAssetId();
				newline.setProperty "type", related.getType();
				newline.setSourcePath target.getSourcePath();
				relatedassetsearcher.saveData newline, null;
			}
		} 
	}
	log.info("assets converted");
	
}

init();
