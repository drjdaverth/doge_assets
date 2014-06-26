package publishing.publishers;

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPReply
import org.openedit.Data
import org.openedit.data.Searcher
import org.openedit.entermedia.Asset
import org.openedit.entermedia.MediaArchive
import org.openedit.entermedia.publishing.*

import com.openedit.OpenEditException
import com.openedit.page.Page
import com.openedit.users.User
import com.openedit.users.UserManager

public class ftppublisher extends basepublisher implements Publisher
{
	private static final Log log = LogFactory.getLog(ftppublisher.class);
	
	public PublishResult publish(MediaArchive mediaArchive,Asset asset, Data inPublishRequest,  Data destination, Data preset)
	{
		PublishResult result = new PublishResult();

		Page inputpage = findInputPage(mediaArchive,asset,preset);
		String servername = destination.get("server");
		String username = destination.get("username");
		String url = destination.get("url");
		
		log.info("Publishing ${asset} to ftp server ${servername}, with username ${username}.");
		
		FTPClient ftp = new FTPClient();
		
		ftp.connect(servername);
		ftp.enterLocalPassiveMode();
		
		//check to see if connected
		int reply = ftp.getReplyCode();
		if(!FTPReply.isPositiveCompletion(reply))
		{
			result.setErrorMessage("Unable to connect to ${servername}, error code: ${reply}")
			ftp.disconnect();
			return result;
		}
		String password = destination.get("password");
		//get password and login
		if(password == null)
		{
			UserManager userManager = mediaArchive.getModuleManager().getBean("userManager");
			User user = userManager.getUser(username);
			password = userManager.decryptPassword(user);
		}
			
		ftp.login(username, password);
		reply = ftp.getReplyCode();
		if(!FTPReply.isPositiveCompletion(reply))
		{
			result.setErrorMessage("Unable to login to ${servername}, error code: ${reply}");
			ftp.disconnect();
			return result;
		}
		ftp.setFileTransferMode(FTPClient.BINARY_FILE_TYPE);
		
		//change paths if necessary
		if(url != null && url.length() > 0)
		{
			ftp.makeDirectory(url);
			ftp.changeWorkingDirectory(url);
			reply = ftp.getReplyCode();
			if(!FTPReply.isPositiveCompletion(reply))
			{
				result.setErrorMessage("Unable to to cd to ${url}, error code: ${reply}");
				ftp.disconnect();
				return result;
			}
		}
		
		String exportname = inPublishRequest.get("exportname");
	
		ftp.storeFile(exportname, inputpage.getInputStream());
		reply = ftp.getReplyCode();
		if(!FTPReply.isPositiveCompletion(reply))
		{
			result.setErrorMessage("Unable to to send file, error code: ${reply}");
			ftp.disconnect();
			return result;
		}
		
		if(ftp.isConnected())
		{
			ftp.disconnect();
		}
		result.setComplete(true);
		log.info("publishished  ${asset} to FTP server ${servername}");
		return result;
	}
}