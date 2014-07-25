<?php
/**
 * @file
 * This file will be used to keep all utility functions.
 */

/**
 * Helper functions for the Video.js module.
 *
 * These functions are put in a utility class so they will only be
 * auto-loaded when needed.
 */
class videojs_utility {
  public static function getDisplaySettingsForm(array &$element, array $values = NULL) {
    if ($values === NULL) {
      $values = self::getDefaultDisplaySettings();
    }

    $element['width'] = array(
      '#type' => 'textfield',
      '#title' => t('Player width'),
      '#default_value' => !empty($values['width']) ? intval($values['width']) : '',
      '#element_validate' => array('_element_validate_integer_positive'),
      '#maxlength' => 10,
      '#size' => 10,
    );

    $element['height'] = array(
      '#type' => 'textfield',
      '#title' => t('Player height'),
      '#default_value' => !empty($values['height']) ? intval($values['height']) : '',
      '#element_validate' => array('_element_validate_integer_positive'),
      '#maxlength' => 10,
      '#size' => 10,
    );

    $element['autoplay'] = array(
      '#type' => 'checkbox',
      '#title' => t('Auto-play files on page load'),
      '#description' => t('Use caution when combining this option with multiple players on the same page.'),
      '#default_value' => !empty($values['autoplay']),
    );

    $element['loop'] = array(
      '#type' => 'checkbox',
      '#title' => t('Loop playback'),
      '#default_value' => !empty($values['loop']),
    );

    $element['hidecontrols'] = array(
      '#type' => 'checkbox',
      '#title' => t('Hide controls'),
      '#default_value' => !empty($values['hidecontrols']),
    );

    $element['defaulttrack'] = array(
      '#type' => 'select',
      '#title' => t('Default subtitle track'),
      '#options' => array(
        'first' => t('First track'),
        'user' => t('Current user language'),
      ),
      '#empty_value' => '',
      '#empty_option' => t('No default track'),
      '#default_value' => !empty($values['defaulttrack']) ? $values['defaulttrack'] : NULL,
      '#description' => t('When you are using VTT subtitle tracks, select which one should be activated by default.'),
    );

    foreach (language_list() as $language) {
      $languagename = empty($language->native) ? $language->name : $language->native;
      $element['defaulttrack']['#options'][$language->language] = $languagename;
    }

    $element['preload'] = array(
      '#type' => 'select',
      '#title' => t('Preload behavior'),
      '#options' => array(
        'auto' => t('Automatic preloading'),
        'metadata' => t('Only metadata'),
        'none' => t('No preloading'),
      ),
      '#default_value' => !empty($values['preload']) ? $values['preload'] : 'auto',
    );
  }

  public static function getDisplaySettingsFormResults(array $values) {
    return array(
      'loop' => !empty($values['loop']),
      'autoplay' => !empty($values['autoplay']),
      'hidecontrols' => !empty($values['hidecontrols']),
      'width' => !empty($values['width']) ? intval($values['width']) : NULL,
      'height' => !empty($values['height']) ? intval($values['height']) : NULL,
      'preload' => !empty($values['preload']) ? $values['preload'] : NULL,
      'defaulttrack' => !empty($values['defaulttrack']) ? $values['defaulttrack'] : NULL,
    );
  }

  public static function getDefaultDisplaySettings() {
    return array(
      'loop' => variable_get('videojs_loop', FALSE),
      'hidecontrols' => variable_get('videojs_hidecontrols', FALSE),
      'autoplay' => variable_get('videojs_autoplay', FALSE),
      'width' => variable_get('videojs_width', 640),
      'height' => variable_get('videojs_height', 360),
      'preload' => variable_get('videojs_preload', 'auto'),
      'defaulttrack' => variable_get('videojs_defaulttrack', ''),
    );
  }

  public static function getDisplaySettings(array $input = array()) {
    $settings = self::getDefaultDisplaySettings();

    if (empty($input)) {
      return $settings;
    }

    foreach ($input as $k => $v) {
      if ($v !== NULL && array_key_exists($k, $settings)) {
        $settings[$k] = $v;
      }
    }

    return $settings;
  }

  /**
   * Tries to find a language based on the input
   *
   * @param $input
   *   String containing a language code or language name.
   *
   * @return
   *   NULL if nothing found, array with code and name if found.
   */
  public static function resolveLanguage($input) {
    if (empty($input) || !is_string($input)) {
      return NULL;
    }
    $input = trim($input);
    if (strlen($input) < 2) {
      return NULL;
    }

    include_once DRUPAL_ROOT . '/includes/iso.inc';
    $languages = _locale_get_predefined_list();

    // Try to match on language code.
    if (strlen($input) === 2 && isset($languages[strtolower($input)])) {
      $input = strtolower($input);
      return array($input, isset($languages[$input][1]) ? $languages[$input][1] : $languages[$input][0]);
    }

    // Try to match on English or native language name.
    foreach ($languages as $code => $lang) {
      if ($lang[0] === $input || (isset($lang[1]) && $lang[1] === $input)) {
        return array($code, $input);
      }
    }

    return NULL;
  }

