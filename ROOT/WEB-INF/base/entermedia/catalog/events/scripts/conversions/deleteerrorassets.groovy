package conversions;

import org.openedit.data.Searcher 
import org.openedit.Data 
import org.openedit.entermedia.modules.*;
import org.openedit.entermedia.edit.*;

import com.openedit.WebPageRequest;
import com.openedit.page.*;
import org.openedit.entermedia.*;
import org.openedit.data.Searcher;
import com.openedit.hittracker.*;
import org.openedit.entermedia.creator.*;

import com.openedit.util.*;

import org.openedit.xml.*;
import org.openedit.entermedia.episode.*;

import java.util.List;
import java.util.ArrayList;

import org.entermedia.email.PostMail
import org.entermedia.email.TemplateWebEmail
import org.openedit.entermedia.util.*

public void clearerrors()
{
	mediaarchive = (MediaArchive)context.getPageValue("mediaarchive");//Search for all files looking for videos
	Searcher tasksearcher = mediaarchive.getSearcherManager().getSearcher (mediaarchive.getCatalogId(), "conversiontask");
	SearchQuery query = tasksearcher.createSearchQuery();
	query.addOrsGroup("status", "error");
	HitTracker newtasks = tasksearcher.search(query);
	List errors = new ArrayList(newtasks);
	
	//Email already went out on the event
	
	def grace_period = mediaarchive.getCatalogSettingValue("events_conversion_error_grace_period");
	def grace_periodmills = new TimeParser().parse(grace_period);
	
	for (Data hit in errors)
	{
		def submitted = newtasks.getDateValue(hit, "submitted");
		if (submittedby(grace_periodmills, submitted))
		{
			tasksearcher.delete(hit, user);
			Asset asset = mediaarchive.getAsset(hit.get("assetid"));
			if( asset != null)
			{
				mediaarchive.removeGeneratedImages(asset);
				//mediaarchive.getAssetSearcher().delete(asset,null);
				asset.setProperty("editstatus","7");
				mediaarchive.saveAsset(asset,null);
			}
			else
			{
				log.error("asset already removed");
			}
		}
	}
	
}

public boolean submittedby(def grace_period_in_milli, def submitted)
{
	//preset time - grace period is be greater than the conversion task submission date, return true for deletion
	def is_ready_for_deletion = new Date().getTime() > (submitted.getTime() + grace_period_in_milli.longValue());
	return is_ready_for_deletion
}

protected TemplateWebEmail getMail() {
	PostMail mail = (PostMail)mediaarchive.getModuleManager().getBean( "postMail");
	return mail.getTemplateWebEmail();
}

clearerrors();

