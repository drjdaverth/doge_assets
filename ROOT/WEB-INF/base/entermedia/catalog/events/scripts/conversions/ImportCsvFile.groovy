import model.importer.BaseImporter
import org.openedit.Data;
import org.openedit.data.Searcher;
import org.openedit.entermedia.util.Row;

class CsvImporter extends BaseImporter
{
	/**
	 * This is an example of making a field lower case
	 */
	protected void addProperties( Row inRow, Data inData)
	{
		super.addProperties( inRow, inData);
		//createLookUp(inSearcher.getCatalogId(),inData,"Division","val_divisions");
	}

}


CsvImporter csvimporter = new CsvImporter();
csvimporter.setModuleManager(moduleManager);
csvimporter.setContext(context);
csvimporter.setMakeId(false);
csvimporter.importData();
