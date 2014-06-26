package assets.model;

import org.openedit.data.Searcher
import org.openedit.Data
import org.openedit.data.Searcher
import org.openedit.entermedia.*
import org.openedit.entermedia.creator.*
import org.openedit.entermedia.edit.*
import org.openedit.entermedia.episode.*
import org.openedit.entermedia.modules.*
import org.openedit.entermedia.search.AssetSearcher
import org.openedit.xml.*

import com.openedit.entermedia.scripts.EnterMediaObject;
import com.openedit.hittracker.*
import com.openedit.page.*
import com.openedit.util.*

import conversions.*

public class AssetTypeManager extends EnterMediaObject 
{
	public void saveAssetTypes(HitTracker inAssets) 
	{
		MediaArchive mediaarchive = (MediaArchive)context.getPageValue("mediaarchive");//Search for all files looking for videos

		AssetSearcher searcher = mediaarchive.getAssetSearcher();

		Searcher typesearcher = mediaarchive.getSearcher("assettype");

		HitTracker types = typesearcher.getAllHits();
		HashMap typemap = new HashMap();
		types.each{
			String extentions = it.extensions;
			String type = it.id;
			if(extentions != null){
				String[] splits = extentions.split(" ");
				splits.each{
					typemap.put(it, type)
				}
			}
		}
		List tosave = new ArrayList();
		for (Data hit in inAssets)
		{
			Asset real = checkForEdits(typemap, hit);
			if( real == null )
			{
				real = checkLibrary(mediaarchive,hit);
			}
			else
			{
				checkLibrary(mediaarchive,real);
			}
			if(real != null)
			{
				tosave.add(real);
			}
			if(tosave.size() == 100)
			{
				saveAssets(searcher, tosave);
				tosave.clear();
			}
		}
		saveAssets(searcher, tosave);
	}
	
	public Asset checkForEdits(Map typemap, Data hit)
	{
		String fileformat = hit.fileformat;
		String currentassettype = hit.assettype;
		String assettype = typemap.get(fileformat);
		if(assettype == null)
		{
			assettype = "none";
		}
		assettype = findCorrectAssetType(hit,assettype);
		if(!assettype.equals(currentassettype))
		{
			Asset real = mediaArchive.getAssetBySourcePath(hit.sourcepath);
			real.setProperty("assettype", assettype);
			return real;
		}
		return null;
	}
	public Asset checkLibrary(MediaArchive mediaarchive, Data hit)
	{
		//Load up asset if needed to change the library?
		return null;
	}
	public Asset checkLibrary(MediaArchive mediaarchive, Asset real)
	{
		//Load up asset if needed to change the library?
		return real;
	}

	public void saveAssets(Searcher inSearcher, Collection tosave)
	{
		//Do any other checks on the asset. Add to library?
		inSearcher.saveAllData(tosave, context.getUser());
	}
	public String findCorrectAssetType(Data inAssetHit, String inSuggested)
	{
/*		String path = inAssetHit.getSourcePath().toLowercase();
		if( path.contains("/links/") )
		{
			return "links";
		}
		if( path.contains("/press ready pdf/") || path.endsWith("_pfinal.pdf") )
		{
			return "printreadyfinal";
		}
*/		
		return inSuggested;
	}
	
}