import org.entermedia.email.PostMail
import org.entermedia.email.TemplateWebEmail
import org.openedit.Data
import org.openedit.data.*
import org.openedit.entermedia.MediaArchive
import org.openedit.entermedia.util.CSVWriter
import org.openedit.util.DateStorageUtil

import com.openedit.OpenEditException
import com.openedit.WebPageRequest
import com.openedit.hittracker.HitTracker
import com.openedit.page.Page
import com.openedit.util.Replacer

public void init(){

	
	
	try{
		
	
	MediaArchive mediaarchive = context.getPageValue("mediaarchive");

	SearcherManager searcherManager = context.getPageValue("searcherManager");
	searchtype = context.findValue("searchtype");
	catalogid = context.findValue("catalogid");
	searcher = searcherManager.getSearcher(catalogid, searchtype);
	boolean friendly = Boolean.parseBoolean(context.findValue("friendly"));
	String view = context.findValue("view");
	details = null;
	if(view != null){
		details = searcher.getDetailsForView(view, context.getUser());
	}

	if(details == null){
		details = searcher.getPropertyDetails();
	}

	HitTracker hits = searcher.getAllHits(context);
	String exportpath = context.findValue("exportpath");

	HashMap map = new HashMap();
	String date = DateStorageUtil.getStorageUtil().formatForStorage(new Date());
	map.put("date",date );
	Replacer replacer = new Replacer();
	exportpath = replacer.replace(exportpath, map);

	Page output = mediaarchive.getPageManager().getPage(exportpath);

	String realpath = output.getContentItem().getAbsolutePath();
	File outputfile = new File(realpath);
	File parent = outputfile.parentFile;
	
	parent.mkdirs();
	FileWriter out = new FileWriter(outputfile);
	CSVWriter writer  = new CSVWriter(out);
	int count = 0;
	headers = new String[details.size()];
	for (Iterator iterator = details.iterator(); iterator.hasNext();) {
		PropertyDetail detail = (PropertyDetail) iterator.next();
		headers[count] = detail.getText();
		count++;
	}
	writer.writeNext(headers);
	log.info("about to start: " + hits.size() + "records");
	context.putPageValue("records", hits);
	context.putPageValue("date", date);
	for (Iterator iterator = hits.iterator(); iterator.hasNext();) {
		hit =  iterator.next();
		//tracker = searcher.searchById(hit.get("id"));
		tracker = hit;

		nextrow = new String[details.size()];//make an extra spot for c
		int fieldcount = 0;
		for (Iterator detailiter = details.iterator(); detailiter.hasNext();)
		{
			PropertyDetail detail = (PropertyDetail) detailiter.next();
			String value = tracker.get(detail.getId());
			//do special logic here

			if(detail.isList() && friendly){
				//Join?
				//#set($label = $searcherManager.getValue($catalogid, $detail.render, $type.properties))
				if(detail.render){
					value = searcherManager.getValue(catalogid, detail.render, tracker.properties);
				} else{

					Data remote  = searcherManager.getData( detail.getListCatalogId(),detail.getListId(), value);

					if(remote != null){
						value= remote.getName();
					}
				}
			}
			nextrow[fieldcount] = value;

			fieldcount++;

		}

		






		writer.writeNext(nextrow);
	}
	writer.close();
	
	
	


	boolean notify = Boolean.parseBoolean(context.findValue("sendnotifications"));
	log.info("Compelte - sending notifications : ${notify}");
	if(notify){
		Data setting = mediaarchive.getCatalogSetting("events_notify_app");
		String appid = setting.get("value");

		def url = "/${appid}/components/notification/exportcomplete.html"
		String emails = context.findValue("to");
		sendEmail(context,emails ,url);

	}

	} 
	
	catch(Exception e){
		Data setting = mediaarchive.getCatalogSetting("events_notify_app");
		String appid = setting.get("value");
		context.putPageValue("error", e);
		String date = DateStorageUtil.getStorageUtil().formatForStorage(new Date());
		context.putPageValue("date", date)
				def url = "/${appid}/components/notification/exporterror.html"
		String emails = context.findValue("to");
		
		sendEmail(context,emails ,url);
	}
	



}


protected void sendEmail(WebPageRequest context, String email, String templatePage){
	Page template = pageManager.getPage(templatePage);
	WebPageRequest newcontext = context.copy(template);
	TemplateWebEmail mailer = getMail();
	mailer.loadSettings(newcontext);
	mailer.setMailTemplatePath(templatePage);
	mailer.setRecipientsFromCommas(email);
	mailer.setSubject("Data Export Nofification");
	mailer.send();
	log.info("Export Notifications email sent to ${email}");
}

protected TemplateWebEmail getMail() {
	PostMail mail = (PostMail)mediaarchive.getModuleManager().getBean( "postMail");
	return mail.getTemplateWebEmail();
}


init();