import java.util.HashMap;
import java.util.Map;

import com.openedit.page.Page
import com.openedit.util.Replacer;

import org.openedit.data.Searcher
import org.openedit.entermedia.Asset
import org.openedit.entermedia.MediaArchive
import org.openedit.*;

import com.openedit.WebPageRequest;
import com.openedit.hittracker.*;

public void init()
{
		MediaArchive mediaArchive = context.getPageValue("mediaarchive");//Search for all files looking for videos
		
		String prefix ="";
		String[] fields = context.getRequestParameters("field");
		Map vals = new HashMap();
		vals.putAll(context.getPageMap());
		
		if( fields != null)
		{
			for (int i = 0; i < fields.length; i++)
			{
				String val = context.getRequestParameter(prefix + fields[i]+ ".value");
				if( val != null)
				{
					vals.put(fields[i],val);
				}
			}
		}
		String sourcepath = mediaArchive.getCatalogSettingValue("projectassetupload");  //${division.uploadpath}/${user.userName}/${formateddate}
		
		String library = context.getRequestParameter("libraries.value");
		if(library != null)
		{
			vals.put("library", library);
		}

		library = context.getRequestParameter("library.value");
		if(library != null)
		{
			vals.put("library", library);
		}

		String division = context.getRequestParameter("division.value");
		if(division != null)
		{
			vals.put("division", division);
		}

		Replacer replacer = new Replacer();
		replacer.setSearcherManager(mediaArchive.getSearcherManager());
		replacer.setCatalogId(mediaArchive.getCatalogId());
		replacer.setAlwaysReplace(true);
		sourcepath = replacer.replace(sourcepath, vals);
		if( sourcepath.startsWith("/") )
		{
			sourcepath = sourcepath.substring(1);
		}
		sourcepath = sourcepath.replace("//", "/"); //in case of missing data
		
		context.setRequestParameter("sourcepath",sourcepath);
		context.putPageValue("sourcepath",sourcepath);
		
}

init();