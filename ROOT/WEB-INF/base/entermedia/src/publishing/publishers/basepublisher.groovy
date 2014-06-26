package publishing.publishers
import java.util.List;

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.openedit.Data
import org.openedit.data.Searcher
import org.openedit.entermedia.Asset
import org.openedit.entermedia.MediaArchive
import org.openedit.entermedia.publishing.*

import com.openedit.page.Page

public abstract class basepublisher implements Publisher {
	private static final Log log = LogFactory.getLog(basepublisher.class);

	protected publishFailure(MediaArchive mediaArchive,Data inPublishRequest, String inMessage) {
		inPublishRequest.setProperty("errormessage", inMessage);
		inPublishRequest.setProperty("status", "error");
	}
	protected Page findInputPage(MediaArchive mediaArchive, Asset asset, Data inPreset) {
		if( inPreset.get("type") == "original") {
			return mediaArchive.getOriginalDocument(asset);
		}
		String input= "/WEB-INF/data/${mediaArchive.catalogId}/generated/${asset.sourcepath}/${inPreset.outputfile}";
		Page inputpage= mediaArchive.getPageManager().getPage(input);
		return inputpage;
	}
	protected Page findInputPage(MediaArchive mediaArchive, Asset asset, String presetid) {
		if( presetid == null) {
			return mediaArchive.getOriginalDocument(asset);
		}
		Data preset = mediaArchive.getSearcherManager().getData( mediaArchive.getCatalogId(), "convertpreset", presetid);
		return findInputPage(mediaArchive,asset,(Data)preset);
	}


	public PublishResult publish(MediaArchive mediaArchive,Asset inAsset, Data inPublishRequest,  Data inDestination, List inPresets) {
		PublishResult result = new PublishResult();
		for (Iterator iterator = inPresets.iterator(); iterator.hasNext();) {
			Data preset = (Data) iterator.next();
			result = publish(mediaArchive, inAsset, inPublishRequest, inDestination, preset);
		}
		return result;//should check all of these?
	}
}