import org.openedit.entermedia.modules.*;
import org.openedit.entermedia.edit.*;
import org.openedit.*;
import com.openedit.page.*;
import org.openedit.entermedia.*;
import org.openedit.data.Searcher;
import com.openedit.hittracker.*;
import org.openedit.entermedia.creator.*


public void init()
{
	mediaArchive = context.getPageValue("mediaarchive");//Search for all files looking for videos
	Searcher targetsearcher = mediaArchive.getAssetSearcher();
	SearchQuery q = targetsearcher.createSearchQuery();
	String ids = context.getRequestParameter("assetids");
	if( ids != null)
	{
		q.setAndTogether(false);
		String[] assetids = ids.split(",");
		for (int i = 0; i < assetids.length; i++)
		{
			q.addMatches("id",assetids[i]);
		}
	}
	else
	{
		q.addMatches("category", "index");
	}

	HitTracker assets = targetsearcher.search(q);
	assets.setHitsName("hits");
	context.setRequestParameter("hitssessionid", assets.getSessionId());
	context.putSessionValue(assets.getSessionId(), assets);

	String outputtype = context.findValue("outputtype");
	//String outputtype = "mp4";
	//We use the output extension so that we don't have look up the original input file to find the actual type
	MediaCreator creator = mediaArchive.getCreatorManager().getMediaCreatorByOutputFormat(outputtype);
	int count = 0;
	
	//for (Iterator iterator = hits.iterator(); iterator.hasNext();) 
	int numPages = assets.getTotalPages();
	for(int i = 0; i < numPages; i++)
	{
		context.setRequestParameter("page", String.valueOf(i + 1));
		HitTracker hits = targetsearcher.loadPageOfSearch(context);
		List page = hits.getPageOfHits();
		for(Iterator iterator = page.iterator(); iterator.hasNext();)
		{
		 	hit =  iterator.next();
			String sourcePath = hit.get("sourcepath");
			ConvertInstructions inStructions = creator.createInstructions(context, mediaArchive, outputtype, sourcePath);
			
			Page output = pageManager.getPage(inStructions.getOutputPath());
			
			if( !output.exists() || output.getContentItem().getLength() == 0 )
			{
				try
				{
					output = creator.createOutput(mediaArchive,inStructions);//archive.getCreatorManager().createOutput( inStructions);
					if ( output != null && output.exists() ) 
					{
						count++;
					}
				}
				catch(Exception e)
				{
					log.info("Input file not found for sourcepath " + sourcePath);
				}
			}
		}
	}
	log.info("created " + count + " previews");
	
}

init();
