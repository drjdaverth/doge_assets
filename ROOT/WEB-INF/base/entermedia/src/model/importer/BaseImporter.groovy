package model.importer;

import java.io.Reader;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.zip.Inflater;

import org.openedit.*;
import org.openedit.data.BaseData;
import org.openedit.data.PropertyDetail;
import org.openedit.data.PropertyDetails;
import org.openedit.data.Searcher;
import org.openedit.entermedia.util.CSVReader;
import org.openedit.entermedia.util.Header
import org.openedit.entermedia.util.ImportFile;
import org.openedit.entermedia.util.Row;


import com.openedit.WebPageRequest;
import com.openedit.entermedia.scripts.EnterMediaObject;
import com.openedit.page.Page;
import com.openedit.util.*;


public class BaseImporter extends EnterMediaObject 
{
	protected Map<String,String> fieldLookUps;
	protected Searcher fieldSearcher;
	protected boolean fieldMakeId;
	
	public Searcher getSearcher()
	{
		return fieldSearcher;
	}
	public void importData() throws Exception 
	{
		fieldSearcher = loadSearcher(context);
		
		String path = context.getRequestParameter("path");
		Page upload = getPageManager().getPage(path);
		Reader reader = upload.getReader();
		List data = new ArrayList();
		
		try 
		{
			ImportFile file = new ImportFile();
			file.setParser(new CSVReader(reader, ',', '\"'));
			file.read(reader);
						
			createMetadata(file.getHeader());

			Row trow = null;
			int rowNum = 0;
			while( (trow = file.getNextRow()) != null )
			{
				rowNum++;

				Data target = null;
				String idCell = trow.get("id");
				if (idCell != null && idCell.trim().length() > 0) 
				{
					target = findExistingData(idCell);
					if (target == null) 
					{
						target = getSearcher().createNewData();
						target.setId(idCell);
					}
				}
				else if ( isMakeId() )
				{
					target = getSearcher().createNewData();
					idCell = getSearcher().nextId();
				}
				else
				{
					target = findExistingRecord(trow);
					if( target == null)
					{
						continue;
					}
				}
				
				addProperties( trow, target);
				target.setId( idCell );
				data.add(target);
				if ( data.size() == 100 )
				{
					getSearcher().saveAllData(data, context.getUser());
					data.clear();
				}
			}
		} 
		finally 
		{
			FileUtils.safeClose(reader);
			getPageManager().removePage(upload);
		}
		getSearcher().saveAllData(data, context.getUser());
	}
	protected Data findExistingRecord(Row inRow)
	{
		return null;
	}
	/** Might be overriden by scripts */
	protected Data findExistingData(String inId )
	{
		return (Data) getSearcher().searchById(inId);
	}
	protected Map<String,Map> getLookUps()
	{
		if( fieldLookUps == null )
		{
			fieldLookUps = new HashMap<String,Map>();
			//fieldLookUps.put("Division", "val_Archive_Division");
		}
		return fieldLookUps;
	}
	protected void createMultiSelect(MultiValued inRow, String inField, String inTable)
	{
		inField = PathUtilities.extractId(inField,true);
		
		String value = inRow.get(inField);
		if( value != null )
		{			
			Map datavalues = loadValueList(inField,inTable,true)
			Collection values = EmStringUtils.split(value);
			List valueids = new ArrayList();
			for (String val: values)
			{
				String id = PathUtilities.extractId(val,true);
				Data data = datavalues.get(id);
				if( data == null )
				{
					//create it
					Searcher searcher = getSearcherManager().getSearcher(getSearcher().getCatalogId(), inTable);
					data = searcher.searchById(id);
					if( data == null )
					{
						data = searcher.createNewData();
						data.setId(id);
						data.setName(val);
						searcher.saveData(data, null);
					}
					datavalues.put(id,  data);
				}
				valueids.add(id); //save it
			}
			inRow.setValues(inField, valueids);
		}

	}

	protected HashMap loadValueList(String inField, String inTableName, boolean inMulti) {
		Map datavalues = getLookUps().get(inField);
		if( datavalues == null )
		{
			datavalues = new HashMap();
			getLookUps().put( inField, datavalues);
			String id = PathUtilities.extractId(inField,true);
			PropertyDetails details = getSearcher().getPropertyDetails()
			PropertyDetail	detail = details.getDetail(id);
			//if( detail.getL
			if( detail.getDataType() != "list")
			{
				detail.setDataType("list");
				detail.setListId(inTableName);
				if( inMulti )
				{
					detail.setViewType("multiselect");
				}
				getSearcher().getPropertyDetailsArchive().savePropertyDetails(details, getSearcher().getSearchType(), context.getUser());
			}
				

		}
		return datavalues
	}
	protected void createLookUp(Data inRow, String inField, String inTable)
	{
		inField = PathUtilities.extractId(inField,true);
		String value = inRow.get(inField);
		if( value != null )
		{
			int comma = value.indexOf(",");
			if( comma > 0 )
			{
				value = value.substring(0,comma);
			}
			Map datavalues = loadValueList(inField,inTable,false)
			Data data = datavalues.get(value);
			if( data == null )
			{
				//create it
				String id = PathUtilities.extractId(value,true);
				Searcher searcher = getSearcherManager().getSearcher(getSearcher().getCatalogId(), inTable);
				data = searcher.searchById(id);
				if( data == null )
				{
					data = searcher.createNewData();
					data.setId(id);
					data.setName(value);
					searcher.saveData(data, null);
				}
				datavalues.put(value,  data);
			}
			inRow.setProperty(inField, data.id);
		} 
	}
	
	protected void createMetadata(Header inHeader)
	{
		PropertyDetails details = getSearcher().getPropertyDetails();
		
		for (Iterator iterator = inHeader.getHeaderNames().iterator(); iterator.hasNext();)
		{
			String header = (String)iterator.next();
			//String header = inHeaders[i];
			String id = PathUtilities.extractId(header,true);
			PropertyDetail detail = details.getDetail(id);
			if( detail == null )
			{
				detail = new PropertyDetail();
				detail.setId(id);
				detail.setText(header);
				detail.setEditable(true);
				detail.setIndex(true);
				detail.setStored(true);
				detail.setCatalogId(getSearcher().getCatalogId() );
				details.addDetail(detail);
				getSearcher().getPropertyDetailsArchive().savePropertyDetails(details, getSearcher().getSearchType(), context.getUser());
			}
		}
	}

	protected void addProperties(Row inRow, Data inData) 
	{
		for (int i = 0; i < inRow.getData().length; i++)
		{
			String val = inRow.getData(i);
			String header = inRow.getHeader().getColumn(i);
			String headerid = PathUtilities.extractId(header,true);

			val = URLUtilities.escapeUtf8(val);  //The XML parser will clean up the & and stuff when it saves it
			if("sourcepath".equals(header))
			{
				inData.setSourcePath(val);
			}
			else if (val != null && val.length() > 0) 
			{
				inData.setProperty(headerid, val);
			}
		}
	}
	
	public boolean isMakeId()
	{
		return fieldMakeId;
	}
	public void setMakeId(boolean inVal)
	{
		fieldMakeId = inVal;
	}
}
