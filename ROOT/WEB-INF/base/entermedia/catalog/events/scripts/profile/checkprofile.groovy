package profile;

import org.openedit.Data
import org.openedit.data.Searcher
import org.openedit.entermedia.MediaArchive

user = context.getUser();
if(user == null){
	return;
}
MediaArchive mediaarchive = context.getPageValue("mediaarchive");
Searcher profilesearcher = mediaarchive.getSearcherManager().getSearcher(mediaarchive.getCatalogId(), "profile");
Data profile = profilesearcher.searchById(context.getUser().getId());
if(profile == null){
	profile = profilesearcher.createNewData();
	profile.setId(context.getUser().getId());
	profile.setSourcePath(context.getUser().getId());
	profilesearcher.saveData(profile, context.getUser());
	
}
context.putPageValue("profile", profile);

