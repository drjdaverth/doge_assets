package publishing;

import org.openedit.Data
import org.openedit.data.Searcher
import org.openedit.entermedia.Asset
import org.openedit.entermedia.MediaArchive
import org.openedit.entermedia.publishing.*
import org.openedit.event.*

import com.openedit.hittracker.HitTracker
import com.openedit.hittracker.SearchQuery
import com.openedit.page.Page


public void init() {

	MediaArchive mediaArchive = (MediaArchive)context.getPageValue("mediaarchive");//Search for all files looking for videos

	Searcher queuesearcher = mediaArchive.getSearcherManager().getSearcher(mediaArchive.getCatalogId(), "publishqueue");


	SearchQuery query = queuesearcher.createSearchQuery();
	WebEvent webevent = context.getPageValue("webevent");
	Asset asset = null;
	if( webevent != null)
	{
		String sourcepath = webevent.getSourcePath();
		if( sourcepath != null )
		{
			asset = mediaArchive.getAssetBySourcePath(sourcepath);
			if( asset != null)
			{
				query.addExact("assetid",asset.getId());
			}
		}
	}
	String assetid = context.getRequestParameter("assetid");
	if(assetid != null){
		query.addExact("assetid", assetid);
	}
	query.addOrsGroup("status","new pending retry");
	query.addNot("remotepublish","true");
	
	HitTracker tracker = queuesearcher.search(query);
	log.info("publishing " + tracker.size() + " assets" + queuesearcher);
	if( tracker.size() > 0)
	{
		for( Data result:tracker)
		{
			Data publishrequest = queuesearcher.searchById(result.getId());
			if( publishrequest == null)
			{
				log.error("Publish queue index out of date");
				continue;
			}
			String presetid = publishrequest.get("presetid");

			if(asset == null)
			{
				assetid = result.get("assetid");
				asset = mediaArchive.getAsset(assetid);
			}
			String resultassetid = result.get("assetid");
			if(asset != null && !asset.getId().equals(resultassetid))
			{
				asset = mediaArchive.getAsset(resultassetid );
			}

			String publishdestination = publishrequest.get("publishdestination");
			
			Data preset = mediaArchive.getSearcherManager().getData(mediaArchive.getCatalogId(), "convertpreset", presetid);
					
			Data destination = mediaArchive.getSearcherManager().getData(mediaArchive.getCatalogId(), "publishdestination",publishdestination);

			try
			{

				Page inputpage = null;
				if( preset.get("type") != "original")
				{
					String input= "/WEB-INF/data/${mediaArchive.catalogId}/generated/${asset.sourcepath}/${preset.outputfile}";
					inputpage= mediaArchive.getPageManager().getPage(input);
				}
				else
				{
					inputpage = mediaArchive.getOriginalDocument(asset);
				}

				if(!inputpage.exists() || inputpage.length() == 0)
				{
					log.info("Input file ${inputpage.getName()} did not exist. Skipping publishing.");
					//Change statys to pending so things can timeout
					publishrequest.setProperty('status', 'pending');
					queuesearcher.saveData(publishrequest, context.getUser());
					continue;
				}

				Publisher publisher = getPublisher(mediaArchive, destination.get("publishtype"));
				PublishResult presult = publisher.publish(mediaArchive,asset,publishrequest, destination,preset);
				if( presult.isError() )
				{
					publishrequest.setProperty('status', 'error');
					publishrequest.setProperty("errordetails", presult.getErrorMessage());
					queuesearcher.saveData(publishrequest, context.getUser());
					firePublishEvent(publishrequest.getId());
					continue;
				}
				if( presult.isComplete() )
				{
					log.info("Published " +  asset + " to " + destination);
					publishrequest.setProperty('status', 'complete');
					publishrequest.setProperty("errordetails", " ");
					queuesearcher.saveData(publishrequest, context.getUser());
					firePublishEvent(publishrequest.getId());
				}
				else if( presult.isPending() )
				{
					publishrequest.setProperty('status', 'pending');
					publishrequest.setProperty("errordetails", " ");
					queuesearcher.saveData(publishrequest, context.getUser());
				}
				//check for remotempublishstatus?
			}
			catch( Throwable ex)
			{
				log.error("Problem publishing ${asset} to ${publishdestination}", ex);
				publishrequest.setProperty('status', 'error');
				if(ex.getCause() != null)
				{
					ex = ex.getCause();
				}
				publishrequest.setProperty("errordetails", "${destination} publish failed ${ex}");
				queuesearcher.saveData(publishrequest, context.getUser());
			}
			asset = null; //This is kind of crappy code.
		
		}
	}
}


protected firePublishEvent(String inOrderItemId)
{
	WebEvent event = new WebEvent();
	event.setSearchType("publishqueue");
	event.setProperty("publishqueueid", inOrderItemId);
	event.setOperation("publishing/publishcomplete");
	event.setUser(context.getUser());
	event.setCatalogId(mediaarchive.getCatalogId());
	mediaarchive.getMediaEventHandler().eventFired(event);

}

protected Publisher getPublisher(MediaArchive inArchive, String inType)
{
//	GroovyClassLoader loader = engine.getGroovyClassLoader();
//	Class groovyClass = loader.loadClass("publishing.publishers.${inType}publisher");
//	Publisher publisher = (Publisher) groovyClass.newInstance();
//	return publisher;
	return moduleManager.getBean("${inType}publisher");
}

init();