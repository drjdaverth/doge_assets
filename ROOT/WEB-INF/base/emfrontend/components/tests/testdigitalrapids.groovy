import com.openedit.WebPageRequest 
import com.openedit.entermedia.scripts.EnterMediaObject 
import com.openedit.entermedia.scripts.ScriptLogger;
import com.openedit.page.Page 
import com.openedit.servlet.OpenEditEngine 
//import org.junit.Test 
import org.openedit.Data 
import org.openedit.data.Searcher;
import org.openedit.entermedia.Asset 
import org.openedit.entermedia.MediaArchive 
import org.openedit.entermedia.modules.OrderModule 
import org.openedit.entermedia.orders.Order 
import org.openedit.util.DateStorageUtil;


class Test extends EnterMediaObject
{
	public MediaArchive getMediaArchive()
	{
		return context.getPageValue("mediaarchive");
	}
	
	public void testCreateDigitalRapids() throws Exception
	{
		String appid = context.findValue("applicationid");
		String catalogid = context.findValue("catalogid");
		
		Asset asset = getMediaArchive().getAsset(context.findValue("testassetid"));
		Searcher tasksearcher = getMediaArchive().getSearcherManager().getSearcher(getMediaArchive().getCatalogId(), "conversiontask");
		Data task = tasksearcher.createNewData();
		task.setSourcePath(asset.getSourcePath());
		task.setProperty("assetid", asset.getId());
		task.setProperty("status", "new");
		task.setProperty("submitted", DateStorageUtil.getStorageUtil().formatForStorage(new Date()));
		task.setProperty("presetid", "digitalrapidspreview");

		tasksearcher.saveData(task,null);
		
		Page proxy = getPageManager().getPage("/WEB-INF/data/" + catalogid + "/generated/" + asset.getSourcePath() + "/video.mp4");
		getPageManager().removePage(proxy);
				
		getMediaArchive().fireMediaEvent("conversions/runconversions", context.getUser(), asset);

		
		Thread.sleep(15000);
		
		if( !assertTrue(proxy.exists()) )
		{
			log.info("${proxy} does not exist");
			return;
		}
		
		log.info("test is green");
	}
}
logs = new ScriptLogger();
logs.startCapture();
try
{
	Test test = new Test();
	test.setLog(logs);
	test.setContext(context);
	test.setModuleManager(moduleManager);
	test.setPageManager(pageManager);
	
	logs.info("<h2>testCreateDigitalRapids()</h2>")
	test.testCreateDigitalRapids();

}
finally
{
	logs.stopCapture();
}
context.putPageValue("messages",logs.getLogs());
