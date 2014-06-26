package publishing.publishers;

import com.openedit.OpenEditException;
import com.openedit.page.Page 

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
//import org.entermedia.aspera.AsperaManager 
//import org.entermedia.aspera.AsperaRepository 
import org.openedit.Data 
import org.openedit.entermedia.Asset 
import org.openedit.entermedia.MediaArchive 
import org.openedit.entermedia.modules.OrderModule;
import org.openedit.entermedia.publishing.*;
import org.openedit.repository.filesystem.StringItem;
import org.openedit.data.*;

public class asperapublisher extends basepublisher implements Publisher
{
	private static final Log log = LogFactory.getLog(asperapublisher.class);

	public PublishResult publish(MediaArchive mediaArchive,Asset inAsset, Data inPublishRequest,  Data inDestination, Data inPreset)
	{
		//log.info("Publish asset to aspera ${asset} for preset: ${presetid} on server: ${publishdestination}" );
		PublishResult result = new PublishResult();
		
		Page inputpage = findInputPage(mediaArchive,inAsset,inPreset);
		String exportname = inPublishRequest.get("exportname");
		
		if( !exportname.startsWith("/"))
		{
			exportname ="/" + exportname;
		}
		StringItem item = new StringItem();
		item.setPath(exportname); //Aspera repo is mounted in a sub folder already. So we just need to append the name
		item.setAbsolutePath(inputpage.getContentItem().getAbsolutePath());

//		AsperaManager manager = (AsperaManager)mediaArchive.getModuleManager().getBean("asperaManager");
//
//		AsperaRepository repo = manager.loadRepository(mediaArchive.getCatalogId(), inDestination );
//		repo.put(item); //copy the file
		log.info("publishished  ${inAsset.sourcepath} to aspera");

		result.setComplete(true);
		return result;
	}
	
	
}

