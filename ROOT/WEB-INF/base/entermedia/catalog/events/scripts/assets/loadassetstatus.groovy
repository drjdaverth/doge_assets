package assets;
import java.util.*

import org.openedit.data.Searcher
import org.openedit.entermedia.Asset 
import org.openedit.entermedia.MediaArchive

import com.openedit.hittracker.HitTracker
import com.openedit.hittracker.SearchQuery
import com.openedit.page.manage.*

public void init()
{
	String assetid = context.getRequestParameter("assetid");
	if( assetid == null)
	{
		return;
	}
	boolean complete = conversionsComplete(assetid);
	if( complete )
	{
		//complete = loadPublishing(assetid);
	}
	if( complete )
	{
		//update the asset import status
		//log.info("import complete, saving asset");
		mediaArchive = (MediaArchive)context.getPageValue("mediaarchive");//Search for all files looking for videos
		Asset asset = mediaArchive.getAsset(assetid);
		if( asset != null)
		{
			asset.setProperty("importstatus","complete");
			mediaArchive.saveAsset(asset,null);
		}
	}
}

init();


public boolean conversionsComplete(String assetid){
	mediaarchive = (MediaArchive)context.getPageValue("mediaarchive");//Search for all files looking for videos
	
	Searcher tasksearcher = mediaarchive.getSearcherManager().getSearcher(mediaarchive.getCatalogId(), "conversiontask");
	log.debug("loadssetstatus is checking for new and submitted conversions");
	
	SearchQuery query = tasksearcher.createSearchQuery();
	//query.addOrsGroup("status", "new submitted retry pending");
	
	query.addMatches("assetid", assetid);
	query.setHitsName("convertstatus");
	
	HitTracker newtasks = tasksearcher.search(query);

	List errors = new ArrayList();
	List complete = new ArrayList();
	List remaining = new ArrayList();
	newtasks.each
	{
		if( it.status == "error" )
		{
			errors.add( it );
		}
		else if( it.status == "complete" )
		{
			complete.add( it );
		}
		else
		{
			remaining.add(it);
		}
	}	
	context.putPageValue("conversionsremaining", remaining);
	context.putPageValue("conversions", newtasks);
	context.putPageValue("conversionerrors", errors);
	if( errors.size() > 0)
	{
		return false;
	}
	if(remaining.size() > 0)
	{
		return false;
	}
	//	if( newtasks && newtasks.size() == 0) //We will assume true since these should have been loaded on import
	
	return true;
}



public boolean loadPublishing(String assetid)
{
	mediaarchive = (MediaArchive)context.getPageValue("mediaarchive");//Search for all files looking for videos
	Searcher queuesearcher = mediaarchive.getSearcherManager().getSearcher(mediaarchive.getCatalogId(), "publishqueue");
	log.debug("checking for new and submitted publish tasks");
	SearchQuery query = queuesearcher.createSearchQuery();
	query.addOrsGroup("status", "new submitted retry pending");
	query.addMatches("assetid", assetid);
	query.setHitsName("publishstatus");
	HitTracker newtasks = queuesearcher.search(query);
	context.putPageValue("publish", newtasks);
	
	//get rid of this
	query = queuesearcher.createSearchQuery();
	query.addMatches("status", "error");
	query.addMatches("assetid", assetid);
	query.setHitsName("publishstatuserrors");
	HitTracker errors = queuesearcher.search(query);
	context.putPageValue("publisherrors", errors);
	
	if( newtasks == null || newtasks.size() > 0 || errors.size() > 0)
	{
		return false;
	}	
	return true;
}

