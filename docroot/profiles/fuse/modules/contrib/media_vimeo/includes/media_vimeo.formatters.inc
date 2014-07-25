<?php

/**
 * @file
 */

/**
 * Implements hook_file_formatter_info().
 */
function media_vimeo_file_formatter_info() {
  $formatters['media_vimeo_video'] = array(
    'label' => t('Vimeo Video'),
    'file types' => array('video'),
    'default settings' => array(),
    'view callback' => 'media_vimeo_file_formatter_video_view',
    'settings callback' => 'media_vimeo_file_formatter_video_settings',
  );

  foreach (array('width', 'height', 'preview_uri', 'api', 'autoplay', 'byline', 'color', 'loop', 'portrait', 'title', 'protocol') as $setting) {
    $formatters['media_vimeo_video']['default settings'][$setting] = media_vimeo_variable_get($setting);
  }

  $formatters['media_vimeo_image'] = array(
    'label' => t('Vimeo Preview Image'),
    'file types' => array('video'),
    'default settings' => array(
      'image_style' => '',
    ),
    'view callback' => 'media_vimeo_file_formatter_image_view',
    'settings callback' => 'media_vimeo_file_formatter_image_settings',
  );
  return $formatters;
}

/**
 * Implements hook_file_formatter_FORMATTER_view().
 */
function media_vimeo_file_formatter_video_view($file, $display, $langcode) {
  $scheme = file_uri_scheme($file->uri);
  // WYSIWYG does not yet support video inside a running editor instance.
  if ($scheme == 'vimeo' && empty($file->override['wysiwyg'])) {
    $element = array(
      '#theme' => 'media_vimeo_video',
      '#uri' => $file->uri,
      '#options' => array(),
    );
    foreach (array('width', 'height', 'api' , 'autoplay', 'byline', 'color', 'loop', 'portrait', 'title', 'protocol') as $setting) {
      $element['#options'][$setting] = isset($file->override[$setting]) ? $file->override[$setting] : $display['settings'][$setting];
    }
    return $element;
  }
}

/**
 * Implements hook_file_formatter_FORMATTER_settings().
 */
function media_vimeo_file_formatter_video_settings($form, &$form_state, $settings) {
  $element = array();

  $element['width'] = array(
    '#title' => t('Width'),
    '#type' => 'textfield',
    '#default_value' => $settings['width'],
    '#element_validate' => array('_vimeo_validate_video_width_and_height'),
  );
  $element['height'] = array(
    '#title' => t('Height'),
    '#type' => 'textfield',
    '#default_value' => $settings['height'],
    '#element_validate' => array('_vimeo_validate_video_width_and_height'),
  );

  // @see http://developer.vimeo.com/player/embedding

  // Multiple and string options.
  $element['color'] = array(
    '#title' => t('Custom video controls color'),
    '#type' => 'textfield',
    '#default_value' => $settings['color'],
    '#description' => t('A 3 or 6 character hex color code'),
    '#maxlength' => 7,
    '#size' => 7,
    '#element_validate' => array('_vimeo_validate_video_color'),
  );
  $element['protocol'] = array(
    '#title' => t('Iframe protocol'),
    '#type' => 'radios',
    '#options' => array(
      'http://' => t('http://'),
      'https://' => t('https://'),
      '//' => t('Relative protocol (may fail if content is viewed outside of a web browser, e.g., email and RSS)'),
    ),
    '#default_value' => $settings['protocol'],
    '#description' => t('There may be issues with the player if the iframe protocol and page protocol do not match.'),
  );

  // Single options.
  // Note: make sure the positive/negitive language lines up with option
  // processing in media_vimeo.theme.inc.
  $element['autoplay'] = array(
    '#title' => t('Autoplay video on load'),
    '#type' => 'checkbox',
    '#default_value' => $settings['autoplay'],
  );
  $element['loop'] = array(
    '#title' => t('Loop video'),
    '#type' => 'checkbox',
    '#default_value' => $settings['loop'],
  );
  $element['title'] = array(
    '#title' => t('Show the video title'),
    '#type' => 'checkbox',
    '#default_value' => $settings['title'],
  );
  $element['byline'] = array(
    '#title' => t('Show the video byline'),
    '#type' => 'checkbox',
    '#default_value' => $settings['byline'],
  );
  $element['portrait'] = array(
    '#title' => t("Show the user's portrait"),
    '#type' => 'checkbox',
    '#default_value' => $settings['portrait'],
  );
  $element['api'] = array(
    '#title' => t('Enable the <a href="http://developer.vimeo.com/player/js-api">Javascript API</a>'),
    '#type' => 'checkbox',
    '#default_value' => $settings['api'],
    '#description' => t('A required id in the format <code>media-vimeo-{video_id}</code> will be added to each iframe.'),
  );

  return $element;
}

