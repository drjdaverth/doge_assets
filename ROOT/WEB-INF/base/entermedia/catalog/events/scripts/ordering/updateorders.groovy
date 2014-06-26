package ordering;

import java.util.List;
import org.entermedia.email.PostMail 
import org.entermedia.email.TemplateWebEmail 
import org.openedit.data.Searcher 
import org.openedit.entermedia.Asset 
import org.openedit.entermedia.MediaArchive;
import com.openedit.hittracker.HitTracker 
import com.openedit.hittracker.SearchQuery 
import org.openedit.Data 
import org.openedit.entermedia.orders.Order 
import org.openedit.entermedia.publishing.Publisher 
import org.openedit.event.*;
import java.util.ArrayList;

protected Order getOrder(String inOrderId)
{
	Searcher ordersearcher = mediaarchive.getSearcherManager().getSearcher(mediaarchive.getCatalogId(), "order");
	Order order = ordersearcher.searchById(inOrderId);
	return order;
}

public void init()
{

	MediaArchive mediaArchive = (MediaArchive)context.getPageValue("mediaarchive");//Search for all files looking for videos
	
	Searcher itemsearcher = mediaArchive.getSearcherManager().getSearcher(mediaArchive.getCatalogId(), "orderitem");
	SearchQuery query = itemsearcher.createSearchQuery();
	WebEvent webevent = context.getPageValue("webevent");
	Asset asset = null;
	if( webevent != null)
	{
		String sourcepath = webevent.getSourcePath();
		 asset = mediaArchive.getAssetBySourcePath(sourcepath);
		if( asset != null)
		{
			query.addExact("assetid",asset.getId());
		}
	}
	
	query.addOrsGroup("status","converted retry");

	HitTracker tracker = itemsearcher.search(query);
	if( tracker.size() > 0)
	{
		for( Data result:new ArrayList(tracker))
		{
			Data orderitem = itemsearcher.searchById(result.getId());
			String presetid = orderitem.get("presetid");
			if( presetid == null)
			{
				//they are previewing only
				log.info("Publish not needed for null preset ");
				orderitem.setProperty("status", "complete");
				itemsearcher.saveData(orderitem,null);
				fireOrderItemCompleteEvent(orderitem.getId());
				
				continue;
			}
			String orderId = orderitem.get('orderid');
			Order order = getOrder(orderId);

			if(asset == null)
			{
				String assetid = result.get("assetid");
				asset = mediaArchive.getAsset(assetid);
			}
			
			String publishdestination = orderitem.get("publishdestination");
			if (publishdestination == null)
			{
				publishdestination = order.get('publishdestination');
			}
			if( publishdestination != null)
			{
				Data destination = mediaArchive.getSearcherManager().getData(mediaArchive.getCatalogId(), "publishdestination",publishdestination);
				try
				{
					Publisher publisher = getPublisher(mediaArchive, destination.get("publishtype"));
					
					String exportname = orderitem.get("filename");
					if( exportname == null)
					{
						Data preset = mediaArchive.getSearcherManager().getData( mediaArchive.getCatalogId(), "convertpreset", presetid);
						if( preset.get("type") != "original")
						{
							exportname = mediaArchive.asExportFileName( asset, preset);
						}
						if( exportname == null)
						{
							inputpage = mediaArchive.getOriginalDocument(asset);
							exportname = inputpage.getName();
						}
						orderitem.setProperty("filename", exportname);
					}
					
					publisher.publish(mediaArchive,order,orderitem, asset);
				
					fireOrderItemCompleteEvent(orderitem.getId());
				}
				catch( Exception ex)
				{
					log.error("Problem publishing ${asset} to ${publishdestination} ${ex}");
					//TODO: Log the details and load the order and change the status
					String counted =  orderitem.get("errorcount");
					if( counted == null)
					{
						counted = "0";
					}
					int num = Integer.parseInt( counted );
					num++;
					if( num > 5)
					{
						orderitem.setProperty('status', 'error');
					}
					else
					{
						orderitem.setProperty('status', 'retry');
					}
					orderitem.setProperty('errorcount', String.valueOf(num));
					orderitem.setProperty("errordetails", "${destination} publish failed ${ex}");
					itemsearcher.saveData(orderitem, context.getUser());
					//log.error(ex);
				}
			}
			else
			{
				orderitem.setProperty('status', 'complete');
				itemsearcher.saveData(orderitem, context.getUser());
				fireOrderItemCompleteEvent(orderitem.getId());
			}
		}
	}
}

protected fireOrderItemCompleteEvent(String inOrderItemId)
{
	WebEvent event = new WebEvent();
	event.setSearchType("orderitem");
	event.setProperty("orderitemid", inOrderItemId);
	event.setOperation("ordering/orderitemfinished");
	event.setUser(context.getUser());
	event.setCatalogId(mediaarchive.getCatalogId());
	mediaarchive.getMediaEventHandler().eventFired(event);

}

protected Publisher getPublisher(MediaArchive inArchive, String inType)
{
	GroovyClassLoader loader = engine.getGroovyClassLoader();
	Class groovyClass = loader.loadClass("publishing.publishers.${inType}publisher");
	Publisher publisher = (Publisher) groovyClass.newInstance();
	return publisher;
}

init();