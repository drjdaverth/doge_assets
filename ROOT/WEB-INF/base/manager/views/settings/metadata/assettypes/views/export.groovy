import org.openedit.data.Searcher
import org.openedit.data.PropertyDetails;
import org.openedit.data.View
import org.openedit.Data;
import org.openedit.data.PropertyDetailsArchive;
import java.util.*;
import org.openedit.Data
import org.openedit.data.Searcher
import com.openedit.page.manage.*;


Searcher typessearcher = searcherManager.getSearcher(mediaarchive.getCatalogId(), "assettype");

PropertyDetailsArchive archive =  searcherManager.getPropertyDetailsArchive(mediaarchive.getCatalogId());
PropertyDetails details = archive.getPropertyDetailsCached("asset");
View toplevel = archive.getDetails(details,"asset/searchselect", null);
toplevel.clear();

Set existingchildren = new HashSet();

Collection views = searcherManager.getList(mediaarchive.getCatalogId(), "assettype/views");
Collection types = typessearcher.getAllHits();

for( Data view in views)
{	
	for( Data assettype in types)
	{
		View existingview = archive.getDetails(details,"asset/assettype/${assettype.getId()}/${view.getId()}", null);
		if( existingview != null &&  existingview.hasChildren() )
		{
			for(Data detail in existingview)
			{
				if( !existingchildren.contains(detail.getId() ))
				{
					toplevel.add(detail);
					existingchildren.add( detail.getId() );
				}
			}
		}
	}
}

Collections.sort(toplevel);


archive.saveView(mediaarchive.getCatalogId(),toplevel,user);
log.info("Saved pick list");