  /**
   * Finds image fields in the given entity and bundle.
   *
   * @param $field
   *   Field definition of the video field, used to match image fields when
   *   this field is rendered using Views.
   * @param $entity_type
   *   Entity type in which the image field must occur.
   * @param $bundle
   *   Bundle in which the image field must occur.
   * @return
   *   Array of image field names.
   */
  public static function findFieldsByType($field, $entity_type, $bundle, array $types) {
    $resultfields = array();

    // Determine the image fields that will be selectable.
    if ($entity_type == 'ctools' && $bundle == 'ctools') {
      // This is a fake instance (see ctools_fields_fake_field_instance()).
      // This occurs for instance when this formatter is used in Views.
      // Display all image fields in bundles that contain this field.
      $otherfields = field_info_fields();
      foreach ($otherfields as $otherfield) {
        if (!empty($otherfield['bundles']) && in_array($otherfield['type'], $types)) {
          // Find a label by finding an instance label
          $instancelabels = array();
          $bundles_names = array();

          foreach ($otherfield['bundles'] as $otherentitytype => $otherbundles) {
            foreach ($otherbundles as $otherbundle) {
              // Check if this image field appears in one of the video field bundles.
              if (isset($field['bundles'][$otherentitytype]) && in_array($otherbundle, $field['bundles'][$otherentitytype])) {
                $otherinstance = field_info_instance($otherentitytype, $otherfield['field_name'], $otherbundle);
                $instancelabels[$otherinstance['label']] = isset($instancelabels[$otherinstance['label']]) ? $instancelabels[$otherinstance['label']] + 1 : 1;
                $bundles_names[] = t('@entity:@bundle', array('@entity' => $otherentitytype, '@bundle' => $otherbundle));
              }
            }
          }

          if (!empty($instancelabels)) {
            arsort($instancelabels);
            $instancelabel = key($instancelabels);
            $resultfields[$otherfield['field_name']] = $instancelabel . ' — ' . t('Appears in: @bundles.', array('@bundles' => implode(', ', $bundles_names)));
          }
        }
      }
    }
    else {
      $otherinstances = field_info_instances($entity_type, $bundle);
      foreach ($otherinstances as $otherinstance) {
        $otherfield = field_info_field_by_id($otherinstance['field_id']);
        if (in_array($otherfield['type'], $types)) {
          $resultfields[$otherinstance['field_name']] = $otherinstance['label'];
        }
      }
    }

    return $resultfields;
  }

  /**
   * Normalizes field items so link items and file items become similar.
   *
   * Also fills in missing mime types and optionally filters on mime type.
   */
  public static function normalizeFieldItems($field_name, array &$items, $defaultmime, $mimefilter = NULL) {
    if (empty($items)) {
      return;
    }

    $field = field_info_field($field_name);
    if ($field['type'] == 'link_field') {
      foreach ($items as $key => $link) {
        $title = $link['title'];

        // Allow the user to override the mime type using the title field
        if (!empty($title) && (strncmp('video/', $title, 6) === 0 || strncmp('audio/', $title, 6) === 0 || strncmp('text/', $title, 5) === 0)) {
          $mime = $title;
          $title = '';
        }
        else {
          $mime = DrupalLocalStreamWrapper::getMimeType($link['url']);

          // If the mime type is application/octet-stream, use the default.
          // This happens for instance for links without extensions.
          if ($mime == 'application/octet-stream') {
            $mime = $defaultmime;
          }
        }

        $items[$key] = array(
          'uri' => url($link['url'], $link),
          'filemime' => $mime,
          'description' => $title,
        );
      }
    }
    else {
      // Make sure the item is fully loaded. Image fields of file entities can only have a fid and no additional data.
      foreach ($items as $key => $item) {
        if (isset($item['fid']) && !isset($item['filemime']) && $item['fid'] > 0) {
          $file = file_load($item['fid']);
          $items[$key] = $item + (array)$file;
        }
      }
    }

    if (!empty($mimefilter)) {
      $filterlength = strlen($mimefilter);
      foreach ($items as $k => $item) {
        if (strncmp($item['filemime'], $mimefilter, $filterlength) !== 0) {
          unset($items[$k]);
        }
      }
    }
  }
}
