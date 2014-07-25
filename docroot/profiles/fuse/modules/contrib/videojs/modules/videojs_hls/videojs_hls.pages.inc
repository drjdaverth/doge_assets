<?php
/**
 * @file
 * Menu callbacks for the Video.js HTTP Live Streaming module.
 */

/**
 * Menu callback for the m3u8/% path.
 */
function videojs_hls_render_dynamic($filedata) {
  if (empty($filedata)) {
    drupal_not_found();
    exit;
  }

  $files = explode('|', rawurldecode($filedata));
  $content = videojs_hls_create_index($files);

  if (!$content) {
    drupal_not_found();
    exit;
  }

  return $content;
}

/**
 * Delivery callback for the m3u8/% path.
 */
function videojs_hls_deliver($page_callback_result) {
  if (is_int($page_callback_result)) {
    drupal_deliver_html_page($page_callback_result);
    return;
  }

  drupal_add_http_header('Content-Type', 'application/vnd.apple.mpegurl; charset=utf-8');
  echo $page_callback_result;
}

function videojs_hls_create_static_file(array $m3u8items) {
  $content = videojs_hls_create_index($m3u8items);

  if (!$content) {
    return FALSE;
  }

  $dir = variable_get('videojs_hls_delivery_static_scheme', variable_get('file_default_scheme', 'public')) . '://' . variable_get('videojs_hls_delivery_static_path', 'm3u8');
  $dir = rtrim($dir, '/');
  if (!file_prepare_directory($dir, FILE_CREATE_DIRECTORY | FILE_MODIFY_PERMISSIONS)) {
    watchdog('videojs_hls', 'Directory %directory is not writable, can\'t use static files. Please check your Video.js HLS settings.', array('%directory' => $dir));
    return FALSE;
  }

  $path = $dir . '/' . md5($content) . '.m3u8';

  if (!file_exists($path)) {
    file_put_contents($path, $content);
  }

  return $path;
}

function videojs_hls_create_index(array $m3u8items) {
  $return = array();
  $matches = array();

  foreach ($m3u8items as $m3u8item) {
    if (!preg_match('#(\d+)k#i', $m3u8item, $matches)) {
      continue;
    }
    $bw = ($matches[1] * 1000);
    $return[$bw] = '#EXT-X-STREAM-INF:PROGRAM-ID=1, BANDWIDTH=' . $bw . "\n" . file_create_url($m3u8item);
  }

  if (empty($return)) {
    return FALSE;
  }

  // Sort the files by bandwidth, lowest first.
  ksort($return);

  array_unshift($return, '#EXTM3U');

  return implode("\n", $return);
}
