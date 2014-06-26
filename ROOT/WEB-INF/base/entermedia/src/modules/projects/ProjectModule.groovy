package model.projects;

import java.util.Iterator;

import org.openedit.Data;
import org.openedit.entermedia.MediaArchive;
import org.openedit.entermedia.modules.BaseMediaModule;

import com.openedit.WebPageRequest;
import com.openedit.hittracker.HitTracker;

public class ProjectModule extends BaseMediaModule
{
	
	public void loadCollections(WebPageRequest inReq) throws Exception
	{
		String catalogid = inReq.findValue("catalogid");
		ProjectManager manager = (ProjectManager)getModuleManager().getBean(catalogid,"projectManager");
		manager.loadCollections(inReq);
	}
	
	public void savedLibrary(WebPageRequest inReq)
	{
		Data saved = (Data)inReq.getPageValue("data");
		if( saved != null)
		{
			inReq.setRequestParameter("profilepreference","last_selected_library" );
			inReq.setRequestParameter("profilepreference.value", saved.getId() );
		}
			
	}
	
	
	public void addAssetToCollection(WebPageRequest inReq)
	{
		//TODO: Support multiple selections
		MediaArchive archive = getMediaArchive(inReq);
		String hitssessionid = inReq.getRequestParameter("hitssessionid");
		String libraryid = inReq.getRequestParameter("libraryid");
		
		ProjectManager manager = (ProjectManager)getModuleManager().getBean(archive.getCatalogId(),"projectManager");
		if( hitssessionid != null )
		{
			HitTracker tracker = (HitTracker)inReq.getSessionValue(hitssessionid);
			if( tracker != null )
			{
				tracker = tracker.getSelectedHitracker();
			}
			if( tracker != null && tracker.size() > 0 )
			{
				for (Iterator iterator = tracker.iterator(); iterator.hasNext();)
				{
					Data data = (Data) iterator.next();
					manager.addAssetToCollection(inReq, archive, libraryid, data.getId());
				}
				inReq.putPageValue("added" , String.valueOf( tracker.size() ) );
				return;
			}
		}
		String assetid = inReq.getRequestParameter("assetid");
		
		manager.addAssetToCollection(inReq, archive, libraryid, assetid);
		inReq.putPageValue("added" , "1" );
	
	}
	
	public void addAssetToLibrary(WebPageRequest inReq)
	{
		//TODO: Support multiple selections
		MediaArchive archive = getMediaArchive(inReq);
		String libraryid = inReq.getRequestParameter("libraryid");
		String hitssessionid = inReq.getRequestParameter("hitssessionid");
		ProjectManager manager = (ProjectManager)getModuleManager().getBean(archive.getCatalogId(),"projectManager");

		if( hitssessionid != null )
		{
			HitTracker tracker = (HitTracker)inReq.getSessionValue(hitssessionid);
			if( tracker != null )
			{
				tracker = tracker.getSelectedHitracker();
			}
			if( tracker != null && tracker.size() > 0 )
			{
				for (Iterator iterator = tracker.iterator(); iterator.hasNext();)
				{
					Data data = (Data) iterator.next();
					manager.addAssetToLibrary(inReq, archive, libraryid, data.getId());
				}
				inReq.putPageValue("added" , String.valueOf( tracker.size() ) );
				return;
			}
		}

		String assetid = inReq.getRequestParameter("assetid");
		manager.addAssetToLibrary(inReq, archive, libraryid, assetid);
		inReq.putPageValue("added" , "1" );
		
	}

	
	
}
