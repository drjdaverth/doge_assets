package conversions;

import java.util.concurrent.ExecutorService

import org.entermedia.locks.Lock
import org.openedit.Data
import org.openedit.data.Searcher
import org.openedit.entermedia.*
import org.openedit.entermedia.creator.*
import org.openedit.entermedia.edit.*
import org.openedit.entermedia.modules.*
import org.openedit.xml.*

import com.openedit.*
import com.openedit.entermedia.scripts.ScriptLogger
import com.openedit.hittracker.*
import com.openedit.page.*
import com.openedit.users.User
import com.openedit.util.*

class CompositeConvertRunner implements Runnable
{
	String fieldSourcePath;
	MediaArchive fieldMediaArchive;
	List runners = new ArrayList();
	User user;
	ScriptLogger log;
	
	public CompositeConvertRunner(MediaArchive archive,String sourcepath)
	{
		fieldMediaArchive = archive;
		fieldSourcePath = sourcepath;
	}
	
	public void run()
	{
		Lock lock = fieldMediaArchive.lockAssetIfPossible(fieldSourcePath, user);
		
		if( lock == null)
		{
			log.info("asset already being processed ${fieldSourcePath}");
			return;
		}
		try
		{
			for( Runnable runner: runners )
			{
				runner.run();
			}
			fieldMediaArchive.updateAssetConvertStatus(fieldSourcePath);
		}
		finally
		{
			fieldMediaArchive.releaseLock(lock);
		}
	
		
	}

	
	public void add(Runnable runner )
	{
		runners.add(runner);
	}
}

class ConvertRunner implements Runnable
{
	MediaArchive mediaarchive;
	Searcher tasksearcher;
	Searcher presetsearcher;
	Searcher itemsearcher;
	Data hit;
	ScriptLogger log;
	User user;
	ModuleManager moduleManager;
	ConvertResult result = null;
	
	public void run()
	{
		try
		{
			convert();Runnable
		}
		catch (Throwable ex )
		{
			log.error(ex);
		}
	}
	public void convert()
	{
		Data realtask = tasksearcher.searchById(hit.getId());
		//log.info("should be ${hit.status} but was ${realtask.status}");
		
		if (realtask != null)
		{
			String presetid = hit.get("presetid");
			log.debug("starting preset ${presetid}");
			Data preset = presetsearcher.searchById(presetid);
			if(preset != null)
			{
				try
				{
					String sourcepath = hit.get("sourcepath");
					result = doConversion(mediaarchive, realtask, preset,sourcepath);
				}
				catch(Throwable e)
				{
					result = new ConvertResult();
					result.setOk(false);
					result.setError(e.toString());
					log.error("Conversion Failed" + e);
				}
				
				if(result != null)
				{
					if(result.isOk())
					{
						if(result.isComplete())
						{
							realtask.setProperty("status", "complete");
							String itemid = realtask.get("itemid")
							if(itemid != null)
							{
								//The item should have a pointer to the conversion, not the other way around
								Data item = itemsearcher.searchById(itemid);
								item.setProperty("status", "converted");
								itemsearcher.saveData(item, null);
							}
							realtask.setProperty("externalid", result.get("externalid"));
							Asset asset = mediaarchive.getAssetBySourcePath(hit.get("sourcepath"));
							
							mediaarchive.fireMediaEvent("conversions/conversioncomplete",user,asset);
						}
						else
						{
							realtask.setProperty("status", "submitted");
							realtask.setProperty("externalid", result.get("externalid"));
						}
					}
					else if ( result.isError() )
					{
						realtask.setProperty('status', 'error');
						realtask.setProperty("errordetails", result.getError() );
						
						//TODO: Remove this one day
						String itemid = realtask.get("itemid")
						if(itemid != null)
						{
							Data item = itemsearcher.searchById(itemid);
							item.setProperty("status", "error");
							item.setProperty("errordetails", result.getError() );
							itemsearcher.saveData(item, null);
						}
						//	conversionfailed  conversiontask assetsourcepath, params[id=102], admin
						Map params = new HashMap();
						params.put("taskid",realtask.getId());
						//String operation, String inMetadataType, String inSourcePath, Map inParams, User inUser)
						mediaarchive.fireMediaEvent("conversions/conversionerror","conversiontask", realtask.getSourcePath(), params, user);
						
					}
					else
					{
						String sourcepath = hit.get("sourcepath");
						log.debug("conversion had no error and will try again later for ${sourcepath}");
						realtask.setProperty('status', 'missinginput');
					}
					tasksearcher.saveData(realtask, user);
				}
			}
			else
			{
				log.info("Can't run conversion for task '${realtask.getId()}': Invalid presetid ${presetid}");
			}
		}
		else
		{
			log.info("Can't find task object with id '${hit.getId()}' '${hit.getSourcePath()}'. Index missing data?")
		}
	}
	