/**
 * Validation for width and height.
 */
function _vimeo_validate_video_width_and_height($element, &$form_state, $form) {

  // Check if the value is a number with an optional decimal or percentage sign, or "auto".
  if (!empty($element['#value']) && !preg_match('/^(auto|([0-9]*(\.[0-9]+)?%?))$/', $element['#value'])) {
    form_error($element, t("The value entered for @dimension is invalid. Please insert a unitless integer for pixels, a percent, or \"auto\". Note that percent and auto may not function correctly depending on the browser and doctype.", array('@dimension' => $element['#title'])));
  }
}

/**
 * Validation for color.
 */
function _vimeo_validate_video_color($element, &$form_state, $form) {

  // If the value is set, check if it's a hex color with or without '#'.
 if (!empty($element['#value']) && !preg_match('/^#?([0-9a-fA-F]{3}|[0-9a-fA-F]{6})$/', $element['#value'])) {
    form_error($element, t("The color value is invalid. Please use a 3 or 6 character hex color code.", array('@dimension' => $element['#title'])));
  }
}

/**
 * Implements hook_file_formatter_FORMATTER_view().
 */
function media_vimeo_file_formatter_image_view($file, $display, $langcode) {
  $scheme = file_uri_scheme($file->uri);
  if ($scheme == 'vimeo') {
    $wrapper = file_stream_wrapper_get_instance_by_uri($file->uri);
    $image_style = $display['settings']['image_style'];
    $valid_image_styles = image_style_options(FALSE);
    if (empty($image_style) || !isset($valid_image_styles[$image_style])) {
      $element = array(
        '#theme' => 'image',
        '#path' => $wrapper->getOriginalThumbnailPath(),
        '#alt' => $file->filename,
      );
    }
    else {
      $element = array(
        '#theme' => 'image_style',
        '#style_name' => $image_style,
        '#path' => $wrapper->getLocalThumbnailPath(),
        '#alt' => $file->filename,
      );
    }
    return $element;
  }
}

/**
 * Implements hook_file_formatter_FORMATTER_settings().
 */
function media_vimeo_file_formatter_image_settings($form, &$form_state, $settings) {
  $element = array();
  $element['image_style'] = array(
    '#title' => t('Image style'),
    '#type' => 'select',
    '#options' => image_style_options(FALSE),
    '#default_value' => $settings['image_style'],
    '#empty_option' => t('None (original image)'),
  );
  return $element;
}

/**
 * Implements hook_file_default_displays().
 */
function media_vimeo_file_default_displays() {

  // Default settings for displaying as a video.
  $default_displays = array();

  $default_video_settings = array(
    'default' => array(
      'width' => 640,
      'height' => 390,
    ),
    'teaser' => array(
      'width' => 560,
      'height' => 340,
      'autoplay' => FALSE,
    ),
    // Legacy view modes included to support older versions of Media,
    // before #1051090 went through. They do no harm and can sit here
    // until there is a Media 2.x stable.
    // @TODO: Remove when Media 2.0 is released.
    'media_large' => array(
      'width' => 560,
      'height' => 340,
      'autoplay' => FALSE,
    ),
    'media_original' => array(
      'width' => 640,
      'height' => 390,
      'autoplay' => media_vimeo_variable_get('autoplay'),
    ),
  );
  foreach ($default_video_settings as $view_mode => $settings) {
    $display_name = 'video__' . $view_mode . '__media_vimeo_video';
    $default_displays[$display_name] = (object) array(
      'api_version' => 1,
      'name' => $display_name,
      'status' => 1,
      'weight' => 1,
      'settings' => $settings,
    );
  }

  // Default settings for displaying a video preview image.
  // We enable preview images even for view modes that also play video
  // for use inside a running WYSIWYG editor. We weight them so video 
  // formatters come first in the cascade to make sure the video formatter
  // is used whenever possible.
  $default_image_styles = array(
    'default' => 'large',
    'preview' => 'media_thumbnail',
    'teaser' => 'large',
    // Legacy view modes, see note above.
    'media_preview' => 'media_thumbnail',
    'media_large' => 'large',
    'media_original' => '',
  );
  foreach ($default_image_styles as $view_mode => $image_style) {
    $display_name = 'video__' . $view_mode . '__media_vimeo_image';
    $default_displays[$display_name] = (object) array(
      'api_version' => 1,
      'name' => $display_name,
      'status' => 1,
      'weight' => 2,
      'settings' => array('image_style' => $image_style),
    );
  }

  return $default_displays;
}
