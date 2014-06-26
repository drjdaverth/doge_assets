package sql;

/*
* Created on Mar 24, 2006
*/

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

public class SqlImportConverter extends CatalogConverter
{
   private static final Log log = LogFactory.getLog(SqlImportConverter.class);
   protected ModuleManager fieldModuleManager;

   // protected ImageConvertQueue fieldImageConvertQueue;

   DateFormat format = null;

   protected PageManager fieldPageManager;
   protected XmlArchive fieldXmlArchive;
   
   public SqlImportConverter()
   {
	   
   }

   public void importAssets(MediaArchive inArchive, ConvertStatus inErrorLog) throws Exception
   {
	   // archive.setPageManager(getPageManager());
	   XmlFile settings = getXmlArchive().getXml( inArchive.getCatalogHome() + "/configuration/sqlsync.xml");
	   Element config = settings.getElement("databasesync");

	   if (config == null)
	   {
		   log.info("No such file " + inArchive.getCatalogHome() +"/configuration/sqlsync.xml" );	   
		   return;
	   }
	   format = new SimpleDateFormat("yyyy-MM-dd h:m:s");// 2005-03-04
	   // 08:28:57
	   for (Iterator iter = config.elementIterator("database"); iter.hasNext();)
	   {
		   Element element = (Element) iter.next();
		   processEntries(element, inErrorLog, inArchive);
	   }
	   inArchive.getCategoryArchive().saveAll();
   }

   public PageManager getPageManager()
   {
	   return fieldPageManager;
   }

   public void setPageManager(PageManager inPageManager)
   {
	   fieldPageManager = inPageManager;
   }

