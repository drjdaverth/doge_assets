import com.openedit.WebPageRequest 
import com.openedit.entermedia.scripts.EnterMediaObject 
import com.openedit.entermedia.scripts.ScriptLogger;
import com.openedit.page.Page 
import com.openedit.servlet.OpenEditEngine 
//import org.junit.Test 
import org.openedit.Data 
import org.openedit.entermedia.Asset 
import org.openedit.entermedia.MediaArchive 
import org.openedit.entermedia.modules.OrderModule 
import org.openedit.entermedia.orders.Order 


class Test extends EnterMediaObject
{
	public MediaArchive getMediaArchive()
	{
		return context.getPageValue("mediaarchive");
	}
	
	public void testCreateActivity() throws Exception
	{
		String appid = context.findValue("applicationid");
		WebPageRequest req = createPageRequest("/${appid}/views/activity/createneworder.html");
		
		OrderModule om = (OrderModule)getMediaArchive().getModuleManager().getModule("OrderModule");
		String catalogid = getMediaArchive().getCatalogId();
		Order order = om.createNewOrder(req);

		String status = order.get("historyuserstatus");
		if( !assertEquals(status,"newrecord") )
		{
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
	
	logs.info("<h2>testCreateActivity()</h2>")
	test.testCreateActivity();

}
finally
{
	logs.stopCapture();
}
context.putPageValue("messages",logs.getLogs());
