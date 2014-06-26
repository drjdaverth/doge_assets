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
//log.info("hits: " +hits);
searcherManager = context.getPageValue("searcherManager");
searchtype = context.findValue("searchtype");
catalogid = context.findValue("catalogid");
searcher = searcherManager.getSearcher(catalogid, searchtype);
boolean friendly = Boolean.parseBoolean(context.getRequestParameter("friendly"));
 details = searcher.getDetailsForView("csvexport", context.getUser());
if(details == null){
	details = searcher.getPropertyDetails();
}

Writer output = context.getPageStreamer().getOutput().getWriter();

//StringWriter output  = new StringWriter();
CSVWriter writer  = new CSVWriter(output);
int count = 0;
headers = new String[details.size()];
for (Iterator iterator = details.iterator(); iterator.hasNext();)
{
	PropertyDetail detail = (PropertyDetail) iterator.next();
	if(friendly){
	headers[count] = detail.getText();
	} else{
	headers[count] = detail.getId();
	}		
	count++;
}
writer.writeNext(headers);
	//log.info("about to start: " + hits);
try
{

	for (Iterator iterator = hits.iterator(); iterator.hasNext();)
	{
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
				//detail.get
				Data remote  = searcherManager.getData( detail.getListCatalogId(),detail.getListId(), value);
			
					if(remote != null){
					value= remote.getName();
				}
				
			}
	
			nextrow[fieldcount] = value;
		
			fieldcount++;
		}	
		writer.writeNext(nextrow);
	}
	
}
catch( Exception ex)
{
	log.error("Could not process " + hit.getSourcePath() , ex);
	writer.flush();
	output.write("Could not process path: " + hit.getSourcePath() + " id:" + hit.getId() );
}

writer.close();

//String finalout = output.toString();
//context.putPageValue("export", finalout);
context.setHasRedirected(true);


