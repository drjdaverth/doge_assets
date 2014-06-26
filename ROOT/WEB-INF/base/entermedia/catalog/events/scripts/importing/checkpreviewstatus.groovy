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


//I do not need this at all since the assets are already set based on the import script or the conversion script

public void checkPreviewStatus() throws Exception
{
	 
	MediaArchive mediaarchive = (MediaArchive)context.getPageValue("mediaarchive");//Search for all files looking for videos

	Searcher assetsearcher = mediaarchive.getAssetSearcher();
	SearchQuery q = assetsearcher.createSearchQuery();
	String ids = context.getRequestParameter("assetids");
	if( ids == null)
	{
		//Do a search for importstatus of "added" -> "converted"
		q.addOrsGroup("importstatus","imported reimported complete");	//things are queued but maybe not complete	
		//why are these not marked as complete or error?
		q.addNot("previewstatus","2");
		q.addNot("previewstatus","3");
		q.addNot("previewstatus","generated");
	}
	else
	{
		String assetids = ids.replace(","," ");
		q.addOrsGroup( "id", assetids );
	}
	
	Collection assets = assetsearcher.search(q);

	Searcher tasksearcher = mediaarchive.getSearcherManager().getSearcher (mediaarchive.getCatalogId(), "conversiontask");
	
	log.debug("Found ${assets.size()} assets");
	assets.each
	{
		//Are the conversions complete?
		/*<property id="new">New</property>
		<property id="missinginput">Input Missing</property>
		<property id="submitted">Processing</property>
		<property id="complete">Complete</property>
		<property id="retry">Retry</property>
		<property id="error">Error</property>
		*/
		Data data = it;
		String assetid = data.getId();
		SearchQuery query = tasksearcher.createSearchQuery();
		//query.addOrsGroup("status", "complete");		
		query.addMatches("assetid", assetid);
		HitTracker newtasks = tasksearcher.search(query);
	
		//Make sure they exists, and if they do that that are all complete. if error mark as error
		boolean markerror = false;
		boolean markcomplete = true;
		if( newtasks.size()  > 0)
		{
			for( Data task in newtasks)
			{
				String status = task.get("status");
				if( status == null)
				{
					markcomplete = false;
					break;
				} 
				if( status.equals("error") )
				{
					markerror = true;
					break;
				}
				else if( !status.equals("complete") )
				{
					markcomplete = false;
					break;
				}
			}
			if( markerror || markcomplete)
			{
				Asset asset = mediaarchive.getAssetBySourcePath(data.getSourcePath());
				if( asset != null)
				{
					if( markerror )
					{
						asset.setProperty("previewstatus","3");
					}
					else
					{
						asset.setProperty("previewstatus","2");
					}
					mediaarchive.saveAsset( asset, user );
				}
			}
		}
	}
	
}
checkPreviewStatus();
