Login
(done) Ø  Get Login Session (getkey.xml) – Get the session ID which is used for all other API calls. (the username and password for this session will be configured in REST).
(done) Ø  List Template Categories (listcategories.xml) – list all categories for DPS templates. pictures and templates

Lists
get lists  Upload Status

Search
Ø  Get Listing Images (listimages.xml) – pass in image (asset) ids in query, and return all images with id matched. 
The result should include image metadata, physical filepath, thumbnail and preview URLs.

Ø  Search Images (searchimages.xml) – pass in metadata search conditions in query, and return all images with 
conditions matched. The result should also include metadata, filepath, thumbnail and preview URLs.

Search web service will be used for following.
o    List images based on client id.
o    Search images on some image metadata.
o    Search templates on some template metadata.
o    Search Ads on some Ad metadata.

Get more results by Page

Get details and save details
Ø  Update Metadata – pass image ids and fields to update in query, update metadata fields with values specified for the images matched.
Ø  Upload Asset – post file and default metadata fields in query, it will create a new asset in DAM with the file and default metadata posted.