	protected ConvertResult doConversion(MediaArchive inArchive, Data inTask, Data inPreset, String inSourcepath)
	{
	String status = inTask.get("status");
	
	String type = inPreset.get("type"); //rhozet, ffmpeg, etc
	MediaCreator creator = getMediaCreator(inArchive, type);
	log.debug("Converting with type: ${type} using ${creator.class} with status: ${status}");
	
	if (creator != null)
	{
		Map props = new HashMap();
		
		String guid = inPreset.guid;
		if( guid != null)
		{
			Searcher presetdatasearcher = inArchive.getSearcherManager().getSearcher(inArchive.getCatalogId(), "presetdata" );
			Data presetdata = presetdatasearcher.searchById(guid);
			//copy over the preset properties..
			props.put("guid", guid); //needed?
			props.put("presetdataid", guid); //needed?
			if( presetdata != null && presetdata.getProperties() != null)
			{
				props.putAll(presetdata.getProperties());
			}
		}
		String pagenumber = inTask.get("pagenumber");
		if( pagenumber != null )
		{
			props.put("pagenum",pagenumber);
		}
		
		ConvertInstructions inStructions = creator.createInstructions(props,inArchive,inPreset.get("extension"),inSourcepath);
		
		//TODO: Copy the task properties into the props so that crop stuff can be handled in the createInstructions
		if(Boolean.parseBoolean(inTask.get("crop")))
		{
//			log.info("HERE!!!");
			inStructions.setCrop(true);
			inStructions.setProperty("x1", inTask.get("x1"));
			inStructions.setProperty("y1", inTask.get("y1"));
			inStructions.setProperty("cropwidth", inTask.get("cropwidth"));
			inStructions.setProperty("cropheight", inTask.get("cropheight"));
			if(inStructions.getProperty("prefwidth") == null){
				inStructions.setProperty("prefwidth", inTask.get("cropwidth"));
			}
			if(inStructions.getProperty("prefheight") == null){
				inStructions.setProperty("prefheight", inTask.get("cropheight"));
			}
			//inStructions.setProperty("useinput", "cropinput");//hard-coded a specific image size (large)
			inStructions.setProperty("useoriginalasinput", "true");//hard-coded a specific image size (large)
			
			inStructions.setProperty("gravity", "default");//hard-coded a specific image size (large)
			inStructions.setProperty("croplast", "true");//hard-coded a specific image size (large)
			
			if(Boolean.parseBoolean(inTask.get("force"))){
				inStructions.setForce(true);
			}
		}
		
		//inStructions.setOutputExtension(inPreset.get("extension"));
		//log.info( inStructions.getProperty("guid") );
		Asset asset = inArchive.getAssetBySourcePath(inSourcepath);
		if(asset == null)
		{
			throw new OpenEditException("Asset could not be loaded ${inSourcepath} marking as error"); 
		}
		if( asset.get("editstatus") == "7") 
		{
			throw new OpenEditException("Could not run conversions on deleted assets ${inSourcepath}");
		}
		inStructions.setAssetSourcePath(asset.getSourcePath());
		String extension = PathUtilities.extractPageType(inPreset.get("outputfile") );
		inStructions.setOutputExtension(extension);

		//new submitted retry missinginput
		if("new".equals(status) || "submitted".equals(status) || "retry".equals(status)  || "missinginput".equals(status))
		{
			//String outputpage = "/WEB-INF/data/${inArchive.catalogId}/generated/${asset.sourcepath}/${inPreset.outputfile}";
			String outputpage = creator.populateOutputPath(inArchive, inStructions, inPreset);
			Page output = inArchive.getPageManager().getPage(outputpage);
			log.debug("Running Media type: ${type} on asset ${asset.getSourcePath()}" );
			result = creator.convert(inArchive, asset, output, inStructions);
		}
		else if("submitted".equals(status))
		{
			result = creator.updateStatus(inArchive, inTask, asset, inStructions);
		}
		else
		{
			log.info("${inTask.getId()} task id with ${status} status not submitted, new, missinginput or retry, is index out of date? ");
		}
	}
	else
	{
		log.info("Can't find media creator for type '${type}'");
	}
	return result;
  }
//TODO: Cache in map
private MediaCreator getMediaCreator(MediaArchive inArchive, String inType)
{
	MediaCreator creator = moduleManager.getBean(inType + "Creator");
	return creator;
 }
} //End Runnable methods

