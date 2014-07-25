<?php

/**
 * @param $oembed_array
 * @return mixed
 */
function oembedoutput_plugin_photo_fields($oembed_array) {
  // validation might not be necessary as it might have to be done client side
  $fields['width'] = array(
    '#title' => 'width',
    '#type' => 'textfield',
    '#description' => t('The width of the image in pixels'),
    '#element_validate' => array('element_validate_integer'),
    '#value' => $oembed_array['width'],
    '#name' => 'width',
  );

  $fields['height'] = array(
    '#title' => 'height',
    '#type' => 'textfield',
    '#description' => t('The height of the image in pixels'),
    '#element_validate' => array('element_validate_integer'),
    '#value' => $oembed_array['height'],
    '#name' => 'height',
  );

  $fields['alt'] = array(
    '#title' => 'alt',
    '#type' => 'textfield',
    '#description' => t('The alternate text for the image'),
    '#name' => 'alt',
    '#value' => $oembed_array['title'],

  );

  // attaching a special class for these fields
  foreach ($fields as $name => $field) {
    $fields[$name]['#attributes'] = array(
      'class' => array('oembed_attr'),
    );
  }

  return $fields;
}