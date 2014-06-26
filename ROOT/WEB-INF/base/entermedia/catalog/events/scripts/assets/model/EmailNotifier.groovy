package assets.model;

import org.openedit.data.Searcher
import org.entermedia.email.PostMail
import org.entermedia.email.TemplateWebEmail
import org.openedit.Data
import org.openedit.data.Searcher
import org.openedit.entermedia.*
import org.openedit.entermedia.creator.*
import org.openedit.entermedia.edit.*
import org.openedit.entermedia.episode.*
import org.openedit.entermedia.modules.*
import org.openedit.entermedia.util.*
import org.openedit.xml.*

import com.openedit.WebPageRequest
import com.openedit.entermedia.scripts.EnterMediaObject;
import com.openedit.hittracker.*
import com.openedit.page.*
import com.openedit.users.UserManager
import com.openedit.util.*

public class EmailNotifier extends EnterMediaObject
{


protected void sendEmail(WebPageRequest context, String email, String templatePage){
	Page template = pageManager.getPage(templatePage);
	WebPageRequest newcontext = context.copy(template);
	TemplateWebEmail mailer = getMail();
	mailer.loadSettings(newcontext);
	mailer.setMailTemplatePath(templatePage);
	mailer.setRecipientsFromCommas(email);
	mailer.setSubject("Asset Upload Notification");
	mailer.send();
}

protected TemplateWebEmail getMail() {
	PostMail mail = (PostMail)mediaarchive.getModuleManager().getBean( "postMail");
	return mail.getTemplateWebEmail();
}

public void emailOnImport() 
{
	log.info("Made it OK");
	MediaArchive mediaArchive = context.getPageValue("mediaarchive");
	Searcher assetsearcher = mediaArchive.getAssetSearcher();
	SearchQuery q = assetsearcher.createSearchQuery();
	String ids = context.getRequestParameter("assetids");
	if( ids == null)
	{
		return;
	}
	String assetids = ids.replace(","," ");
	q.addOrsGroup( "id", assetids );
	HitTracker assets = assetsearcher.search(q);
	context.putPageValue("assets", assets);

	Data setting = mediaarchive.getCatalogSetting("events_notify_app");
	String appid = setting.get("value");
		
	def url = "/${appid}/components/notification/assetsimportedcustom.html"
	
	//get password and login
	UserManager userManager = mediaArchive.getModuleManager().getBean("userManager");
	
	HitTracker hits = userManager.getUsersInGroup("uploadnotification");
	
	hits.each{
		String email = it.email;
		sendEmail(context, email, url);
	}
}

}
//Comment this in to enable emailing on upload example
//emailOnImport();

