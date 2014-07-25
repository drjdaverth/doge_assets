<?php

/**
 * @file
 * Input filter that enhances oEmbed enabled URLs with extra content
 */

define('OEMBED_PATTERN_AUTOEMBED', '|^\s*(https?://[^\s"]+)\s*$|im');
define('OEMBED_PATTERN_EMBED_SHORTCODE', '/(.?)\[embed\b(.*?)\](.+?)\[\/embed\](.?)/s');
define('OEMBED_PATTERN_EMBED_UNWRAP', '/<p>\s*+(\[embed\b.*?\].+?\[\/embed\])\s*+<\/p>/s');

/**
 * Implements hook_filter_info().
 */
function oembed_filter_info() {
  $filters['oembed'] = array(
    'title' => t('oEmbed filter'),
    'description' => t('Embeds content for oEmbed-enabled web addresses and turns the rest, and e-mail addresses, into clickable links.'),
    'prepare callback' => 'oembed_filter_oembed_prepare',
    'process callback' => 'oembed_filter_oembed_process',
    'settings callback' => 'oembed_filter_oembed_settings',
    'tips callback' => 'oembed_filter_oembed_tips',
    'default settings' => array(
      'options' => '',
      'view_mode' => 'full',
      'autoembed' => TRUE,
    ),
  );
  $filters['oembed_legacy'] = array(
    'title' => t('oEmbed legacy filter'),
    'description' => t('Embeds content for oEmbed-enabled web addresses and turns the rest, and e-mail addresses, into clickable links.'),
    'process callback' => 'oembed_filter_oembed_legacy_process',
    'settings callback' => 'oembed_filter_oembed_legacy_settings',
    'default settings' => array(
      'maxwidth' => '',
      'maxheight' => '',
    ),
  );
  return $filters;
}

/**
 * Implements hook_filter_FILTER_settings().
 */
function oembed_filter_oembed_legacy_settings($form, &$form_state, $filter, $format, $defaults, $filters) {
  module_load_include('inc', 'oembed', 'oembed_legacy');
  return _oembed_filter_settings($form, $form_state, $filter, $format, $defaults);
}

/**
 * Implements hook_filter_FILTER_process().
 */
function oembed_filter_oembed_legacy_process($text, $filter, $format, $langcode, $cache, $cache_id) {
  module_load_include('inc', 'oembed', 'oembed_legacy');
  return _oembed_filter_apply($text, $filter, $format, $langcode, $cache, $cache_id);
}

/**
 * Implements hook_filter_FILTER_settings().
 */
function oembed_filter_oembed_settings($form, &$form_state, $filter, $format, $defaults, $filters) {
  $filter->settings += $defaults;
  $settings = array();

  if (module_exists('file_entity') && module_exists('inline')) {
    $options = array();
    $entity_info = entity_get_info('file');
    $view_modes = $entity_info['view modes'];
    foreach ($view_modes as $view_mode_name => $view_mode_info) {
      $options[$view_mode_name] = $view_mode_info['label'];
    }
    if (empty($options)) {
      $options['full'] = t('Full');
    }
    $settings['view_mode'] = array(
      '#type' => 'select',
      '#options' => $options,
      '#title' => t('File entity view mode'),
      '#default_value' => $filter->settings['view_mode'],
    );
  }
  else {
    $settings['view_mode'] = array(
      '#type' => 'value',
      '#value' => $filter->settings['view_mode'],
    );
  }
  $settings['options'] = array(
    '#type' => 'textfield',
    '#title' => t('Default oEmbed request options'),
    '#default_value' => $filter->settings['options'],
    '#description' => t('A series of attribute value pairs for the default request options. For example, <em>maxwidth="500"</em>.'),
  );
  $settings['autoembed'] = array(
    '#type' => 'checkbox',
    '#title' => t('Automatically embed URLs'),
    '#default_value' => $filter->settings['autoembed'],
    '#description' => t('When possible, embed the media content from a URL directly in the input.'),
  );
  return $settings;
}

/**
 * Implements hook_filter_FILTER_process().
 */
function oembed_filter_oembed_prepare($text, $filter, $format, $langcode, $cache, $cache_id) {
  if ($filter->settings['autoembed']) {
    $text = preg_replace_callback(OEMBED_PATTERN_AUTOEMBED, 'oembed_preg_auto_replace', $text);
  }
  return $text;
}

/**
 * Implements hook_filter_FILTER_process().
 */
