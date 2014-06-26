import com.openedit.page.Page 
import org.openedit.data.Searcher 
import org.openedit.entermedia.Asset 
import org.openedit.entermedia.MediaArchive 
import org.openedit.*;

import com.openedit.WebPageRequest;
import com.openedit.hittracker.*;
import org.openedit.entermedia.creator.*;

public boolean  createOutput(MediaCreator creator, incontext, mediaArchive, hit, forceConvert)
{
	String sourcePath = hit.get("sourcepath");
	ConvertInstructions inStructions = creator.createInstructions(incontext, mediaArchive, 'jpg', sourcePath);
	inStructions.setForce(forceConvert);
	Page output = pageManager.getPage(inStructions.getOutputPath());
	
	if( forceConvert || !output.exists() || output.getContentItem().getLength() == 0  )
	{
			try
			{
				output = creator.createOutput(mediaArchive,inStructions);//archive.getCreatorManager().createOutput( inStructions);
				if ( output != null && output.exists() )
				{
					return true;
				}
			}
			catch(Exception e)
			{
				log.error("Error creating ouput " + sourcePath + " " +  e);
			}
	}
	return output != null && output.exists();
}

public void init()
{
	MediaArchive mediaArchive = context.getPageValue("mediaarchive");//Search for all files looking for videos
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
		q.addMatches("id", "*");
	}
	
	HitTracker assets = targetsearcher.search(q);
	assets.setHitsName("makeimageshits");
	context.setRequestParameter("hitssessionid", assets.getSessionId());
	context.setSession(null);
	context.putSessionValue(assets.getSessionId(), assets);

	MediaCreator creator = mediaArchive.getCreatorManager().getMediaCreatorByOutputFormat("jpg");
	
	//TODO: Use preset settings instead of paths
	WebPageRequest previewcontext = makeContext( mediaArchive.getCatalogHome() + "/downloads/preview/cache/preview.jpg", context);
	WebPageRequest mediumpluscontext = makeContext(mediaArchive.getCatalogHome() + "/downloads/preview/mediumplus/medium.jpg", context);
	WebPageRequest mediumcontext = makeContext(mediaArchive.getCatalogHome() + "/downloads/preview/medium/medium.jpg", context);
	WebPageRequest thumbcontext = makeContext(mediaArchive.getCatalogHome() + "/downloads/preview/thumb/thumb.jpg", context);
	WebPageRequest thumbscompactcontext = makeContext(mediaArchive.getCatalogHome() + "/downloads/preview/thumbsmall/thumb.jpg", context);
	
	int count = 0;
	int made = 0;
	
	int numPages = assets.getTotalPages();
	log.info("Pages: " + numPages);
	List assetsToSave = new ArrayList();
	for(int i = 0; i < numPages; i++)
	{	
		context.setRequestParameter("page", String.valueOf(i + 1));
		HitTracker hits = targetsearcher.loadPageOfSearch(context);
		//log.info("New page: " + i );
		
		List page = hits.getPageOfHits();
		for(Iterator iterator = page.iterator(); iterator.hasNext();)
		{
		 	Data hit =  iterator.next();
			count++;
			//log.info("Found " + hit.getId());
			String status = hit.get("previewstatus");
			boolean created = createOutput(creator, previewcontext,mediaArchive,hit, false);
			boolean force = false;
			
			if( created )
			{
				if(status.equals("1"))
				{
					force = true;	
				} 
				createOutput(creator, mediumpluscontext,mediaArchive,hit, force);
				createOutput(creator, mediumcontext,mediaArchive,hit, force);
				createOutput(creator, thumbcontext,mediaArchive,hit, force);
				createOutput(creator, thumbscompactcontext,mediaArchive,hit, force);
		
				if( status != "2")
				{
					Asset asset = mediaArchive.getAssetBySourcePath(hit.get("sourcepath"));
					if( asset != null)
					{
						asset.setProperty("previewstatus", "2");
						assetsToSave.add(asset)
						made++;
						if(assetsToSave.size() == 100)
						{
							mediaArchive.saveAssets assetsToSave;
							assetsToSave.clear();
						}
					}
					else
					{
						log.info("Null asset found " + hit.get("sourcepath") );
					}
				}
			}
		}	
	}
	mediaArchive.saveAssets assetsToSave;
	log.info("checked " + count + " records. updated " + made + " images" );
	
}

def makeContext(path, WebPageRequest context)
{
	Page img = pageManager.getPage( path);
	return context.copy(img);
}


init();
