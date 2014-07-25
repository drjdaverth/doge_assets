<?php

/**
 * A generic handler for "rich" type
 * This could be an iframe, etc
 * @param $oembed_array
 * @return mixed
 */
function oembedoutput_plugin_rich_fields($oembed_array) {
  $fields = array();

  $html = $oembed_array['html'];
  $html_obj = str_get_html($html);

  $element = $html_obj->nodes[1];

  // set some allowed attributes
  // right now mostly accomondate iframes
  // @todo, we have to account for attribute types
  $allowed = array('width', 'height', 'title', 'frameborder');
  foreach ($element->attr as $name => $value) {
    if (in_array($name, $allowed)) {
      $fields[$name] = array(
        '#type' => 'textfield',
        '#title' => $name,
        '#name' => $name,
        '#value' => $value,
      );
    }
  }

  // attaching a special class for these fields
  foreach ($fields as $name => $field) {
    $fields[$name]['#attributes'] = array(
      'class' => array('oembed_attr'),
    );
  }

  return $fields;
}