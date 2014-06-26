package model.assets

import org.openedit.Data
import org.openedit.MultiValued
import org.openedit.data.BaseData
import org.openedit.data.Searcher
import org.openedit.entermedia.Asset
import org.openedit.entermedia.MediaArchive

import com.openedit.entermedia.scripts.EnterMediaObject
import com.openedit.hittracker.HitTracker
import com.openedit.hittracker.SearchQuery

public class LibraryManager extends EnterMediaObject
{
	protected Map fieldLibraryFolders = new HashMap();
	protected Object NULL = new BaseData();
	
	public void assignLibraries(MediaArchive mediaarchive, HitTracker assets)
	{
		Searcher searcher = mediaarchive.getAssetSearcher();
		Searcher librarySearcher = mediaarchive.getSearcher("library")
		
		List tosave = new ArrayList();
		int savedsofar = 0;
		for (MultiValued hit in assets)
		{
			def sourcepath = hit.getSourcePath();
			//log.info("try ${sourcepath}" );
			String[] split = sourcepath.split("/");
			String sofar = "";
			for( int i=0;i<split.length - 1;i++)
			{
				if( i > 10 )
				{
					break;
				}
				sofar = "${sofar}${split[i]}";
				Data library = findLibrary(librarySearcher,sofar);
				sofar = "${sofar}/";
		
				if( library != null )
				{
					Collection existing = hit.getValues("libraries");
					if( existing == null || !existing.contains(library.getId()))
					{
						Asset asset = mediaarchive.getAssetBySourcePath(sourcepath);
						asset.addLibrary(library.getId());
						//log.info("found ${sofar}" );
						tosave.add(asset);
						savedsofar++;
						break;
					}
				}
			}
			if(tosave.size() == 100)
			{
				searcher.saveAllData(tosave, null);
				savedsofar = tosave.size() + savedsofar;
				log.info("assets added to library: ${savedsofar} " );
				tosave.clear();
			}
		}
		searcher.saveAllData(tosave, null);
		savedsofar = tosave.size() + savedsofar;
		log.info("completedlibraryadd added : ${savedsofar} " );
		fieldLibraryFolders.clear();
		
	}
	protected Data findLibrary(Searcher librarySearcher, String inFolder)
	{
		Data library = fieldLibraryFolders.get(inFolder);
		if( library == null)
		{
			SearchQuery query = librarySearcher.createSearchQuery();
			query.setAndTogether(false);
			query.addExact( "folder", inFolder );
			query.addSortBy("folderDown");
			library =	librarySearcher.searchByQuery(query);
			if( library == null)
			{
				library = NULL;
			}
			fieldLibraryFolders.put(inFolder, library);
			if( fieldLibraryFolders.size() > 20000 )
			{
				fieldLibraryFolders.clear();
			}
		}
		if(library == NULL)
		{
			return null;
		}
		return library;

	}
}