   public void processEntries(Element config, ConvertStatus inErrorLog, MediaArchive inArchive) throws Exception
   {
	   String url = config.attributeValue("url");
	   String table = config.attributeValue("table");
	   String driverName = config.attributeValue("driver");
	   String categoryField = config.attributeValue("categoryfield");
	   String subCategoryField = config.attributeValue("subCategoryField");
	   String nameField = config.attributeValue("namefield");
	   String query = config.attributeValue("query");
	   String parentCategoryId = config.attributeValue("id");
	   String rootPath = config.attributeValue("rootpath");
	   List excludeList = getCategoriesToExclude(config);
	   Category parentCategory = inArchive.getCategoryArchive().getCategory(parentCategoryId);
	   if (parentCategory == null)
	   {
		   parentCategory = new Category();
		   parentCategory.setName(parentCategoryId);
		   parentCategory.setId(parentCategoryId);
		   inArchive.getCategoryArchive().addChild(parentCategory);
		   inArchive.getCategoryArchive().saveAll();
	   }

	   log.info("Starting SQL Import...");
	   inErrorLog.add("Checking database " + url);
	   java.sql.ResultSet rs = null;
	   Connection conn = null;

	   try
	   {
		   Driver driver = (Driver) Class.forName(driverName).newInstance();
		   DriverManager.registerDriver(driver);
		   conn = DriverManager.getConnection(url);

		   Statement stmt = conn.createStatement();

		   if (query == null)
		   {
			   query = "select * from " + table;
		   }
		   rs = stmt.executeQuery(query);

		   PropertyDetails details = inArchive.getAssetPropertyDetails();
		   ResultSetMetaData rsMetaData = rs.getMetaData();
		   int colcount = rsMetaData.getColumnCount();
		   List detailsList = new ArrayList(colcount);
		   //only copy the colums we have
		   for (int i = 0; i < colcount; i++)
		   {
			   String external = rsMetaData.getColumnName(i+1);
			   PropertyDetail detail = details.getDetailByExternalId(external);
			   if( detail != null)
			   {
				   if (detail.isStored() || detail.isKeyword())  //why these options?
				   {
					   detailsList.add(detail);
				   }
			   }
		   }
		   
		   while (rs.next())
		   {
			   String name = rs.getString(nameField);
			   // extract the category
			   String categoryString = rs.getString(categoryField);

			   String categoryId = getIdFromString(categoryString);

			   Category cat;
			   if (categoryString == null || categoryString.length() == 0)
			   {
				   cat = parentCategory;
			   }
			   else
			   {
				   cat = inArchive.getCategoryArchive().getCategory(categoryId);
			   }

			   if (excludeList.contains(categoryString))
			   {
				   // log.info("skipping catalog " + categoryString);
				   continue;
			   }

			   if (cat == null)
			   {
				   cat = new Category();
				   cat.setName(categoryString);
				   cat.setId(categoryId);
				   parentCategory.addChild(cat);
				   inArchive.getCategoryArchive().cacheCategory(cat);
				   inArchive.getCategoryArchive().saveAll();
			   }

			   // extract the sub category
			   String subCategoryString = rs.getString(subCategoryField);
			   if (excludeList.contains(subCategoryString))
			   {
				   // log.info("skipping catalog " + subCategoryString);
				   continue;
			   }
			   String subCategoryId = getIdFromString(subCategoryString);

			   Category subcat = null;
			   if (subCategoryString == null || subCategoryString.length() == 0)
			   {
				   subcat = cat;
			   }
			   else
			   {
				   subcat = inArchive.getCategoryArchive().getCategory(subCategoryId);
			   }

			   if (subcat == null)
			   {
				   subcat = new Category();
				   subcat.setName(subCategoryString);
				   subcat.setId(subCategoryId);
				   cat.addChild(subcat);
				   inArchive.getCategoryArchive().cacheCategory(subcat);
				   inArchive.getCategoryArchive().saveAll();
			   }

			   Map props = new HashMap();
			   
			   for (Iterator iter = detailsList.iterator(); iter.hasNext();)
			   {
				   PropertyDetail detail = (PropertyDetail) iter.next();
				   try
				   {
					   String value = rs.getString(detail.getExternalId());
					   String key = detail.getId();
					   if (key != null && value != null)
					   {
						   props.put(key, value);
					   }
				   }
				   catch (Exception e)
				   {
					   log.error(e);
				   }
			   }
			   String mountname = config.attributeValue("mountname");
			   if( mountname == null)
			   {
				   throw new OpenEditException("mountname setting is required");
			   }
			   String folderpathname = config.attributeValue("folderpathname");
			   String folder = (String)props.get(folderpathname);
			   if( folder == null)
			   {
				   //folder = "sql";
				   throw new OpenEditException("folderpathname was empty on record " + props);
			   }
			   //check the begining
			   if( !folder.startsWith("/"))
			   {
				   folder = "/" + folder;
			   }
			   //check the end
			   if( folder.endsWith("/"))
			   {
				   folder = folder.substring(0,folder.length() - 1);
			   }
			   
			   
			   String sourcepath = mountname + folder + "/" + name;

			   String id = getIdFromString(sourcepath);
			   Asset newasset = inArchive.getAssetBySourcePath(sourcepath);
			   if (!inErrorLog.isForcedConvert() && newasset != null)
			   {
				   // TODO: Check modification stamp
				   inArchive.getAssetArchive().clearAsset(newasset);
			   }
			   else
			   {
				   newasset = new Asset();
				   newasset.setId(id);
				   newasset.setName(name);
			   }
			   if(newasset.getSourcePath() == null)
			   {
				   newasset.setSourcePath(sourcepath);
			   }
			   newasset.addCategory(subcat);
			   //SFXE/AM_/AM008/AM_ AM008-99 StormInt;9905.WAV
			   for (Iterator iterator = props.keySet().iterator(); iterator.hasNext();)
			   {
				   String key = (String) iterator.next();
				   newasset.setProperty(key, (String)props.get(key));
			   }
			   //Check if File Format has been set.  If not, try to figure it out.
			   newasset.setProperty("fileformat", newasset.getFileFormat());
			   inArchive.getAssetArchive().saveAsset(newasset);
			   inArchive.getAssetSearcher().updateIndex(newasset);

			   inArchive.getAssetArchive().clearAsset(newasset);
			   inErrorLog.addConvertedAsset(newasset);

		   }
	   }
	   finally
	   {
		   if (rs != null)
		   {
			   rs.close();
		   }
		   if (conn != null)
		   {
			   conn.close();
		   }
	   }
	   log.info("Finished SQL Import");
   }

   private String getIdFromString(String inputString)
   {
	   String categoryId = inputString.toLowerCase();
	   categoryId = extractId(categoryId, true);
	   return categoryId;
   }

   protected List getCategoriesToExclude(Element config)
   {
	   ArrayList categoryList = new ArrayList();
	   for (Iterator iter = config.elementIterator("exclude"); iter.hasNext();)
	   {
		   Element element = (Element) iter.next();
		   String category = element.getText();
		   categoryList.add(category);

	   }
	   return categoryList;
   }

   public XmlArchive getXmlArchive()
   {
	   return fieldXmlArchive;
   }

   public void setXmlArchive(XmlArchive inXmlArchive)
   {
	   fieldXmlArchive = inXmlArchive;
   }

}