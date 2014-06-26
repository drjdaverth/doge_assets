import java.util.Calendar
import java.util.GregorianCalendar

import org.openedit.Data
import org.openedit.data.Searcher

import com.openedit.hittracker.HitTracker
import com.openedit.hittracker.SearchQuery


Searcher ordersearcher = searcherManager.getSearcher(mediaarchive.getCatalogId(),"order");
SearchQuery oquery = ordersearcher.createSearchQuery();
oquery.addOrsGroup("orderstatus","ordered complete");
oquery.addOrsGroup("ordertype", "email export download");
GregorianCalendar cal = new GregorianCalendar();
cal.add(Calendar.MONTH, -1);
oquery.addAfter("date", cal.getTime());
oquery.addSortBy("historydateDown");
Collection hits = ordersearcher.search(oquery);

if( hits.size() == 0)
{
	return;
}
Searcher orderitemssearcher = searcherManager.getSearcher(mediaarchive.getCatalogId(),"orderitem");
SearchQuery iquery = orderitemssearcher.createSearchQuery();

StringBuffer orderids = new StringBuffer();
for(Data hit: hits)
{
	orderids.append(hit.getId() );
	orderids.append(" ");
}

iquery.addOrsGroup("orderid",orderids.toString());
HitTracker items = orderitemssearcher.search(iquery);

StringBuffer assetids = new StringBuffer();
for(Data hit : items)
{
	assetids.append(hit.get("assetid") );
	assetids.append(" ");
}

SearchQuery query = mediaarchive.getAssetSearcher().createSearchQuery();
if( assetids.length() == 0)
{
	query.addExact("id","none");
	mediaarchive.getAssetSearcher().cachedSearch(context,query);
	return;
}

query.addOrsGroup("id",assetids.toString());

mediaarchive.getAssetSearcher().cachedSearch(context,query);
