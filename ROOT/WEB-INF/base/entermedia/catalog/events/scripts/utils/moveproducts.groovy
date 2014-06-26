package utils;

import org.openedit.data.Searcher
import org.openedit.entermedia.MediaArchive

import com.openedit.page.Page
import com.openedit.page.manage.PageManager

public void init(){
MediaArchive archive = context.getPageValue("mediaarchive");
Searcher productSearcher = archive.getSearcher("product");
PageManager pm = archive.getPageManager();
Page source = pm.getPage("/${archive.getCatalogId()}/products/");
Page destination = pm.getPage("/WEB-INF/data/${archive.getCatalogId()}/products/");
if(source.exists()){
pm.movePage(source, destination);
}

Page counter =  pm.getPage("/${archive.getCatalogId()}/data/product.properties");
Page target = pm.getPage("/WEB-INF/data/${archive.getCatalogId()}/products/product.properties");
if(counter.exists()){
pm.movePage(counter, target);
}



}
init();