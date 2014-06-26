package publishing.publishers;

import org.apache.commons.httpclient.HttpClient
import org.apache.commons.httpclient.HttpMethod
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.methods.multipart.FilePart
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity
import org.apache.commons.httpclient.methods.multipart.Part
import org.apache.commons.httpclient.methods.multipart.StringPart
import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.dom4j.Element
import org.dom4j.io.SAXReader;
import org.openedit.Data
import org.openedit.entermedia.Asset
import org.openedit.entermedia.MediaArchive
import org.openedit.entermedia.publishing.*

import com.openedit.page.Page
import com.openedit.users.User
import com.openedit.users.UserManager

public class entermediapublisher extends basepublisher implements Publisher
{
	private static final Log log = LogFactory.getLog(entermediapublisher.class);
	private SAXReader reader = new SAXReader();
	
	public PublishResult publish(MediaArchive mediaArchive,Asset asset, Data inPublishRequest,  Data destination, Data preset)
	{
		PublishResult result = new PublishResult();

		Page inputpage = findInputPage(mediaArchive,asset,preset);
		String servername = destination.get("server");
		String username = destination.get("username");
		String url = destination.get("url");
		
		log.info("Publishing ${asset} to EnterMedia server ${servername}, with username ${username}.");
		
		UserManager userManager = mediaArchive.getModuleManager().getBean("userManager");
		User user = userManager.getUser(username);
		if(user == null)
		{
			result.setErrorMessage("Unknown user, ${username}");
			return result;
		}
		
		String password = userManager.decryptPassword(user);
		
		
		String exportname = inPublishRequest.get("exportname");
		File tosend = new File(inputpage.getContentItem().getAbsolutePath());
		Map results = upload(servername, mediaArchive.getCatalogId(), asset.getSourcePath(), tosend);
		if(upload != null){
		result.setComplete(true);
		log.info("publishished  ${asset} to FTP server ${servername}");
		
		}
		return result;
	}
	
	
	
	
	
	
	
	
	
	
	public Map<String, String> upload(String server, String inCatalogId, String inSourcePath, File inFile)
	{
		String url =server + "/media/services/" + "/uploadfile.xml?catalogid=" + inCatalogId;
		PostMethod method = new PostMethod(url);

		try
		{
			 def parts =[new FilePart("file", inFile.getName(), inFile),	new StringPart("sourcepath", "users/admin/")] as Part[];
			
			method.setRequestEntity( new MultipartRequestEntity(parts, method.getParams()) );
	
			Element root = execute(method);
			Map<String, String> result = new HashMap<String, String>();
			for(Object o: root.elements("asset"))
			{
				Element asset = (Element)o;
				result.put(asset.attributeValue("id"), asset.attributeValue("sourcepath"));
			}
			return result;
		}
		catch( Exception e )
		{
			return null;
		}
	}
	
	
	
	protected Page findInputPage(MediaArchive mediaArchive, Asset asset, Data inPreset)
	{
		
		
		if( inPreset.get("type") == "original")
		{
			return mediaArchive.getOriginalDocument(asset);
			
		}
		String input= "/WEB-INF/data/${mediaArchive.catalogId}/generated/${asset.sourcepath}/${inPreset.outputfile}";
		Page inputpage= mediaArchive.getPageManager().getPage(input);
		return inputpage;

	}
	protected Page findInputPage(MediaArchive mediaArchive, Asset asset, String presetid)
	{
		if( presetid == null)
		{
			return mediaArchive.getOriginalDocument(asset);
		}
		Data preset = mediaArchive.getSearcherManager().getData( mediaArchive.getCatalogId(), "convertpreset", presetid);
		return findInputPage(mediaArchive,asset,(Data)preset);
	}
	
	
	protected Element execute( HttpMethod inMethod )
	{
		try
		{
			int status = getClient().executeMethod(inMethod);
			if(status != 200)
			{
				throw new Exception("Request failed: status code " + status);
			}
			Element result = reader.read(inMethod.getResponseBodyAsStream()).getRootElement();
			return result;
		}
		catch (Exception e)
		{
			throw new RuntimeException(e);
		}
		
	}
	/**
	* The web services API require a client to log first.
	* The login is the same as one used within the EnterMedia usermanager
	* There are two Cookies that need to be passed in on subsequent requests
	* 1. JSESSIONID - This is used by resin or similar Java container. Enables short term sessions on the server
	* 2. entermedia.key - This allows the user to be auto-logged in. Useful for long term connections.
	* 	  If the web server is restarted then clients don't need to log in again
	*/
   public HttpClient getClient(Data destination)
   {
	   if (fieldClient == null)
	   {
		   fieldClient = new HttpClient();
//		   PostMethod method = new PostMethod(destination. + getDefaultAppId() + getRestPath() + "/login.xml");
//		   method.addParameter("accountname", getUserName());
//		   method.addParameter("password", getPassword());
//		   execute(method);
	   }
	   return fieldClient;
   }
	
}