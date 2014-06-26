package sql;
/*
 * Created on Oct 2, 2005
 */
//package org.openedit.entermedia.model;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.Statement;

import org.openedit.entermedia.BaseEnterMediaTest;
import org.openedit.entermedia.ConvertStatus;
import org.openedit.entermedia.MediaArchive;
import org.openedit.entermedia.scanner.SqlImportConverter;

public class SqlImportTest extends BaseEnterMediaTest
{
	public SqlImportTest(String inArg0)
	{
		super(inArg0);
	}

	public void xtestConnect() throws Exception
	{
		/*
		 * Class.forName("net.sourceforge.jtds.jdbc.Driver"); Connection con =
		 * DriverManager.getConnection("jdbc:jtds:sqlserver://10.35.39.213:1433","username",
		 * "password"); Statement stmt = con.createStatement(); ResultSet rs =
		 * stmt.executeQuery("SELECT * FROM SFXLib");
		 */
//		String url = "jdbc:ashpool://file://c:/myxml";
		String url = "jdbc:jtds:sqlserver://10.35.39.213:1433/mSoftLib;user=openedit;password=openedit;tds=4.2";

			Driver driver = (Driver) Class.forName("com.rohanclan.ashpool.jdbc.Driver").newInstance();
			DriverManager.registerDriver(driver);
			Connection conn = DriverManager.getConnection(url, null);
			Statement stmt = conn.createStatement();
			java.sql.ResultSet rs;
			rs = stmt.executeQuery("select * FROM SFXlib;");
			assertNotNull(rs);
	}

	public void testSync() throws Exception
	{
		/*
		 * Class.forName("net.sourceforge.jtds.jdbc.Driver"); Connection con =
		 * DriverManager.getConnection("jdbc:jtds:sqlserver://10.35.39.213:1433","username",
		 * "password"); Statement stmt = con.createStatement(); ResultSet rs =
		 * stmt.executeQuery("SELECT * FROM SFXLib");
		 */
		SqlImportConverter conv = (SqlImportConverter)getBean("SqlImportConverter");
		conv.setPageManager(getFixture().getPageManager());
		MediaArchive archive = getEnterMedia().getMediaArchive("entermedia/catalogs/testcatalog");		
		
	    conv.importAssets(archive, new ConvertStatus());
	}
}
