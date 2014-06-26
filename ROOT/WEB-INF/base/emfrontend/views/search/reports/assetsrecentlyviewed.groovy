import java.util.Calendar
import java.util.GregorianCalendar

import org.openedit.Data
import org.openedit.data.Searcher

import com.openedit.hittracker.HitTracker
import com.openedit.hittracker.SearchQuery

// Find all asset ids viewed in the last month
Searcher orderitemssearcher = searcherManager.getSearcher(mediaarchive.getCatalogId(),"assetpreviewLog");
SearchQuery iquery = orderitemssearcher.createSearchQuery();
GregorianCalendar cal = new GregorianCalendar();
cal.add(Calendar.MONTH, -1);
iquery.addAfter("date", cal.getTime());

HitTracker items = orderitemssearcher.search(iquery);

Set assetids=new HashSet();
for (Data hit : items)
{
	String assetid=hit.get("assetid");
	if (assetid!=null) 
	{
		assetids.add(assetid);
	}
}

SearchQuery query = mediaarchive.getAssetSearcher().createSearchQuery();
if( assetids.size() == 0)
{
	query.addExact("id","none");
	mediaarchive.getAssetSearcher().cachedSearch(context,query);
	return;
}

// Build space delimited String from set of Asset ids
StringBuffer assetidsbuffer = new StringBuffer();
for (String assetid: assetids) {
		assetidsbuffer.append(assetid );
		assetidsbuffer.append(" ");
}

query.addOrsGroup("id",assetidsbuffer.toString());

mediaarchive.getAssetSearcher().cachedSearch(context,query);
