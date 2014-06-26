package utils;

import org.openedit.data.Searcher
import org.openedit.entermedia.MediaArchive

import assets.model.AssetTypeManager
import assets.model.LibraryAddingAssetTypeManager

import com.openedit.hittracker.HitTracker
import com.openedit.hittracker.SearchQuery



public void runit()
{
MediaArchive mediaArchive = context.getPageValue("mediaarchive");
Searcher assetsearcher = mediaArchive.getAssetSearcher();

log.info("Ran");
HitTracker assets = assetsearcher.getAllHits();
AssetTypeManager manager = new LibraryAddingAssetTypeManager();
manager.context = context;
manager.saveAssetTypes(assets);

}

runit();