import org.openedit.data.Searcher
import org.openedit.entermedia.Asset
import org.openedit.entermedia.MediaArchive
import org.openedit.entermedia.scanner.PresetCreator;
import org.openedit.*;
import com.openedit.hittracker.*;

public void init()
{
		MediaArchive mediaarchive = context.getPageValue("mediaarchive");//Search for all files looking for videos
		Searcher assetsearcher = mediaarchive.getAssetSearcher();
		Searcher tasksearcher = mediaarchive.getSearcherManager().getSearcher (mediaarchive.getCatalogId(), "conversiontask");
		PresetCreator presets = new PresetCreator();

		//HitTracker assets = assetsearcher.getAllHits();
//		SearchQuery q = assetsearcher.createSearchQuery();
//		q.addOrsGroup("importstatus", "imported reimported");
		SearchQuery q = assetsearcher.createSearchQuery().append("category", "index");
		q.addNot("editstatus","7");
		q.addSortBy("id");
		HitTracker assets =  assetsearcher.search(q);
		assets.setHitsPerPage(1000);
		
		log.info("Processing ${assets.size()}" + q	);
		
		
		long added = 0;
		long checked  = 0;
		long logcount  = 0;
		long completed = 0;
		List tosave = new ArrayList();
		for (Data hit in assets)
		{
			checked++;
			logcount++;
			
			Asset asset = mediaarchive.getAssetBySourcePath(hit.getSourcePath());
			if( asset == null )
			{
				log.info("Missing" + hit.getSourcePath() );
				continue; //Bad index
			}

			int more = presets.createMissingOnImport(mediaarchive, tasksearcher, asset);
			added = added + more;
			if( logcount == 1000 )
			{
				logcount = 0;
				log.info("Checked ${checked} ${added} ${more} "  + asset.get("importstatus"));
			}
			if( more == 0 && !"converting".equals(asset.get("previewstatus") ) )
			{
				//log.info("complete ${asset}");
				asset.setProperty("previewstatus","converting");
				//mediaarchive.saveAsset(asset, null);
				tosave.add(asset);
				completed++;
			}
			if( tosave.size() == 500 )
			{
				mediaarchive.saveAssets(tosave);
				tosave.clear();
				log.info("checked ${checked} assets. ${added} tasks queued, ${completed} completed. please run event again since index has changed order" );
				
			}
		}
		mediaarchive.saveAssets(tosave);
		log.info("checked ${checked} assets. ${added} tasks queued , ${completed} completed." );
}

init();