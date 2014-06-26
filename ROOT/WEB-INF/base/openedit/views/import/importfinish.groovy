import org.openedit.Data;
import org.openedit.data.PropertyDetail;
import org.openedit.data.Searcher;

import com.openedit.util.URLUtilities;

public void importData(WebPageRequest inReq) throws Exception {
	Searcher searcher = loadSearcher(inReq);

	List data = new ArrayList();
	Page upload = getPageManager()
			.getPage(
					"/" + inReq.findValue("searchhome")
							+ "/temp/import/import.csv");
	Reader reader = upload.getReader();
	try {
		boolean done = false;
		CSVReader read = new CSVReader(reader, ',', '\"');

		String[] headers = read.readNext();

		int idcolumn = 0;

		String line = null;
		int rowNum = 0;
		Data question = null;
		List questions = new ArrayList();
		String[] tabs;
		while ((tabs = read.readNext()) != null) {

			rowNum++;

			String idCell = tabs[idcolumn];

			// This means we have moved on to a new product
			//natasha 18584
			if (idCell != null) {
				if (question == null || !question.getId().equals(idCell)) {
					
					Data target = (Data) searcher.searchById(idCell);
					if(target == null){
						for (Iterator iterator = data.iterator(); iterator.hasNext();) {
							Data one = (Data) iterator.next();
							if(idCell.equals(one.getId())){
								target = one;
							}
						}
						
					}
					if (target == null) {
						
						target = searcher.createNewData();
						target.setId(idCell);
					
					}
					
						
				
					addProperties(searcher, headers, tabs, target);
					if(target.getSourcePath() == null){
						target.setSourcePath(target.getId());
					}
					data.add(target);

				}
			}

		}
		// inErrorLog.add("Processed: " + products.size() + " products");

		// inSearcher.clear();
		// input.delete();

	} finally {
		FileUtils.safeClose(reader);
	}
	searcher.saveAllData(data, inReq.getUser());
	searcher.reIndexAll();
}


protected void addProperties(Searcher inSearcher, String[] inHeaders,
	String[] inInTabs, Data inProduct) {

for (int i = 0; i < inHeaders.length; i++) {
	String header = inHeaders[i];
	
	
	PropertyDetail detail = inSearcher.getPropertyDetails().getDetail(
			header);
	
	String val = inInTabs[i].trim();
	if(detail.isList()){
		
	}
	
	
	val = URLUtilities.xmlEscape(val);
	if("sourcepath".equals(header)){
		inProduct.setSourcePath(val);
	}
	if (detail != null && val != null && val.length() > 0) {
		
		inProduct.setProperty(detail.getId(), val);
	} else if(val != null && val.length() >0){
		inProduct.setProperty(header, val);
	}
}

}
