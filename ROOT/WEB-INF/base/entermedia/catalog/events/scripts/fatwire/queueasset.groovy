
import java.util.ArrayList;
import java.util.List;
import java.util.Iterator;
import org.openedit.data.Searcher;
import org.openedit.data.SearcherManager;
import org.openedit.entermedia.MediaArchive;
import org.openedit.Data;
import com.openedit.page.Page;
import com.openedit.page.manage.PageManager;
import com.openedit.users.User;
import com.openedit.util.FileUtils;
import com.openedit.util.PathProcessor;
import com.openedit.hittracker.SearchQuery;
import com.openedit.hittracker.HitTracker;
import org.openedit.entermedia.Asset;
import org.openedit.entermedia.scanner.AssetImporter;
import com.openedit.util.XmlUtil;
import org.openedit.repository.ContentItem;
import com.openedit.entermedia.scripts.ScriptLogger;
import java.security.MessageDigest;

import com.openedit.WebPageRequest
import com.openedit.entermedia.scripts.EnterMediaObject
import com.openedit.entermedia.scripts.ScriptLogger;
import com.openedit.page.Page
import com.openedit.servlet.OpenEditEngine
import org.openedit.Data
import org.openedit.entermedia.Asset
import org.openedit.entermedia.MediaArchive
import org.openedit.entermedia.modules.OrderModule
import org.openedit.entermedia.orders.Order

import org.openedit.util.DateStorageUtil


public void init()
{
	log.info("QueueAsset running");
	
	//get mediaarchive
	MediaArchive archive = context.getPageValue("mediaarchive");
	
	//setup searchers
	SearcherManager sm = archive.getSearcherManager();
	Searcher presetsearch = sm.getSearcher(archive.getCatalogId(), "convertpreset");
	Searcher publishqueuesearch = sm.getSearcher(archive.getCatalogId(), "publishqueue");
	Searcher conversionsearch = sm.getSearcher(archive.getCatalogId(), "conversiontask");
	Searcher publishdestinationsearch = sm.getSearcher(archive.getCatalogId(), "publishdestination");
	
	//get form data from page
	String assetId = context.findValue("publishassetid");//asset id that needs to be published
	if (assetId == null){
		log.info("Unable to process request: asset is null");
		return;
	}
	Asset asset = archive.getAsset(assetId);//get the asset from the asset id
	//get the specific presetid if available
	String presetId = context.findValue("presetid");
	//get specific regionid if available
	String regionId = context.findValue("regionid");
	if (regionId == null){
		Searcher fatwireregionsearch = sm.getSearcher(archive.getCatalogId(), "fatwireregion");
		Data defaultfr = fatwireregionsearch.searchByField("default", "true");
		if (defaultfr!=null){
			regionId = defaultfr.getId();
		}
	}
	
	//execute searches
	SearchQuery fatwirequery = publishdestinationsearch.createSearchQuery().append("name", "FatWire");
	Data fatwireData = publishdestinationsearch.searchByQuery(fatwirequery);
	String fatwireId = fatwireData.getId();//this is required because we're passing a fatwire publication task to the publication queue
	
	SearchQuery presetquery = presetsearch.createSearchQuery().append("publishtofatwire", "true");//look for all publishtofatwire values set to true
	HitTracker presethits = presetsearch.search(presetquery);
	if (presethits.size() == 0)
	{
		log.info("Unable to process request: no fatwire presets have been defined");
		//add asset to context
		context.putPageValue("asset",asset);
		return;
	}
	
	//start iterating through presets
	for (int i=0; i < presethits.size(); i++)
	{
		Data preset = presethits.get(i);
		// add extra check for specific presetid
		if (presetId!=null && !preset.getId().equals(presetId)){
			continue;
		}
		
		//get a few key variables
		String outputfile = preset.get("outputfile");
		String exportname = archive.asExportFileName(null, asset, preset);
		
		//create conversion task if necessary
		boolean needstobecreated = true;
		String conversiontaskid = null;
		if( "original".equals( preset.get("type") ) )
		{
			needstobecreated = !archive.getOriginalDocument(asset).exists();
		}
		else if( archive.doesAttachmentExist(outputfile, asset) )
		{
			needstobecreated = false;
		}
		if( needstobecreated )
		{
			SearchQuery q = conversionsearch.createSearchQuery().append("assetid", asset.getId()).append("presetid",preset.getId());
			Data newTask = conversionsearch.searchByQuery(q);
			if( newTask == null )
			{
				newTask = conversionsearch.createNewData();
				newTask.setProperty("status", "new");
				newTask.setProperty("assetid", asset.getId());
				newTask.setProperty("presetid", preset.getId());
				newTask.setSourcePath(asset.getSourcePath());
				conversionsearch.saveData(newTask, null);
			}
			conversiontaskid = newTask.getId();
		}
		
		//add entry to publishqueue
		Data data = publishqueuesearch.createNewData();
		data.setProperty("assetid", assetId);
		data.setProperty("assetsourcepath", asset.getSourcePath());
		data.setProperty("publishdestination", fatwireId);
		data.setProperty("exportname", exportname);
		data.setProperty("presetid", preset.getId());
		data.setProperty("status", "new");
		data.setProperty("date", DateStorageUtil.getStorageUtil().formatForStorage(new Date()));
		String homeUrl = "${context.siteRoot}${home}${apphome}";
		data.setProperty("homeurl",homeUrl);
		String username = context.getUserName();
		data.setProperty("username",username);
		data.setProperty("convertpresetoutputfile",outputfile);
		//add reference to conversiontask id if one was created
		if (conversiontaskid!=null){
			data.setProperty("conversiontaskid", conversiontaskid );
		}
		//add regionid if one was specified
		if (regionId!=null){
			data.setProperty("regionid", regionId);
		}
		publishqueuesearch.saveData(data, null);//save to publishqueue
	}
	//trigger publishing queue
	archive.fireSharedMediaEvent("publishing/publishassets");
	//add asset to context
	context.putPageValue("asset",asset);
	//done message
	log.info("finished processing QueueAsset script");
}


init();