function oembed_filter_oembed_process($text, $filter, $format, $langcode, $cache, $cache_id) {
  global $_oembed_filter_settings;

  $_oembed_filter_settings = !empty($filter->settings['options']) ? oembed_parse_attr($filter->settings['options']) : array();
  $_oembed_filter_settings['view_mode'] = $filter->settings['view_mode'];

  // Undo auto paragraph around oEmbed shortcodes.
  $text = preg_replace(OEMBED_PATTERN_EMBED_UNWRAP, '$1', $text);

  $text = preg_replace_callback(OEMBED_PATTERN_EMBED_SHORTCODE, 'oembed_preg_tag_replace', $text);

  unset($_oembed_filter_settings);

  return $text;
}

/**
 * Implements hook_filter_FILTER_tips().
 */
function oembed_filter_oembed_tips($filter, $format, $long) {
  if ($long) {
    return t('Embed content by wrapping a supported URL in [embed] &hellip; [/embed]. Set options such as width and height with attributes [embed width="123" height="456"] &hellip; [/embed]. Unsupported options will be ignored.');
  }
  else {
    return t('Embed content by wrapping a supported URL in [embed] &hellip; [/embed].');
  }
}

/**
 * PREG replace callback finds [embed] shortcodes, URLs and request options.
 */
function oembed_preg_tag_replace($match) {
  global $_oembed_filter_settings;

  // allow [[oembed]] syntax for escaping a tag
  if ($match[1] == '[' && $match[4] == ']') {
    return substr($match[0], 1, -1);
  }

  $url = $match[3];

  $view_mode = $_oembed_filter_settings['view_mode'];

  $shortcode_options = !empty($match[2]) ? oembed_parse_attr($match[2]) : array();
  $options = array_merge($_oembed_filter_settings, $shortcode_options);

  if (isset($options['view_mode'])) {
    $view_mode = $options['view_mode'];
    unset($options['view_mode']);
  }

  return $match[1] . oembed_resolve_link($url, $view_mode, $options) . $match[4];
}

/**
 * PREG replace callback finds URLs
 */
function oembed_preg_auto_replace($match) {
  return '[embed]'. $match[1] ."[/embed]\n";
}

/**
 * PREG replace callback finds [embed] shortcodes, URLs and request options.
 *
 * @see MediaInternetOEmbedHandler::preSave().
 */
function oembed_resolve_link($url, $view_mode = 'full', $options = array()) {

  $url = decode_entities($url);

  $element = array();
  $matches = array();

  // If file_entity module is enabled, treat the URL as an uploaded file. Inline is used
  // to defer the rendering of the embedded content until the entity is actually viewed.
  // This technique allows content to be cached by Drupal's filter system.
  if (module_exists('file_entity') && module_exists('inline')) {
    $file = oembed_url_to_file($url);
    $file->override = $options;
    if (isset($file->fid)) {
      $macro_params = array();
      $macro_params[] = 'entity';
      $macro_params[] = 'type=file';
      $macro_params[] = 'id='. $file->fid;
      $macro_params[] = 'view_mode='. $view_mode;
      $element = array('#markup' => "\r\n".'[' . implode('|', $macro_params) . ']'."\r\n");
    }
  }
  else if ($provider = oembed_get_provider($url, $matches)) {
    $embed = oembed_get_data($url);
    if ($embed) {
      $element = oembed_render_element('oembed', $url, $options);
    }
  }

  $return = drupal_render($element);

  if (empty($return)) {
    $return = $url;
  }

  return $return;
}

/**
 * Retrieve all attributes from the shortcodes tag.
 *
 * @see shortcode_parse_atts in WordPress 3.1.3.
 * @param string $text
 * @return array List of attributes and their value.
 */
function oembed_parse_attr($text) {
  $attributes = array();
  $pattern = '/(\w+)\s*=\s*"([^"]*)"(?:\s|$)|(\w+)\s*=\s*\'([^\']*)\'(?:\s|$)|(\w+)\s*=\s*([^\s\'"]+)(?:\s|$)|"([^"]*)"(?:\s|$)|(\S+)(?:\s|$)/';
  $text = preg_replace("/[\x{00a0}\x{200b}]+/u", " ", $text);
  if (preg_match_all($pattern, $text, $matches, PREG_SET_ORDER)) {
    foreach ($matches as $match) {
      if (!empty($match[1])) {
        $attributes[strtolower($match[1])] = stripcslashes($match[2]);
      }
      elseif (!empty($match[3])) {
        $attributes[strtolower($match[3])] = stripcslashes($match[4]);
      }
      elseif (!empty($match[5])) {
        $attributes[strtolower($match[5])] = stripcslashes($match[6]);
      }
      elseif (isset($match[7]) and strlen($match[7])) {
        $attributes[] = stripcslashes($match[7]);
      }
      elseif (isset($match[8])) {
        $attributes[] = stripcslashes($match[8]);
      }
    }
  } else {
    $attributes = ltrim($text);
  }
  return $attributes;
}

