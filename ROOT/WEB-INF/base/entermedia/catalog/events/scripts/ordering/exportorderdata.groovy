package ordering;

import org.openedit.Data
import org.openedit.data.*
import org.openedit.entermedia.MediaArchive
import org.openedit.entermedia.util.CSVWriter

import com.openedit.hittracker.HitTracker
import com.openedit.page.Page
import com.openedit.page.manage.PageManager
public void init(){
	searcherManager = context.getPageValue("searcherManager");
	searchtype = context.findValue("searchtype");
	searcher = searcherManager.getSearcher(catalogid, "orderitem");
	HitTracker hits = searcher.getAllHits();
	details = searcher.getPropertyDetails();
	PageManager pm = searcherManager.getModuleManager().getBean("pageManager");
	Page outputp = pm.getPage("/rawdata.csv");
	FileWriter output  = new FileWriter(outputp.getContentItem().getAbsolutePath());
	CSVWriter writer  = new CSVWriter(output);
	int count = 0;
	headers = new String[details.size()+10];
	for (Iterator iterator = details.iterator(); iterator.hasNext();) {
		PropertyDetail detail = (PropertyDetail) iterator.next();
		headers[count] = detail.getText();
		count++;
	}

	MediaArchive archive = context.getPageValue("mediaarchive");
	Searcher orderSearcher = archive.getSearcher("order");
	Searcher userSearcher = searcherManager.getSearcher("system", "user");
	//writer.writeNext(headers);
	int counter = 0;
	for (Iterator iterator = hits.iterator(); iterator.hasNext();) {
		nextrow = new String[details.size()+50];//make an extra spot for c
		Integer fieldcount = 0;
        counter++;
		log.info("counter: ${counter}");
		tracker = iterator.next();
		order= archive.getSearcherManager().getData(archive.getCatalogId(), "order",  tracker.orderid);
		user = userSearcher.searchById(tracker.userid);
		if(!order || !user){
			log.info("missing data: ${order} ${user}");
		}
		fieldcount = addData(nextrow, fieldcount, orderSearcher, order);
		fieldcount =addData(nextrow,fieldcount, userSearcher, user);
		fieldcount =addData(nextrow,fieldcount, searcher, tracker);
		fieldcount++;
		if(user != null){
			user.getGroups().each{
				if(!it.name.contains("media")){
					log.info("Appending group:  "+ it.name)
					nextrow[fieldcount] = it.name;
					fieldcount++;
				}
			}
		}
		//nextrow[fieldcount] = tracker.user.registrationcode;
		//fieldcount++;
		//log.info("Store ${store.id}")
		writer.writeNext(nextrow);
	}
	writer.close();

	String finalout = output.toString();
	context.putPageValue("export", finalout);
}

protected int addData(String[] nextrow, Integer fieldcount, Searcher searcher, Data data){

	for (Iterator detailiter = searcher.getPropertyDetails().iterator(); detailiter.hasNext();) {

		PropertyDetail detail = (PropertyDetail) detailiter.next();
		if(data != null){
			String value = data.get(detail.getId());
			if(detail.getId().contains("password")){
				value="***";
			}
			if(detail.isList()){
				Data remote  = searcherManager.getData( detail.getListCatalogId(),detail.getListId(), value);

				if(remote != null){
					value= remote.getName();
				}
			}

			nextrow[fieldcount] = value;
		}
		fieldcount++;
	}
	return fieldcount;
}

init();