protected ConvertRunner createRunnable(MediaArchive mediaarchive, Searcher tasksearcher, Searcher presetsearcher, Searcher itemsearcher, Data hit)
{
	   ConvertRunner runner = new ConvertRunner();
	   runner.mediaarchive = mediaarchive;
	   runner.tasksearcher = tasksearcher;
	   runner.presetsearcher = presetsearcher;
	   runner.itemsearcher = itemsearcher;
	   runner.hit = hit;
	   runner.log = log;
	   runner.user = user; //if you get errors here make sure they did not delete the admin user
	   runner.moduleManager= moduleManager;
	   return runner;
}
   
		
public void checkforTasks()
{
	mediaarchive = (MediaArchive)context.getPageValue("mediaarchive");//Search for all files looking for videos
	
	Searcher tasksearcher = mediaarchive.getSearcherManager().getSearcher (mediaarchive.getCatalogId(), "conversiontask");
	Searcher itemsearcher = mediaarchive.getSearcherManager().getSearcher (mediaarchive.getCatalogId(), "orderitem");
	Searcher presetsearcher = mediaarchive.getSearcherManager().getSearcher (mediaarchive.getCatalogId(), "convertpreset");
	
	
	SearchQuery query = tasksearcher.createSearchQuery();
	query.addOrsGroup("status", "new submitted retry missinginput");
	query.addSortBy("assetid");
	query.addSortBy("ordering");
	
	String assetids = context.getRequestParameter("assetids");
	if(assetids != null)
	{
		assetids = assetids.replace(","," ");
		query.addOrsGroup( "id", assetids );
	}
	else
	{	
		String assetid = context.getRequestParameter("assetid");
		if(assetid != null)
		{
			query.addMatches("assetid", assetid);
		}
	}
	context.setRequestParameter("assetid", (String)null); //so we clear it out for next time. needed?
	HitTracker newtasks = tasksearcher.search(query);
	newtasks.setHitsPerPage(20000);  //This is a problem. Since the data is being edited while we change pages we skip every other page. Only do one page at a time
	log.info("processing ${newtasks.size()} conversions ${newtasks.getHitsPerPage()} at a time");
	
	List runners = new ArrayList();
	
	if( newtasks.size() == 1 )
	{
		Data task = (Data)newtasks.first();
		ConvertRunner runner = createRunnable(mediaarchive,tasksearcher,presetsearcher, itemsearcher, task);
		runners.add(runner);
		runner.run();
		mediaarchive.updateAssetConvertStatus(task.getSourcePath());
	}
	else
	{
		ExecutorManager executorManager = (ExecutorManager)moduleManager.getBean("executorManager");
		ExecutorService  executor = executorManager.createExecutor();
		CompositeConvertRunner byassetid = null;
		String lastassetid = null;
		for(Data hit: newtasks.getPageOfHits() )
		{
			ConvertRunner runner = createRunnable(mediaarchive,tasksearcher,presetsearcher, itemsearcher, hit );
			runners.add(runner);
			String id = hit.get("assetid"); //Since each converter locks the asset we want to group these into one sublist
			if( id == null )
			{
//				throw new OpenEditException("asset id was null on " + hit );
				Data missingdata = tasksearcher.searchById(hit.getId())
				missingdata.setProperty("status", "error");
				missingdata.setProperty("errordetails", "asset id is null");
				tasksearcher.saveData(missingdata, null);
				continue;
			}
			if( id != lastassetid )
			{
				if( byassetid != null )
				{
					executor.execute(byassetid);
				}
				lastassetid = hit.get("assetid");
				byassetid = new CompositeConvertRunner(mediaarchive,hit.getSourcePath() );
				byassetid.log = log;
				byassetid.user = user;
			}
			byassetid.add(runner);
		}
		if( byassetid != null )
		{
			executor.execute(byassetid);
		}
		executorManager.waitForIt(executor);
	}
	
	if( newtasks.size() > 0 )
	{
		for(ConvertRunner runner: runners)
		{
			if( runner.result != null && runner.result.isComplete() )
			{
				mediaarchive.fireSharedMediaEvent("conversions/conversionscomplete");
				mediaarchive.fireSharedMediaEvent("conversions/runconversions");
				break;
			}
		}
	}
	log.info("Completed ${newtasks.size()} conversions");
	
}


checkforTasks();

