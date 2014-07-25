<?php

/**
 * Implementation of MediaInternetBaseHandler.
 *
 * @see hook_media_internet_providers().
 */
class MediaInternetVimeoHandler extends MediaInternetBaseHandler {
  public function parse($embedCode) {
    // Vimeo has a few URL formats:
    //   http://vimeo.com/*
    //   http://vimeo.com/video/*
    //   http://vimeo.com/groups/*/videos/*
    //   http://vimeo.com/channels/*#$ID
    $patterns = array(
      '@vimeo\.com/(\d+)@i',
      '@vimeo\.com/video/(\d+)@i',
      '@vimeo\.com/groups/.+/videos/(\d+)@i',
      '@vimeo\.com/channels/.+#(\d+)@i',
    );
    foreach ($patterns as $pattern) {
      preg_match($pattern, $embedCode, $matches);
      if (isset($matches[1])) {
        return file_stream_wrapper_uri_normalize('vimeo://v/' . $matches[1]);
      }
    }
  }

  public function claim($embedCode) {
    if ($this->parse($embedCode)) {
      return TRUE;
    }
  }

  public function getFileObject() {
    $uri = $this->parse($this->embedCode);
    $file = file_uri_to_object($uri, TRUE);

    // Try to default the file name to the video's title.
    if (empty($file->fid) && $info = $this->getOEmbed()) {
      $file->filename = truncate_utf8($info['title'], 255);
    }
    return $file;
  }

  /**
   * Returns information about the media.
   *
   * See http://video.search.yahoo.com/mrss
   *
   * @return
   *   If ATOM+MRSS information is available, a SimpleXML element containing
   *   ATOM and MRSS elements, as per those respective specifications.
   *
   * @todo Would be better for the return value to be an array rather than a
   *   SimpleXML element, but media_retrieve_xml() needs to be upgraded to
   *   handle namespaces first.
   */
  public function getMRSS() {
    $uri = $this->parse($this->embedCode);
    $video_id = arg(1, file_uri_target($uri));
    $rss_url = url('http://gdata.vimeo.com/feeds/api/videos/' . $video_id, array('query' => array('v' => '2')));
    // @todo Use media_retrieve_xml() once it's upgraded to include elements
    //   from all namespaces, not just the document default namespace.
    $entry = simplexml_load_file($rss_url);
    return $entry;
  }

  /**
   * Returns information about the media.
   *
   * See http://www.oembed.com/ and https://vimeo.com/api/docs/oembed
   *
   * @return
   *   If oEmbed information is available, an array containing 'title', 'type',
   *   'url', and other information as specified by the oEmbed standard.
   *   Otherwise, NULL.
   */
  public function getOEmbed() {
    $uri = $this->parse($this->embedCode);
    $external_url = drupal_realpath($uri);
    $oembed_url = url('http://vimeo.com/api/oembed.json', array(
      'query' => array('url' => $external_url))
    );
    $response = drupal_http_request($oembed_url);
    if (!isset($response->error)) {
      return drupal_json_decode($response->data);
    }
  }
}
