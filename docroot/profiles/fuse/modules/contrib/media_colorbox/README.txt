INSTALLATION
------------
1. Place the media_colorbox directory into your Drupal modules
   directory (normally sites/all/modules).

2. Enable the module by navigating to:

     Administration » Modules

CONFIGURATION
-------------

1. For your configured media field, go to "Manage display" for the content type 
   with that field and choose the "Media Colorbox" formatter, and configure it. 
   Choose the view mode of the file using "File view mode" for how the media file 
   will be displayed on the page, and the view mode used within Colorbox using 
   "Colorbox view mode".

2. If you are using the Media module to display fields, configure the chosen
   view modes by navigating to:

   Administration » Configuration » Media » File types » Manage display
   
   Select the 'Colorbox Link' formatter for an image or text field, this will create 
   a Colorbox link to the entity displaying the 'Colorbox view mode' inside the 
   Colorbox.
   
   The 'Colorbox Link' formatter is only available for file entities (not node, user, 
   taxonomy or any other type of entity).  Therefore, it is not very useful on any but 
   'Default' view mode for the file type being configured - unless you use the 'Rendered file' 
   formatter for a display setting and select a view mode that uses 'Colorbox Link'.

   If you are directly using a file field, configure the chosen view modes.
   You may want to create additional view modes using hook_entity_info_alter()
   or a module like Display suite.
   
Colorbox Captions
-----------------

You may configure the Media Colorbox field formatter to use a text or text_long field of 
the Media file (file entity) being displayed as the caption in the Colorbox overlay.  From 
the admin/config/media/file-types page select the 'manage display' link for any of the file
types and 

KNOWN ISSUES
------------

The default theme implementation of theme_image_style() does not always include "width"
and "height" attributes on the IMG tag. This causes colorbox to resize to zero.
Override the theme_image_style() to include image dimensions, e.g. with :

/**
 * Override theme_image_style().
 */
function THEME_image_style($variables) {
  $style_name = $variables['style_name'];
  $path = $variables['path'];

  $style_path = image_style_path($style_name, $path);
  if (!file_exists($style_path)) {
    $style_path = image_style_url($style_name, $path);
  }
  $variables['path'] = $style_path;

  if (is_file($style_path)) {
    if (list($width, $height, $type, $attributes) = @getimagesize($style_path)) {
      $variables['width'] = $width;
      $variables['height'] = $height;
    }
  }
  
  return theme('image', $variables);
}
