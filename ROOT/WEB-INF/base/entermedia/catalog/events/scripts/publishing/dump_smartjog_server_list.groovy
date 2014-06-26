package publishing;

import org.codehaus.groovy.runtime.InvokerHelper;


import org.openedit.Data 
import org.openedit.entermedia.MediaArchive;
import org.openedit.entermedia.smartjog.SmartJog 
import org.openedit.xml.XmlSearcher 

import com.openedit.page.Page;
import com.openedit.users.User 
import com.openedit.util.XmlUtil 
import com.smartjog.webservices.Server 

User user = context.getPageValue("user");
MediaArchive mediaArchive = context.getPageValue("mediaarchive");//Search for all files looking for videos
Page dir = mediaArchive.getPageManager().getPage("/WEB-INF/data/" + mediaArchive.getCatalogId() + "/smartjog/");
SmartJog ssc = new SmartJog(dir.getContentItem().getAbsolutePath());

/*
 * <publishlocations>
	<location id="1" type="smartjog" serverId="7383">Name</location> 
</publishlocations>
 */

XmlUtil xmlUtil = mediaArchive.getModuleManager().getBean(mediaArchive.getCatalogId(),"xmlUtil");

XmlSearcher destSearcher = mediaArchive.getSearcherManager().getSearcher(mediaArchive.getCatalogId(), "publishing/smartjogdestination");
destSearcher.deleteAll(user);

List dataList = new ArrayList();

Server[] serverArray = ssc.getAllServers();
if (serverArray != null && serverArray.length > 0)
{
	log.info("Writing out all servers.");
	for (Server server : serverArray)
	{
		if (server != null)
		{
			Data serverData = destSearcher.createNewData();
			serverData.setProperty("publishtype", "smartjog");
			serverData.setProperty("serverId", server.getServerId().toString());
			serverData.setProperty("name", xmlUtil.xmlEscape(server.getServerName()));
			dataList.add(serverData);
		}
	}
	log.info("Wrote " + dataList.size() + " servers.");
}

destSearcher.saveAllData(dataList, user);