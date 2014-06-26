package notifications;

import java.net.URL;

import com.openedit.WebPageRequest
import com.openedit.hittracker.HitTracker
import com.openedit.hittracker.SearchQuery

import org.apache.commons.httpclient.HttpClient;

import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.PostMethod
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.openedit.Data
import org.openedit.data.Searcher
import org.openedit.data.SearcherManager
import org.openedit.entermedia.MediaArchive
import org.openedit.entermedia.push.PushManager
import org.openedit.entermedia.search.AssetSearcher

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import org.dom4j.Attribute;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public void init(){
	log.info("------ Running Asset Approved Notification ------");
	MediaArchive archive = context.getPageValue("mediaarchive");
	PushManager pushmanager = (PushManager)archive.getModuleManager().getBean("pushManager");
	pushmanager.pullApprovedAssets(context,archive);
	log.info("------ Finished Asset Approved Notification ------");
}

init();