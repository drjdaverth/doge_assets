
import org.openedit.data.Searcher 
import org.entermedia.email.PostMail
import org.entermedia.email.TemplateWebEmail
import org.openedit.Data
import org.openedit.MultiValued
import org.openedit.data.Searcher
import org.openedit.entermedia.*
import org.openedit.entermedia.creator.*
import org.openedit.entermedia.edit.*
import org.openedit.entermedia.episode.*
import org.openedit.entermedia.modules.*
import org.openedit.entermedia.util.*
import org.openedit.util.DateStorageUtil;
import org.openedit.xml.*

import com.openedit.WebPageRequest
import com.openedit.hittracker.*
import com.openedit.page.*
import com.openedit.util.*



public void saved(Data inData)
{
	if( inData == null)
	{
		return;
	}
	String type = context.findValue("searchtype");
	if( type == "librarycollectionshares")
	{
		String save = context.findValue("save");
		if(save  == "true" )
		{
			sendShareEmails((MultiValued)inData);
		}
			
	}
	else if ( type == "")
	{
		//sendNotify();
	}
	
}
public void sendShareEmails(MultiValued inData)
{
	String appid = context.findValue("applicationid");
	String sent = inData.get("sent");
	if( sent  == null || sent.isEmpty())
	{
		List emails = inData.getValues("emails");
		def url = "/${appid}/views/modules/librarycollection/email/sharetemplate.html";
		String subject = context.findValue("subject");
		subject = mediaarchive.getReplacer().replace(subject,inData);
		try
		{
			sendEmail(context, emails, url, subject);
			inData.setProperty("sent", DateStorageUtil.getStorageUtil().formatForStorage(new Date()));
			inData.setProperty("fromuser", context.getUserName() );
		}
		catch( Exception ex)
		{
			inData.setProperty("error", "Could not sent notification " + ex);
		}
		mediaarchive.getSearcher("librarycollectionshares").saveData(inData,null);		
	}
}

protected void sendEmail(WebPageRequest context, Collection emails, String templatePage, String subject)
{
	Page template = pageManager.getPage(templatePage);
	WebPageRequest newcontext = context.copy(template);
	TemplateWebEmail mailer = getMail();
	mailer.loadSettings(newcontext);
	mailer.setMailTemplatePath(templatePage);
	mailer.setRecipientsFromStrings(emails);
	mailer.setSubject(subject);
	mailer.send();
	log.info("sent email ${emails}");
}

protected TemplateWebEmail getMail()
{
	PostMail mail = (PostMail)mediaarchive.getModuleManager().getBean( "postMail");
	return mail.getTemplateWebEmail();
}
Data data = (Data)context.getPageValue("data");
saved(data);

