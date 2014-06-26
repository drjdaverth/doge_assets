package importing;

import org.openedit.Data
import org.openedit.MultiValued;
import org.openedit.data.BaseData
import org.openedit.data.Searcher
import org.openedit.entermedia.Asset
import org.openedit.entermedia.MediaArchive
import org.openedit.entermedia.scanner.PresetCreator
import org.openedit.util.DateStorageUtil
import com.openedit.page.Page

import com.openedit.hittracker.HitTracker
import com.openedit.hittracker.SearchQuery

public void createTasksForUpload() throws Exception
{
	PresetCreator presets = new PresetCreator();
	 
	mediaarchive = (MediaArchive)context.getPageValue("mediaarchive");//Search for all files looking for videos

	Searcher tasksearcher = mediaarchive.getSearcherManager().getSearcher (mediaarchive.getCatalogId(), "conversiontask");
	Searcher presetsearcher = mediaarchive.getSearcherManager().getSearcher (mediaarchive.getCatalogId(), "convertpreset");
	Searcher destinationsearcher = mediaarchive.getSearcherManager().getSearcher (mediaarchive.getCatalogId(), "publishdestination");
	
	Searcher publishqueuesearcher = mediaarchive.getSearcherManager().getSearcher (mediaarchive.getCatalogId(), "publishqueue");

	MediaArchive mediaArchive = context.getPageValue("mediaarchive");//Search for all files looking for videos
	Searcher targetsearcher = mediaArchive.getAssetSearcher();
	
	//There is a chance that the index is out of date. 
	
	SearchQuery q = targetsearcher.createSearchQuery();
	String ids = context.getRequestParameter("assetids");
	//log.info("Found ${ids} assets from context ${context}");
	
	if( ids == null)
	{
		//Do a search for importstatus of "added" -> "converted"
		q.addOrsGroup( "importstatus", "imported reimported" );
	}
	else
	{
		String assetids = ids.replace(","," ");
		q.addOrsGroup( "id", assetids );
	}
	
	List assets = new ArrayList(targetsearcher.search(q) );
	if( assets.size() == 0 )
	{
		log.error("Problem with import, no asset found");
	}
	boolean foundsome = false;
	assets.each
	{
		foundsome = false;
		Asset asset = mediaArchive.getAsset(it.id);
		
		String rendertype = mediaarchive.getMediaRenderType(asset.getFileFormat());
		SearchQuery query = presetsearcher.createSearchQuery();
		query.addMatches("onimport", "true");
		query.addMatches("inputtype", rendertype); //video

		HitTracker hits = presetsearcher.search(query);
	//	log.info("Found ${hits.size()} automatic presets");
		hits.each
		{
			Data hit = it;
		//	Data newconversion = tasksearcher.createNewData();

			Data preset = (Data) presetsearcher.searchById(it.id);
			
			//TODO: Move this to a new script just for auto publishing
			presets.createPresetsForPage(tasksearcher, preset, asset,0);
			
			String pages = asset.get("pages");
			if( pages != null )
			{
				int npages = Integer.parseInt(pages);
				if( npages > 1 )
				{
					for (int i = 1; i < npages; i++)
					{
						presets.createPresetsForPage(tasksearcher, preset, asset, i + 1);
					}
				}
			}
			foundsome = true;
		}
		//Add auto publish queue tasks
		saveAutoPublishTasks(publishqueuesearcher,destinationsearcher, presetsearcher, asset, mediaArchive)

		
		if( foundsome )
		{
			asset.setProperty("importstatus","imported");
			if( asset.get("previewstatus") == null)
			{
				asset.setProperty("previewstatus","converting");
			}
			//runconversions will take care of setting the importstatus
		}
		else
		{
			asset.setProperty("importstatus","complete");
			asset.setProperty("previewstatus","mime");
		}
		mediaarchive.saveAsset( asset, user );
	}
	if( foundsome )
	{
		//PathEventManager pemanager = (PathEventManager)moduleManager.getBean(mediaarchive.getCatalogId(), "pathEventManager");
		//pemanager.runPathEvent("/${mediaarchive.getCatalogId()}/events/conversions/runconversions.html",context);
		mediaarchive.fireSharedMediaEvent("importing/importcomplete");
	}
	else
	{
		log.info("No assets found");
	}
	
}

private saveAutoPublishTasks(Searcher publishqueuesearcher, Searcher destinationsearcher, Searcher presetsearcher, Asset asset, MediaArchive mediaArchive) {
	SearchQuery autopublish = destinationsearcher.createSearchQuery();
	autopublish.addMatches("onimport", "true");

	HitTracker destinations = destinationsearcher.search(autopublish);

	destinations.each
	{
		MultiValued destination = it;
		Collection destpresets = destination.getValues("convertpreset");

		destpresets.each
		{
			String destpresetid = it;
			Data destpreset = presetsearcher.searchById(destpresetid);
			Data publishrequest = publishqueuesearcher.createNewData();
			publishrequest.setSourcePath(asset.getSourcePath());
			publishrequest.setProperty("status", "pending"); //pending on the convert to work
			publishrequest.setProperty("assetid", asset.id);
			publishrequest.setProperty("presetid", destpresetid);
			String nowdate = DateStorageUtil.getStorageUtil().formatForStorage(new Date() );
			publishrequest.setProperty("date", nowdate);

			publishrequest.setProperty("publishdestination", destination.id);
			String exportName=null;

			if( destpreset.get("type") != "original")
			{
				exportName = mediaArchive.asExportFileName( asset, destpreset);
			}
			if( exportName == null)
			{
				Page inputpage = mediaArchive.getOriginalDocument(asset);
				exportName = inputpage.getName();
			}
			publishrequest.setProperty ("exportname", exportName);

			publishqueuesearcher.saveData(publishrequest, context.getUser());
		}
	}
}


createTasksForUpload();

