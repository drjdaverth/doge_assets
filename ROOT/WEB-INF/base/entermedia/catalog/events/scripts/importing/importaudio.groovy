package importing;

import com.openedit.page.Page 
import org.openedit.data.Searcher 
import org.openedit.entermedia.Asset 
import org.openedit.entermedia.MediaArchive 
import org.openedit.*;

import com.openedit.WebPageRequest;
import com.openedit.hittracker.*;
import org.openedit.entermedia.creator.*;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.ResultSetMetaData;
import java.sql.Statement;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Element;
import org.openedit.data.PropertyDetail;
import org.openedit.data.PropertyDetails;
import org.openedit.entermedia.Asset;
import org.openedit.entermedia.CatalogConverter;
import org.openedit.entermedia.Category;
import org.openedit.entermedia.ConvertStatus;
import org.openedit.entermedia.MediaArchive;
import org.openedit.xml.XmlArchive;
import org.openedit.xml.XmlFile;

import com.openedit.ModuleManager;
import com.openedit.OpenEditException;
import com.openedit.page.manage.PageManager;
import org.openedit.entermedia.scanner.AssetImporter;

public void init()
{
	MediaArchive archive = context.getPageValue("mediaarchive");
	AssetImporter importer = (AssetImporter)moduleManager.getBean("assetImporter");
	//importer.setExcludeFolders("Fonts,Links");
	importer.setIncludeExtensions("mp3,wav");
	importer.setUseFolders(false);
	
	String assetRoot = "/WEB-INF/data/" + archive.getCatalogId() + "/originals/";
		
	List created = importer.processOn(assetRoot, assetRoot, archive, context.getUser());
	log.info("created audio files " + created.size() );
		
}

init();
