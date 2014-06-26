import com.openedit.page.Page
import org.openedit.data.Searcher
import org.openedit.entermedia.Asset
import org.openedit.entermedia.MediaArchive
import org.openedit.*;

import com.openedit.WebPageRequest;
import com.openedit.hittracker.*;

public void init()
{
		MediaArchive archive = context.getPageValue("mediaarchive");//Search for all files looking for videos
		Searcher tasksearcher = archive.getSearcherManager().getSearcher(archive.getCatalogId(), "conversiontask");
		
		log.info("clear errors");
		
		SearchQuery query = tasksearcher.createSearchQuery();
		query.addMatches("status", "error");
		
		HitTracker tasks = tasksearcher.search(query);
		List all = new ArrayList(tasks);
		for (Data hit in all)
		{
			Data realtask = tasksearcher.searchById(hit.getId());
			realtask.setProperty("status","new");
			tasksearcher.saveData(realtask,null);
		}
}

init();