import java.io.OutputStream;
import java.sql.Connection
import java.sql.DatabaseMetaData
import java.sql.ResultSet;
import java.sql.ResultSetMetaData
import java.sql.SQLException
import java.sql.Statement

import org.openedit.db.util.ConnectionPool
import org.openedit.entermedia.util.CSVWriter;

import com.openedit.page.Page

import model.importer.BaseImporter

class ImportWithSqlHelper extends BaseImporter
{
	
	public void importTables() throws Exception
	{
		String catalogId = context.findValue("catalogid");
		ConnectionPool pool = (ConnectionPool)getModuleManager().getBean("connectionPool");
	log.info("Connecting");
		Connection con = pool.instance(catalogId);
    log.info("Connected");
		DatabaseMetaData allmd = con.getMetaData();
		ResultSet rs = allmd.getTables(null, null, "%", null);
		while (rs.next()) 
		{
			String table = rs.getString(3);
			try{
				Page page = getPageManager().getPage("/WEB-INF/tmp/exports/" + table + ".csv");
				if( page.exists() )
				{
					log.info("already completed " + table );
					continue;
				}
				Statement st = con.createStatement();
				
				if( 
					table.endsWith("SearchResult") ||
					table.contains("Picker") ||
					table.startsWith("Asset") ||
					table.equals("Contact")  || 
					table.equals("Contact 2")  ||
					table.equals("Contact 3")  ||
					table.startsWith("Publication")  ||
					table.startsWith("Usage")  ||  
					table.startsWith("Index") ||
					table.startsWith("Tag") ||
					
					table.startsWith("Submission") 
				  )
				{
					log.info("skip " + table);
					continue;
				}
				log.info("search " + table);
				ResultSet rows = st.executeQuery("SELECT * FROM \"" + table + "\"");
				ResultSetMetaData md = rs.getMetaData();
				int col = md.getColumnCount();
				log.info( table + "Number of Column : "+ col);
				
				OutputStream out = getPageManager().saveToStream(page);
				CSVWriter writer = new CSVWriter(new OutputStreamWriter(out,"UTF-8") );
				writer.writeAll(rows,true);
				log.info("saved " + table);
				
				writer.close();
			}
			catch (SQLException s)
			{
				log.info(table + " SQL statement is not executed!" + s);
			}
		}
		log.info("Closing");
		pool.close(con);
	}
	
	/*
	protected void addProperties(Searcher inSearcher, Row inRow, Data inData)
	{

		String code = inRow.get("projectcode");
		code = code.toLowerCase();
		inRow.set("projectcode",code);
		super.addProperties(inSearcher, inRow, inData);
	}
	*/
}


ImportWithSqlHelper importer = new ImportWithSqlHelper();
importer.setModuleManager(moduleManager);
importer.setContext(context);
importer.setLog(log);
importer.importTables();
//importer.importData();
