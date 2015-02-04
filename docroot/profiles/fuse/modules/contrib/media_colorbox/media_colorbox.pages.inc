<?php
/**
 * @file
 * Media Colorbox pages callbacks.
 */

/**
 * Menu callback; Displays a given file inside a colorbox.
 */
function media_colorbox_callback($file, $view_mode, $langcode) {
  if (!user_access('administer files') && (file_uri_scheme($file->uri) === 'private')) {
    return MENU_ACCESS_DENIED;
  }
  
  $fview = file_view($file, $view_mode, $langcode);
  $text = drupal_render($fview);
  print $text;
}
