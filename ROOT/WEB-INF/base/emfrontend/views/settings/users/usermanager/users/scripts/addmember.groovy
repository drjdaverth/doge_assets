import com.openedit.users.Group;

import com.openedit.OpenEditException;

import java.util.*;

import org.openedit.Data 
import org.openedit.data.Searcher 

import com.openedit.users.Group 
import com.openedit.users.User 
import com.openedit.page.manage.*;

public Group getGroup()
{
	String groupid = context.getRequestParameter("groupid");
	
	if (groupid == null)
	{
		throw new OpenEditException("mastergroup not set");
	}
	
	Group group = userManager.getGroup(groupid);
	if (group == null)
	{
		group = userManager.createGroup(groupid, catalogid);
	}
	
	return group;
}

public User getUser(Group inGroup)
{
	String email = context.getRequestParameter("email.value");
	String emailcheck = context.getRequestParameter("emailmatch.value");
	
	if (email == null || !email.equals(emailcheck))
	{
		throw new OpenEditException("E-mail addresses don't match.");
	}
	
	User newuser = userManager.getUserByEmail(email);
	
	String password = context.getRequestParameter("password.value");
	String passwordcheck = context.getRequestParameter("passwordmatch.value");
	
	if( newuser == null)
	{
		//create new
		if( !password.equals(passwordcheck))
		{
			throw new OpenEditException("passwords don't match");
		}
			
		String username = context.getRequestParameter("userName.value");
		newuser = userManager.createUser( username, password);
		newuser.setVirtual(false);
	}
	
	if (!newuser.isInGroup(inGroup))
	{
		newuser.addGroup(inGroup);
	}
	
	return newuser;
}

public Data saveUserProfile(String inUserId)
{
	Searcher userprofilesearcher = searcherManager.getSearcher(catalogid,"userprofile");
	
	hits = userprofilesearcher.fieldSearch("id", inUserId);
	def userprofile;
	if (hits.size() == 1)
	{
		userprofilehit = hits.get(0);
		userprofile = userprofilesearcher.searchById(userprofilehit.getId());
	}
	else
	{
		userprofile = userprofilesearcher.createNewData();
		userprofile.setId(inUserId);
		userprofile.setSourcePath(inUserId);
	}
		
	details = userprofilesearcher.getDetailsForView("userprofile/edit", context.getPageValue("user"));
	
	fieldlist = []
	
	details.each {
		fieldlist << it.id;
	}
	
	fields = fieldlist as String[]
	
	userprofilesearcher.saveDetails(context,fields,userprofile,userprofile.getId());
}

public void addUser()
{
	Group group = getGroup();
	User newuser = getUser(group);
	
	Searcher usersearcher = searcherManager.getSearcher("system","user");
	
	List details = usersearcher.getDetailsForView("user/simpleuseradd", context.getPageValue("user"));
	
	fieldlist = []
	details.each { fieldlist << it.id; }
	def fields = fieldlist as String[]
	
	usersearcher.saveDetails(context,fields,newuser,newuser.getId());
	
	saveUserProfile(newuser.getId());
}

public void editUser()
{
	String ok = context.getRequestParameter("save");
	if (ok == "true")
	{
		User loggedin = context.getPageValue("user");
		
		String userid = context.getRequestParameter("userid");
		
		User edituser = usermanager.getUser(userid);
		
		//save the user object
		Searcher usersearcher = searcherManager.getSearcher("system","user");
		String groupid = context.findValue("mastergroup");
		Group mastergroup = usermanager.getGroup(groupid);
		if( mastergroup == null)
		{
			mastergroup = usermanager.createGroup(groupid);
		}
		if( !edituser.isInGroup(mastergroup) )
		{
			edituser.addGroup(mastergroup);
			log.info("added group to user");
		}

		List details = usersearcher.getDetailsForView("user/simpleuseredit", loggedin);
		
		fieldlist = []
		
		details.each {
			fieldlist << it.id;
		}
		
		def fields = fieldlist as String[]		
		usersearcher.saveDetails(context,fields,edituser,userid);
		
		//save the userprofileobject
		saveUserProfile(userid);
		
		context.putPageValue("saved", "true");
	}
}

String method = context.getRequestParameter("method");
if (method == "adduser")
{
	addUser();
}
else if (method == "edituser")
{
	editUser();
}
