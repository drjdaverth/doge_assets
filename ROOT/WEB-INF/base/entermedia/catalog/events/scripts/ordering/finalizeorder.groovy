package ordering;

import com.openedit.users.User;
import java.text.SimpleDateFormat;
import com.openedit.WebPageRequest;
import com.openedit.hittracker.HitTracker
import com.openedit.page.Page;
import com.openedit.users.User;
import org.entermedia.email.PostMail;
import org.entermedia.email.TemplateWebEmail;
import org.openedit.Data
import org.openedit.data.Searcher
import org.openedit.entermedia.MediaArchive;
import org.openedit.entermedia.orders.Order;
import org.openedit.entermedia.orders.OrderHistory;
import org.openedit.entermedia.orders.OrderManager;
import org.openedit.event.WebEvent;
import org.openedit.entermedia.orders.OrderManager;
import org.openedit.entermedia.orders.OrderHistory;
import org.openedit.util.DateStorageUtil;

MediaArchive mediaarchive = (MediaArchive)context.getPageValue("mediaarchive");

protected Order getOrder(String inOrderId) {
	Searcher ordersearcher = mediaarchive.getSearcherManager().getSearcher(mediaarchive.getCatalogId(), "order");
	Order order = ordersearcher.searchById(inOrderId);
	return order;
}



protected TemplateWebEmail getMail() {
	PostMail mail = (PostMail)mediaarchive.getModuleManager().getBean( "postMail");
	return mail.getTemplateWebEmail();
}

protected void sendEmail(Order inOrder) {

//	if (inOrder.get('orderstatus') == 'complete' || inOrder.get('orderstatus') == 'finalizing') {
//		log.info("Order is aleady completed");
//		return;
//	}
//	inOrder.setProperty('orderstatus', 'finalizing');
	
	try {
		context.putPageValue("orderid", inOrder.getId());
		context.putPageValue("order", inOrder);


		String publishid = inOrder.get("publishdestination");
		String appid = inOrder.get("applicationid");


		if(publishid != null){
			Data dest = mediaarchive.getSearcherManager().getData(mediaarchive.getCatalogId(), "publishdestination", publishid);
			String email = dest.get("administrativeemail");
			if(email != null){
				sendEmail(context, email, "/${appid}/views/activity/email/admintemplate.html");
				//TODO: Save the fact that email was sent back to the publishtask?
			}
		}
		String emailto = inOrder.get('sharewithemail');
		String notes = inOrder.get('sharenote');

		if(emailto != null) {
			String expireson=inOrder.get("expireson");
			if ((expireson!=null) && (expireson.trim().length()>0)) {
				Date date = DateStorageUtil.getStorageUtil().parseFromStorage(expireson);
				context.putPageValue("expiresondate", date);
				context.putPageValue("expiresformat", new SimpleDateFormat("MMM dd, yyyy"));
			}

			sendEmail(context, emailto, "/${appid}/views/activity/email/sharetemplate.html");
		}
		if( "download" != inOrder.get("ordertype") )
		{ 
			String userid = inOrder.get("userid");
			if(userid != null)
			{
				User muser = userManager.getUser(userid);
				if(muser != null)
				{
					String owneremail =muser.getEmail();
					if(owneremail != null)
					{
						context.putPageValue("sharewithemail", emailto);
						sendEmail(context, owneremail, "/${appid}/views/activity/email/usertemplate.html");
					}
				}
			}
		}
//		inOrder.setProperty('orderstatus', 'complete');
		inOrder.setProperty('emailsent', 'true');
	}
	catch (Exception ex) 
	{
		inOrder.setOrderStatus( 'error');
		inOrder.setProperty('orderstatusdetail', "Could not email " + ex);
		ex.printStackTrace();
		log.error("Could not email " + ex);
	}

//	OrderManager manager = moduleManager.getBean("orderManager");
//	OrderHistory history = manager.createNewHistory( mediaarchive.getCatalogId(), inOrder, context.getUser(), "ordercomplete" );
//	manager.saveOrderWithHistory( mediaarchive.getCatalogId(), context.getUser(), inOrder, history );
	
	log.info("order is complete ${inOrder.getId()}");
	
}



WebEvent webevent = context.getPageValue("webevent");
String orderid = webevent.get('orderid');

Order order = getOrder(orderid);
sendEmail(order);

protected void sendEmail(WebPageRequest context, String email, String templatePage){
	//send e-mail
	Page template = pageManager.getPage(templatePage);
	WebPageRequest newcontext = context.copy(template);
	TemplateWebEmail mailer = getMail();
	mailer.loadSettings(newcontext);
	mailer.setMailTemplatePath(templatePage);
	mailer.setRecipientsFromCommas(email);
	//mailer.setMessage(inOrder.get("sharenote"));
	//mailer.setWebPageContext(context);
	mailer.send();
	log.info("email sent to ${email}");
}