/**
 * Extract all URLs for oEmbed to process.
 *
 * Returns an array of URLs grouped by field, delta, and column.
 */
function _oembed_field_extract_urls($entity_type, $entity) {
  $urls = array();

  // Determine if any formats use oEmbed filter.
  $filter_settings = array();
  foreach (filter_formats() as $format) {
    $filters = filter_list_format($format->format);
    if (isset($filters['oembed']) && $filters['oembed']->status) {
      $filter_settings[$format->format] = $filters['oembed']->settings;
    }
  }

  if (!empty($filter_settings)) {

    list(, , $bundle) = entity_extract_ids($entity_type, $entity);
    $instances = field_info_instances($entity_type, $bundle);

    foreach ($instances as $info) {

      // All text fields have a text_processing setting. Only search text fields with
      // text processing enabled.
      if (isset($info['settings']['text_processing']) && $info['settings']['text_processing']) {
        $items = field_get_items($entity_type, $entity, $info['field_name']);
        if (!$items) {
          continue;
        }
        foreach ($items as $delta => $item) {
          if (isset($filter_settings[$item['format']])) {

            // URLs may be contained within the other column values.
            foreach (array('value', 'summary') as $column) {
              if (!empty($item[$column])) {
                $text = $item[$column];

                // copied from oembed_filter_oembed_prepare().
                if ($filter_settings[$item['format']]['autoembed']) {
                  $text = preg_replace_callback(OEMBED_PATTERN_AUTOEMBED, 'oembed_preg_auto_replace', $text);
                }

                // copied from oembed_filter_oembed_process().
                $matches = array();
                preg_match_all(OEMBED_PATTERN_EMBED_SHORTCODE, $text, $matches);
                $urls[$info['field_name']][$delta][$column] = array_filter($matches[3], '_oembed_field_filter_urls');
              }
            }
          }
        }
      }
    }
  }

  return $urls;
}

/**
 * array_filter() callback that removes URLs for which there is no provider.
 */
function _oembed_field_filter_urls($match) {
  $matches = array();
  if (oembed_get_provider($match, $matches)) {
    return TRUE;
  }
  return FALSE;
}

/**
 * Implements hook_field_attach_validate().
 */
function oembed_field_attach_validate($entity_type, $entity, array &$errors) {
  foreach (_oembed_field_extract_urls($entity_type, $entity) as $field_name => $items) {
    foreach ($items as $delta => $item) {
      foreach ($item as $column => $urls) {
        $messages = array();
        foreach ($urls as $url) {
          $embed = oembed_get_data($url);
          $validation_errors = oembed_validate_response($embed);
          if (!empty($validation_errors)) {
            $message = t('!url could not be embedded.', array('!url' => l(_filter_url_trim($url, 50), $url)));
            $message .= theme('item_list', array('items' => $validation_errors));
            $messages[] = $message;
          }
        }
        if (!empty($messages)) {
          $errors[$field_name][$entity->language][$delta][] = array(
            'error' => 'oembed_'. $column,
            'message' => theme('item_list', array('items' => $messages)),
            'repeat' => TRUE,
          );
        }
      }
    }
  }
}

/**
 * Implements hook_entity_insert().
 */
function oembed_entity_insert($entity, $entity_type) {
  if (module_exists('file_entity')) {
    list($id, , ) = entity_extract_ids($entity_type, $entity);
    $uris = array();
    foreach (_oembed_field_extract_urls($entity_type, $entity) as $items) {
      foreach ($items as $item) {
        foreach ($item as $urls) {
          foreach ($urls as $url) {
            $uris[$url] = isset($uris[$url]) ? $uris[$url] + 1 : 1;
          }
        }
      }
    }

    foreach ($uris as $url => $count) {
      $file = oembed_url_to_file($url, TRUE);
      file_usage_delete($file, 'oembed', $entity_type, $id, 0);
      file_usage_add($file, 'oembed', $entity_type, $id, $count);
    }
  }
}

function oembed_entity_update($entity, $entity_type) {
  oembed_entity_insert($entity, $entity_type);
}

