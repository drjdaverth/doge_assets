<?php

/**
 * @file
 * Token integration for the File download count module.
 */

/**
 * Implements hook_token_info().
 */
function file_download_count_token_info() {
  $info['tokens']['file']['download-count'] = array(
    'name' => t('Download count'),
    'description' => t('The number of times this file has been downloaded.'),
  );

  return $info;
}

/**
 * Implememnts hook_tokens().
 */
function file_download_count_tokens($type, $tokens, array $data = array(), array $options = array()) {
  $replacements = array();

  //$url_options = array('absolute' => TRUE);
  //if (isset($options['language'])) {
  //  $url_options['language'] = $options['language'];
  //  $language_code = $options['language']->language;
  //}
  //else {
  //  $language_code = NULL;
  //}

  $sanitize = !empty($options['sanitize']);

  if ($type == 'file' && !empty($data['file'])) {
    $file = $data['file'];

    foreach ($tokens as $name => $original) {
      switch ($name) {
        case 'download-count':
          $replacements[$original] = $file->download_count;
          break;
      }
    }
  }

  return $replacements;
}
