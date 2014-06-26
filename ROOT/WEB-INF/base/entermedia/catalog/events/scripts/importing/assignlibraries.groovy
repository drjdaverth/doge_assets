package importing;

import model.assets.LibraryManager

import org.openedit.data.Searcher
import org.openedit.entermedia.MediaArchive

import com.openedit.hittracker.HitTracker
import com.openedit.page.manage.*

public void setupLibraries()
{
	MediaArchive mediaarchive = context.getPageValue("mediaarchive");
	Searcher assetsearcher = mediaarchive.getAssetSearcher();

	HitTracker assets = assetsearcher.getAllHits();
	
	LibraryManager librarymanager = new LibraryManager();
	librarymanager.log = log;
	librarymanager.assignLibraries(mediaarchive, assets);

}
setupLibraries();

