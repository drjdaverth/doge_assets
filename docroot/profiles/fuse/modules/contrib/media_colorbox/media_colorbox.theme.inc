<?php
/**
 * @file
 * Media Colorbox theme functions.
 */

/**
 * Returns HTML for a Media Colorbox file field formatter.
 *
 * @param $variables
 *   An associative array containing:
 *   - item: A build array.
 *   - entity_id: The entity ID.
 *   - field: The field data.
 *   - display_settings: The display settings.
 *   - langcode: The language code.
 *   - path: The path to the content.
 *   - title: The title of the content.
 *
 * @ingroup themeable
 */
function theme_media_colorbox($variables) {
  $entity_id = $variables['entity_id'];
  $file_id = $variables['file_id'];
  $field = $variables['field'];
  $field_name = isset($field['field_name']) ? $field['field_name'] : '';
  $settings = $variables['display_settings'];

 //switch to figure out where caption should come from
  switch ($settings['colorbox_caption']) {
    case 'title':
      $caption = $variables['title'];
      break;
    case 'mediafield':
      $caption = $variables['media_colorbox_caption'];
      break;
    default:
      $caption = '';
  }

  // Shorten the caption for the example styles or when caption shortening is active.
  $colorbox_style = variable_get('colorbox_style', 'default');
  $trim_length = variable_get('colorbox_caption_trim_length', 75);
  if ((variable_get('colorbox_caption_trim', 0)) && (drupal_strlen($caption) > $trim_length)) {
    $caption = drupal_substr($caption, 0, $trim_length - 5) . '...';
  }

  // Build the gallery id.
  switch ($settings['colorbox_gallery']) {
    case 'post':
      $gallery_id = 'gallery-' . $entity_id;
      break;
    case 'page':
      $gallery_id = 'gallery-all';
      break;
    case 'field_post':
      $gallery_id = 'gallery-' . $entity_id . '-' . $field_name;
      break;
    case 'field_page':
      $gallery_id = 'gallery-' . $field_name;
      break;
    case 'custom':
      $gallery_id = $settings['colorbox_gallery_custom'];
      break;
    default:
      $gallery_id = '';
  }

  //load file and render for select view mode
  if($file_id!=NULL){
    $file = file_load($file_id);
    $fview = file_view($file, $settings['file_view_mode'], $variables['langcode']);
    if ($file->type=='image'){
      $variables['path'] = file_create_url($file->uri);
    }
    $text = drupal_render($fview);
  }elseif(isset($variables['item'])) {
    $text=drupal_render($variables['item']);
  }
  //strip anchor tags as rendered output will be wrapped by another anchor tag
  //fix for issue #1477662
  $stripped_text = media_colorbox_strip_only($text, 'a');
  $output = theme('link', array(
    //'text' => drupal_render($variables['item']),
    'text' => $stripped_text,
    'path' => $variables['path'],
    'options' => array(
      'html' => TRUE,
      'attributes' => array(
        'title' => $caption,
        'class' => 'media-colorbox ' . $variables['item_class'],
        'style' => $variables['item_style'],
        'rel' => $gallery_id,
        'data-mediaColorboxFixedWidth' => $settings['fixed_width'],
        'data-mediaColorboxFixedHeight' => $settings['fixed_height'],
        'data-mediaColorboxAudioPlaylist' => $settings['audio_playlist'],
      ),
    ),
  ));

  return $output;
}

function media_colorbox_strip_only($str, $tags) {
    if(!is_array($tags)) {
        $tags = (strpos($str, '>') !== false ? explode('>', str_replace('<', '', $tags)) : array($tags));
        if(end($tags) == '') array_pop($tags);
    }
    foreach($tags as $tag) $str = preg_replace('#</?'.$tag.'[^>]*>#is', '', $str);
    return $str;
}
