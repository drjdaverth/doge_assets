package notifications;

import com.openedit.WebPageRequest
import com.openedit.hittracker.HitTracker
import com.openedit.hittracker.SearchQuery
import com.openedit.page.Page
import com.openedit.users.UserManager
import org.entermedia.email.PostMail
import org.entermedia.email.TemplateWebEmail
import org.openedit.Data
import org.openedit.data.Searcher
import org.openedit.data.SearcherManager
import org.openedit.entermedia.Asset
import org.openedit.entermedia.MediaArchive
import org.openedit.entermedia.search.AssetSearcher
import org.openedit.util.DateStorageUtil


public void init(){
	
	log.info("------ Running Asset Upload Notification ------");
	
	//TODO (for the script to work):
	// - add onimportnotification field to asset table, set to boolean
	// - edit all assets to enable this field
	// - create new group called Email Notification (ie., emailnotification)
	// - assign user(s) to this group
	// - add a notification email (from) to _site.xconf or where ever
	
	
	
	final String SEARCH_FIELD = "onimportnotification";//add to asset table, boolean
	//do edit all
	MediaArchive archive = context.getPageValue("mediaarchive");
	Searcher userprofilesearcher = archive.getSearcher("userprofile");
	ArrayList<Data> assetsToUpdate = new ArrayList<Data>();
	AssetSearcher assetSearcher = archive.getAssetSearcher();
	SearchQuery query = assetSearcher.createSearchQuery();
	query.append(SEARCH_FIELD, "false");
	query.addSortBy("assetaddeddate");
	HitTracker hits = assetSearcher.search(query);
	hits.each {
		Data data = it;
		Asset asset = (Asset) assetSearcher.searchById(data.id);
		asset.setProperty(SEARCH_FIELD, "true");
		assetsToUpdate.add(asset);
	}
	if (assetsToUpdate.isEmpty()){
		log.info("No new uploads, aborting");
		return;
	}
	ArrayList emails = new ArrayList();
	UserManager sm = archive.getModuleManager().getBean("userManager");
	HitTracker results = sm.getUsersInGroup("emailnotification");
	if (results.size() > 0) {
		for(Iterator detail = results.iterator(); detail.hasNext();) {
			Data userInfo = (Data)detail.next();
			emails.add(userInfo.get("email"));
		}
	}
	if (emails.isEmpty()){
		log.info("No users belonging to Email Notification group, aborting");
		return;
	}
	log.info("Sending email notifications to ${emails}");
	String fromEmail = "support@openedit.org";//maybe put in _site.xconf?
	boolean isSent = dispatchEmail(archive,context,hits,emails,fromEmail);
	if (isSent){
		assetSearcher.saveAllData(assetsToUpdate, null);
	} else {
		log.info("Dispatch email was not successful, aborting asset update");
	}
	log.info("------ Finished running Asset Upload Notification ------");
}

public boolean dispatchEmail(MediaArchive inArchive, WebPageRequest inReq, HitTracker inHits, ArrayList<String> inEmails, String inFrom){
	PostMail mail = (PostMail)inArchive.getModuleManager().getBean( "postMail");
	
	Data setting = inArchive.getCatalogSetting("events_notify_app");
	String appid = setting.get("value");
	
	String templatePath = "/${appid}/components/notification/assetimport-email-template.html";
	Page template = inArchive.getPageManager().getPage(templatePath);
	WebPageRequest newcontext = inReq.copy(template);
	newcontext.putPageValue("hits",inHits);
	TemplateWebEmail mailer = mail.getTemplateWebEmail();
	mailer.setFrom(inFrom);
	mailer.loadSettings(newcontext);
	mailer.setMailTemplatePath(templatePath);
	mailer.setRecipientsFromStrings(inEmails);
	mailer.setSubject("Asset Upload Notification");
	mailer.send();
	return mailer.isSent();
}



init();