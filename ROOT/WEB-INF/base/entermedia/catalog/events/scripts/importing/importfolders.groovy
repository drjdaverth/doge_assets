package importing;

import org.openedit.entermedia.MediaArchive 
import org.openedit.*;

import com.openedit.hittracker.*;
import com.openedit.page.Page
import com.openedit.util.Exec
import com.openedit.util.ExecResult

import org.openedit.entermedia.creator.*;

import java.text.Normalizer.Form;
import java.util.Iterator;

import org.openedit.entermedia.MediaArchive;

import org.openedit.entermedia.scanner.HotFolderManager
import org.openedit.repository.ContentItem;

public void init()
{
	MediaArchive archive = context.getPageValue("mediaarchive");
	
	HotFolderManager manager = (HotFolderManager)moduleManager.getBean("hotFolderManager");

	Collection hits = manager.loadFolders( archive.getCatalogId() );
	for(Iterator iterator = hits.iterator(); iterator.hasNext();)
	{
		Data folder = (Data)iterator.next();
		String base = "/WEB-INF/data/" + archive.getCatalogId() + "/originals";
		String name = folder.get("subfolder");
		String path = base + "/" + name ;
		
		//look for git folders?
		try
		{
			pullGit(path,1);
			manager.importHotFolder(archive,folder);
		}
		catch( Exception ex)
		{
			log.error("Could not process folder ${path}",ex);
		}
	}
	
	/*
	AssetImporter importer = (AssetImporter)moduleManager.getBean("assetImporter");
	importer.setExcludeFolders("Fonts,Links");
	//importer.setIncludeFiles("psd,tif,pdf,eps");
	importer.setUseFolders(false);
	
	String assetRoot = "/WEB-INF/data/" + archive.getCatalogId() + "/originals/";
		
	List created = importer.processOn(assetRoot, assetRoot, archive, context.getUser());
	log.info("created images " + created.size() );
	*/
	
}

void pullGit(String path, int deep)
{
	ContentItem page = pageManager.getRepository().getStub(path + "/.git");
	if( page.exists() )
	{
		Exec exec = moduleManager.getBean("exec");
		List commands = new ArrayList();
		ContentItem root = pageManager.getRepository().get(path);		
//		commands.add("--work-tree=" + root.getAbsolutePath());
//		commands.add("--git-dir=" + page.getAbsolutePath());

		File from = new File( root.getAbsolutePath() );
		commands.add("pull");
		ExecResult result = exec.runExec("git",commands, from);
		if( result.isRunOk() )
		{
			log.info("pulled from git "  + root.getAbsolutePath() );
		}
		else
		{
			log.error("Could not pull from "  + root.getAbsolutePath() );
		}
	}
	else if( deep < 4 )
	{
		List paths = pageManager.getChildrenPaths(path);
		if( paths != null )
		{
			deep++;
			for(String child: paths)
			{
				ContentItem childpage = pageManager.getRepository().getStub(child);
				if( childpage.isFolder() )
				{
					pullGit(child,deep);
				}
			}	
		}
	}
}

init();
