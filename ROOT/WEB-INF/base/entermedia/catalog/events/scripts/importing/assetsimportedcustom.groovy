package importing;

import model.assets.LibraryManager

import org.openedit.data.Searcher
import org.openedit.entermedia.Asset
import org.openedit.entermedia.MediaArchive

import assets.model.AssetTypeManager
import assets.model.EmailNotifier

import com.openedit.hittracker.HitTracker
import com.openedit.hittracker.SearchQuery
import com.openedit.page.manage.*

public void setAssetTypes()
{
	String ids = context.getRequestParameter("assetids");
	if( ids == null)
	{
	   log.info("AssetIDS required");
	   return;
	}
	String assetids = ids.replace(","," ");

	MediaArchive mediaArchive = context.getPageValue("mediaarchive");
	Searcher assetsearcher = mediaArchive.getAssetSearcher();
	SearchQuery q = assetsearcher.createSearchQuery();
	q.addOrsGroup( "id", assetids );

	HitTracker assets = assetsearcher.search(q);
	AssetTypeManager manager = new AssetTypeManager();
	manager.context = context;
	manager.log = log;
	manager.saveAssetTypes(assets);
	
	setupProjects(assets);
	
}
public void sendEmail()
{
	EmailNotifier emailer = new EmailNotifier();
	emailer.context = context;
	emailer.emailOnImport();
}

public void setupProjects(HitTracker assets)
{
	//Look at source path for each asset?
	MediaArchive mediaarchive = (MediaArchive)context.getPageValue("mediaarchive");//Search for all files looking for videos
	LibraryManager librarymanager = new LibraryManager();
	librarymanager.log = log;
	librarymanager.assignLibraries(mediaarchive, assets);
		
	
}

public void verifyRules()
{
	String ids = context.getRequestParameter("assetids");
	if( ids == null)
	{
	   log.info("AssetIDS required");
	   return;
	}
	String assetids = ids.replace(","," ");

	MediaArchive mediaArchive = context.getPageValue("mediaarchive");
	Searcher assetsearcher = mediaArchive.getAssetSearcher();
	SearchQuery q = assetsearcher.createSearchQuery();
	q.addOrsGroup( "id", assetids );

	HitTracker assets = assetsearcher.search(q);
	assets.each{
		 Asset asset = mediaArchive.getAsset("${it.id}");
		 if(asset.width != null){
			 int width = Integer.parseInt(asset.width);
			 if(width < 1024){
				 asset.setProperty("editstatus", "rejected");
				 asset.setProperty("notes", "Asset did not meet minimum width criteria.  Width was ${asset.width}");
				 
			 }
			 assetsearcher.saveData(asset, null);
		 }
	}
	
	
	
}


setAssetTypes();
//verifyRules();

//sendEmail();
