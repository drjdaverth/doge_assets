<?php

/**
 *  @file
 *  Create a Vimeo Stream Wrapper class for the Media/Resource module.
 */

/**
 *  Create an instance like this:
 *  $vimeo = new ResourceVimeoStreamWrapper('vimeo://v/[video-code]');
 */
class MediaVimeoStreamWrapper extends MediaReadOnlyStreamWrapper {
  static function getMimeType($uri, $mapping = NULL) {
    return 'video/vimeo';
  }

  /**
   * Call the Vimeo Simple API and fetch the video information.
   *
   * See http://vimeo.com/api/docs/simple-api
   *
   * @return
   *   Array of properties.
   */
  static function getVideoProperties($video_id) {
    // The .php format returns a serialized array.
    $response = drupal_http_request('http://vimeo.com/api/v2/video/'. $video_id .'.php');
    return unserialize($response->data);
  }

  function getTarget($f) {
    return FALSE;
  }

  function interpolateUrl() {
    return 'http://vimeo.com/' . intval($this->parameters['v']);
  }

  function getOriginalThumbnailPath() {
    $video_properties = self::getVideoProperties($this->parameters['v']);
    return $video_properties[0]['thumbnail_large'];
  }

  function getLocalThumbnailPath() {
    $local_path = 'public://media-vimeo/' . intval($this->parameters['v']) . '.jpg';
    if (!file_exists($local_path)) {
      $dirname = drupal_dirname($local_path);
      file_prepare_directory($dirname, FILE_CREATE_DIRECTORY | FILE_MODIFY_PERMISSIONS);
      @copy($this->getOriginalThumbnailPath(), $local_path);
    }
    return $local_path;
  }
}
