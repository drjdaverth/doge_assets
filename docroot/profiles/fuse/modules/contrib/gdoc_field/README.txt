Drupal Embedded Google Docs Viewer module:
------------------------------------------
Maintainer:
  Jeff Markel (http://drupal.org/user/15171)
Requires - Drupal 7
License - GPL (see LICENSE)

The Embedded Google Docs Viewer is a module which
adds a formatter to core's File field. The formatter
uses Google's embeddable Google Docs viewer to render
Adobe Acrobat pdf files, and Microsoft Word, Excel, and 
Powerpoint files (i.e. files suffixed with .pdf, .doc,
.docx, .xls, .xlsx, .ppt, or .pptx).

After adding a File field to a Drupal content type,
the Embedded Google Docs Viewer formatter may be selected
for use on the conntent type's "Manage Display" tab.

N.B.: Only files that are public may use this formatter - 
Google Docs must be able to access the file in order to
render and display it. In other words, it won't work on 
a typical development laptop, or if your server is
behind a firewall where Google can't access it.