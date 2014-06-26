import groovy.xml.MarkupBuilder

import org.openedit.Data
import org.openedit.data.PropertyDetail
import org.openedit.data.PropertyDetails
import org.openedit.entermedia.Asset

import com.openedit.hittracker.HitTracker

import groovy.xml.MarkupBuilder

import org.openedit.Data
import org.openedit.data.PropertyDetails
import org.openedit.entermedia.Asset

import com.openedit.hittracker.HitTracker

import groovy.xml.MarkupBuilder

import org.openedit.Data
import org.openedit.data.PropertyDetails
import org.openedit.entermedia.Asset

import com.openedit.hittracker.HitTracker





public void init() {

	mediaarchive = context.getPageValue("mediaarchive");


	def writer = new StringWriter();
	def xml = new MarkupBuilder(writer);
	HitTracker hits = mediaarchive.getAssetSearcher().getAllHits();
	PropertyDetails details = mediaarchive.getAssetSearcher().getPropertyDetails();

	xml{
		hits.each{
			Data hit = (Data) it;
			Asset asset = mediaarchive.getAsset(hit.id);
			'row'{
				'id' hit.id
				'imagepath' "http://localhost:8080/media/catalogs/devices/downloads/preview/medium/${asset.sourcepath}/thumb.jpg"
				details.each{
					PropertyDetail detail = it;
					String detailid = detail.getId();
					String value = asset.getProperty(detail.getId());
					if(detail.isList()){
						Data remote  = mediaarchive.searcherManager.getData( detail.getListCatalogId(),detail.getListId(), value);

						if(remote != null){
							value= remote.getName();
						}
					}
					"field-${detail.getId()}" "${value}";
				}
			}
		}
	}


	context.putPageValue("export", writer.toString());
}



init();