import java.io.StringWriter
import java.util.Iterator

import org.openedit.Data
import org.openedit.data.*
import org.openedit.entermedia.util.CSVWriter

import com.openedit.hittracker.HitTracker


HitTracker hits = (HitTracker) context.getPageValue("hits");
if(hits == null){
	String sessionid = context.getRequestParameter("hitssessionid");
	hits = context.getSessionValue(sessionid);
}
log.info("hits: " +hits);
searcherManager = context.getPageValue("searcherManager");
searchtype = context.findValue("searchtype");
catalogid = context.findValue("catalogid");
searcher = searcherManager.getSearcher(catalogid, searchtype);
String friendlystring = context.getRequestParameter("friendly");
if(friendlystring == null){
	friendlystring = context.findValue("friendly");
}

boolean friendly = Boolean.parseBoolean(friendlystring);

String view = context.findValue("view");
String[] detaillist = context.getRequestParameters("detail");
Collection details = null;

if(detaillist != null){
	log.info("Detail List was used - customizing export");
	details = new ArrayList();
	for(int i = 0;i<detaillist.length;i++){
		String detailid = detaillist[i];
		detail = searcher.getDetail(detailid);
		if(detail != null){
			details.add(detail);
		}
	}
} else{
	details = searcher.getDetailsForView(view, context.getUser());
}


if(details == null){
	details = searcher.getPropertyDetails();
}

StringWriter output  = new StringWriter();
CSVWriter writer  = new CSVWriter(output);
int count = 0;
headers = new String[details.size()];
for (Iterator iterator = details.iterator(); iterator.hasNext();) {
	PropertyDetail detail = (PropertyDetail) iterator.next();
	headers[count] = detail.getText();
	count++;
}
writer.writeNext(headers);
log.info("about to start: " + hits);
for (Iterator iterator = hits.iterator(); iterator.hasNext();) {
	hit =  iterator.next();
	tracker = searcher.searchById(hit.get("id"));


	nextrow = new String[details.size()];//make an extra spot for c
	int fieldcount = 0;
	for (Iterator detailiter = details.iterator(); detailiter.hasNext();)
	{
		PropertyDetail detail = (PropertyDetail) detailiter.next();
		String value = tracker.get(detail.getId());
		//do special logic here
		if(detail.isList() && friendly){
			Data remote  = searcherManager.getData( detail.getListCatalogId(),detail.getListId(), value);
			if(remote != null){
				String remotefield = detail.remotefield;
				if(remotefield == null){
					value= remote.getName();
				} else{
					value = remote.getProperty(remotefield);
				}
			}
		}


		nextrow[fieldcount] = value;

		fieldcount++;
	}




	writer.writeNext(nextrow);
}
writer.close();

String finalout = output.toString();
context.putPageValue("export", finalout);


