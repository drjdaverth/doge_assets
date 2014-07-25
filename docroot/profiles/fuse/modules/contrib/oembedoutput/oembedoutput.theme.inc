<?php

/**
 * @param $vars
 */
function oembedoutput_preprocess_oembedoutput_form(&$vars) {
  drupal_add_js(drupal_get_path('module', 'oembedoutput') . '/js/oembedoutput.js');
  $vars['js'] = drupal_get_js();

  drupal_add_css(drupal_get_path('module', 'oembedoutput') . '/oembedoutput.css');
  $vars['css'] = drupal_get_css();
  // $result = $vars['result'];
}