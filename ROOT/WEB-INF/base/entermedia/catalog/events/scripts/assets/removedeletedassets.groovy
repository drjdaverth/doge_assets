package assets
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

import com.openedit.hittracker.*
import com.openedit.page.*
import com.openedit.util.*

import conversions.*


public void checkforTasks()
{
	mediaarchive = (MediaArchive)context.getPageValue("mediaarchive");//Search for all files looking for videos
	
	AssetSearcher searcher = mediaarchive.getAssetSearcher();
	//recordmodificationdate
	//editstatus:7
	
	log.info("checking for deleted assets older than 30 days");
	Calendar now = Calendar.getInstance();
	now.add(Calendar.MONTH, -1);
	SearchQuery query = searcher.createSearchQuery();
	
	query.addMatches("editstatus", "7");
	query.addBefore("recordmodificationdate", now.getTime())
	
	HitTracker newitems = searcher.search(query);
	log.info("Searching for ${query} found ${newitems.size()}");
	
	for (Data hit in newitems)
	{	
		Asset realitem = searcher.searchById(hit.getId());
		
		
		if (realitem != null)
		{
			mediaarchive.removeGeneratedImages(realitem);
			//mediaarchive.removeOriginals(realitem);
			searcher.delete(realitem, context.getUser());
		}
		else
		{
			log.info("Can't find task object with id '${hit.getId()}'. Index out of date?")
		}
	}
}
checkforTasks();