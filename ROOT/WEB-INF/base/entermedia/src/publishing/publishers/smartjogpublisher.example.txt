package publishing.publishers;

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.openedit.Data
import org.openedit.entermedia.Asset
import org.openedit.entermedia.MediaArchive
import org.openedit.entermedia.publishing.PublishResult
import org.openedit.entermedia.publishing.Publisher
import org.openedit.entermedia.smartjog.SmartJog
import org.openedit.entermedia.smartjog.Status

import com.openedit.OpenEditException
import com.openedit.page.Page
import com.smartjog.webservices.Delivery
import com.smartjog.webservices.ServerFile
 

public class smartjogpublisher extends basepublisher implements Publisher
{
	private static final Log log = LogFactory.getLog(smartjogpublisher.class);
	
	public PublishResult publish(MediaArchive mediaArchive,Asset asset, Data inPublishRequest,  Data destination, Data inPreset)
	{
		PublishResult result = new PublishResult();

		Page inputpage = findInputPage(mediaArchive,asset,inPreset);
		String exportname = inPublishRequest.get("exportname");
		String mountPath = "/WEB-INF/publish/smartjog";
		if( !exportname.startsWith("/"))
		{
			mountPath = mountPath + "/";
		}
		String fullPath = mountPath + exportname;
	

		String serverId = destination.get("server");
		if (serverId == null)
		{
			result.setErrorMessage("SMART JOG ERROR: Must specify a server for delivery.");
			return result;
		}
		if( inPublishRequest.get("status") == "pending")
		{
			updateStatus(mediaArchive,exportname, new Integer(Integer.parseInt(serverId)), inPublishRequest,result );			
		}
		else
		{
			Page publishPage = mediaArchive.getPageManager().getPage(fullPath);
			if( !publishPage.exists() )
			{
				mediaArchive.getPageManager().copyPage(inputpage, publishPage); //put the file on the ftp server for deliveryx
			}
			
			startSmartJogDelivery(mediaArchive,exportname,inPublishRequest, new Integer(Integer.parseInt(serverId)), result);
		}

		return result;
	}
	public void updateStatus(MediaArchive mediaArchive, String inFilename, Integer serverId, Data publishtask, PublishResult inResult)
	{
		
SmartJog ssc = mediaArchive.getModuleManager().getBean("smartJog");		
		String tracking = publishtask.get("trackingnumber");
		String[] numbers = tracking.split(",");
		int deliveryid = Integer.parseInt(numbers[1]);
		//Get a file on the local server
		Status status = ssc.updateStatus(numbers[0],serverId,deliveryid);
		publishtask.setProperty("completionpercent", status.getPercent() );
		if( status.getStatus().equals("Complete"))
		{
			inResult.setComplete(true);
		}
		else if( status.getStatus().equals("Error"))
		{
			inResult.setErrorMessage("SmartJob reported an status of Error");
		}
		else
		{
			inResult.setPending(true);
		}
		log.info("Smartjog status is updated to: " + status.getPercent());
		//ssc.
	}
	public void startSmartJogDelivery(MediaArchive mediaArchive, String inFilename, Data publishtask, Integer serverId, PublishResult inResult)
	{
		SmartJog ssc = mediaArchive.getModuleManager().getBean("smartJog");
			
		//Get a file on the local server
		log.info("Filename was: " + inFilename);
		int attempts = 0;
		
		ServerFile serverFile = ssc.getServerFile(new Integer(14573), inFilename);
		if(serverFile == null)
		{
			log.info("SmartJog has not noticed the file we put there. Try again later");
			return;
		}

		Delivery delivery = ssc.deliverFileToServer(serverId.intValue(), serverFile.getServerFileId()); 
		if (delivery != null)
		{ 
			log.info ( "SMART JOG: Delivery has just been created. Displaying its outgoing tracking :"); 
			 
			log.info("< deliveryId="+delivery.getDeliveryId()
					+ " - trackingNumber="
					+ delivery.getTrackingNumber()+" - filename="
					+ delivery.getFilename() +" - md5="+delivery.getMd5()
					+ " - status="+delivery.getStatus()+" >");
				
			publishtask.setProperty("trackingnumber","${delivery.getTrackingNumber()},${delivery.getDeliveryId()}" );
			inResult.setPending(true);
		}
		else
		{
			log.error("Delivery should no be null");
		}
	}
}

