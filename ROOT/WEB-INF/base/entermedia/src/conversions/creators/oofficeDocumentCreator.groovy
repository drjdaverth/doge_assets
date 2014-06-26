package conversions.creators;

import org.apache.commons.logging.Log
import org.apache.commons.logging.LogFactory
import org.openedit.entermedia.Asset
import org.openedit.entermedia.MediaArchive
import org.openedit.entermedia.creator.BaseCreator
import org.openedit.entermedia.creator.ConvertInstructions
import org.openedit.entermedia.creator.ConvertResult

import com.openedit.OpenEditException
import com.openedit.page.Page
import com.openedit.util.ExecResult
import com.openedit.util.PathUtilities

public class oofficeDocumentCreator extends BaseCreator
{
	protected final def formats = ["doc","docx","rtf","ppt","pptx","wps","odt","html","xml","csv", "xls", "xlsx", "odp"];
	private static final Log log = LogFactory.getLog(this.class);
	
	public boolean canReadIn(MediaArchive inArchive, String inInputType)
	{
		for (int i = 0; i < formats.size(); i++)
		{
			if( inInputType.equals(formats.get(i)))
			{
				return true;
			}
		}
		return false;
	}
	
	public synchronized ConvertResult convert(MediaArchive inArchive, Asset inAsset, Page inOut, ConvertInstructions inStructions)
	{
		ConvertResult result = new ConvertResult();
		result.setOk(false);
		
		Page input = inArchive.findOriginalMediaByType("document",inAsset);
		if( input == null )
		{
			return result;
		}

		List command = new ArrayList();
		
		command.add("-headless");
		command.add("-nologo");
		//command.add("-invisible");
		command.add("-norestore");		
		
		command.add("-convert-to");		
		command.add("pdf:writer_pdf_Export");

		command.add("-outdir");
		String dir = inOut.getDirectory();
		log.info("{$inOut} turns into ${dir}");
		dir = getPageManager().getPage(dir).getContentItem().getAbsolutePath();
		new File( dir ).mkdirs();
		command.add(dir);
		
		command.add(input.getContentItem().getAbsolutePath());
		
		ExecResult done = getExec().runExec("soffice",command);
		
		result.setOk(done.isRunOk());
		if( done.isRunOk() )
		{
			String newname = PathUtilities.extractPageName(input.getName()) + ".pdf";
			Page tmpfile = getPageManager().getPage(inOut.getDirectory() + "/" + newname);
			if( !tmpfile.exists())
			{
				throw new OpenEditException("OpenOffice did not create output file " + tmpfile);
			}
			getPageManager().movePage(tmpfile, inOut);
		}
	    result.setOk(true);
	    return result;
	}

	public String populateOutputPath(MediaArchive inArchive, ConvertInstructions inStructions)
	{
		//we only generate PDF for now
		StringBuffer path = new StringBuffer();

		//legacy for people who want to keep their images in the old location
		String prefix = inStructions.getProperty("pathprefix");
		if( prefix != null)
		{
			path.append(prefix);
		}
		else
		{
			path.append("/WEB-INF/data");
			path.append(inArchive.getCatalogHome());
			path.append("/generated/");
		}
		path.append(inStructions.getAssetSourcePath());
		path.append("/");

		String postfix = inStructions.getProperty("pathpostfix");
		if( postfix != null)
		{
			path.append(postfix);
		}	
		inStructions.setOutputExtension("pdf");
		path.append("document." + inStructions.getOutputExtension());

		inStructions.setOutputPath(path.toString());
		return path.toString();
	}

}
