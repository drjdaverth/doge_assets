<?php
/**
 * @file
 * Theme and preprocess functions for the Video.js module.
 */

/**
 * Preprocess function for videojs.tpl.php.
 */
function videojs_preprocess_videojs(&$vars) {
  videojs_add();

  $items = $vars['items'];
  $codecs = array(
    'video/mp4' => 'video/mp4',
    'video/x-m4v' => 'video/mp4',
    'video/webm' => 'video/webm',
    'application/octet-stream' => 'video/webm',
    'video/ogg' => 'video/ogg',
    'application/ogg' => 'video/ogg',
    'video/quicktime' => 'video/mp4',
    'video/flv' => 'video/flv',
    'video/x-flv' => 'video/flv',
    'audio/mpeg' => 'audio/mpeg',
    'audio/ogg' => 'audio/ogg',
    'application/vnd.apple.mpegurl' => 'application/vnd.apple.mpegurl', // .m3u8
  );

  // Add the defaults to $vars['attributes'] and merge with $vars.
  $options = videojs_utility::getDisplaySettings($vars['attributes']);
  $vars = array_merge($vars, $options);
  $vars['items'] = array();
  $vars['tracks'] = array();
  $vars['poster'] = NULL;
  $vars['class'] = '';

  if (!empty($vars['attributes']['class'])) {
    $vars['class'] = implode(' ', $vars['attributes']['class']);
  }

  foreach ($items as $item) {
    
    // Add src variables.
    if (!empty($item['uri'])) {
      $src = file_create_url($item['uri']);
      $item['src'] = array('raw' => $src, 'safe' => check_plain($src));
    }
    
    if (empty($item['filemime'])) {
      continue;
    }

    if (!isset($codecs[$item['filemime']])) {
      // check for image file and add in it as poster
      if (strncmp($item['filemime'], 'image/', 6) === 0) {
        $vars['poster'] = $item['uri'];
      }

      // Filter out tracks.
      if (strncmp($item['filemime'], 'text/vtt', 8) === 0) {
        // Add raw/safe for videotype var.
        $item['filemime'] = array('raw' => $item['filemime'], 'safe' => check_plain($item['filemime']));
        $vars['tracks'][] = $item + array(
          'default' => FALSE,
          'kind' => 'subtitles',
          'charset' => 'utf-8',
          'label' => '',
          'langcode' => '',
        );
      }

      // Skip unplayable items.
      continue;
    }

    // Add raw/safe for videotype var.
    $item['videotype'] = array('raw' => $codecs[$item['filemime']], 'safe' => check_plain($codecs[$item['filemime']]));

    $vars['items'][] = $item;
  }

  // Turn the poster image URI into a URL, if it isn't an URL already.
  if (!empty($vars['poster']) && strncmp($vars['poster'], 'http://', 7) !== 0  && strncmp($vars['poster'], 'https://', 8) !== 0) {
    if (empty($vars['posterimage_style'])) {
      $vars['poster'] = file_create_url($vars['poster']);
    }
    else {
      $vars['poster'] = image_style_url($vars['posterimage_style'], $vars['poster']);
    }
  }
}

/**
 * Preprocess function for videojs-media-wysiwyg-preview.tpl.php.
 */
function videojs_preprocess_videojs_media_wysiwyg_preview(&$vars) {
  videojs_preprocess_videojs($vars);

  // If the video has no poster image, borrow one from the media module.
  // This theme function should only be used by the media module, so assume
  // it is loaded.
  if (empty($vars['poster'])) {
    $poster = drupal_get_path('module', 'media') . '/images/icons/default/video-x-generic.png';
    $raw = file_create_url($poster);
    $vars['poster'] = array('raw' => $raw, 'safe' => check_plain($raw));
  }
}
