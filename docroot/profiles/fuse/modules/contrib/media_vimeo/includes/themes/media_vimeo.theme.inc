<?php

/**
 * @file media_vimeo/includes/themes/media_vimeo.theme.inc
 *
 * Theme and preprocess functions for Media: Vimeo.
 */

/**
 * Preprocess function for theme('media_vimeo_video').
 */
function media_vimeo_preprocess_media_vimeo_video(&$variables) {

  // Build the URI.
  $wrapper = file_stream_wrapper_get_instance_by_uri($variables['uri']);
  $parts = $wrapper->get_parameters();
  $variables['video_id'] = $parts['v'];

  // Parse options and build the query string. Only add the option to the query
  // array if the option value is not default. Be careful, depending on the
  // wording in media_vimeo.formatters.inc, TRUE may be query=0.
  // @see http://developer.vimeo.com/player/embedding
  $query = array();

  // These queries default to 0. If the option is true, set value to 1.
  foreach (array('api', 'autoplay', 'loop') as $option) {
    if ($variables['options'][$option]) {
      $query[$option] = 1;
    }
  }
  // Add a query player ID and identical html ID if js API is set.
  if ($variables['options']['api']) {
    $query['player_id'] = drupal_html_id('media-vimeo-' . $variables['video_id']);
    $variables['api_id_attribute'] = 'id="' . $query['player_id'] . '" ';
  }
  else {
    $variables['api_id_attribute'] = NULL;
  }

  // These queries default to 1. If the option is false, set value to 0.
  foreach (array('portrait', 'title', 'byline') as $option) {
    if (!$variables['options'][$option]) {
      $query[$option] = 0;
    }
  }

  // Strings.
  if (isset($variables['options']['color'])) {
    $query['color'] = str_replace('#', '', $variables['options']['color']);
  }

  // Non-query options.
  $protocol = $variables['options']['protocol'];

  // Add some options as their own template variables.
  foreach (array('width', 'height') as $themevar) {
    $variables[$themevar] = $variables['options'][$themevar];
  }

  // Build the iframe URL with options query string.
  $variables['url'] = url($protocol . 'player.vimeo.com/video/' . $variables['video_id'], array('query' => $query, 'external' => TRUE));
}
