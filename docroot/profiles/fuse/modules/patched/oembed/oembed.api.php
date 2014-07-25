<?php

/**
 * @file
 * Hooks provided by the oEmbed module.
 */

/**
 * Alters an oEmbed request parameters and provider.
 *
 * @param array $parameters
 *   oEmbed request parameters.
 * @param object $provider
 *   oEmbed provider info.
 * @param string $url
 *   The original URL or embed code to parse.
 */
function hook_oembed_request_alter(&$parameters, &$provider, $url) {
  if ($provider['name'] == 'default:youtube') {
    $parameters['iframe'] = '1';
  }
}

/**
 * Alters an oEmbed response.
 *
 * @param array $response
 *   oEmbed response data.
 */
function hook_oembed_response_alter(&$response) {
}

/**
 * Modify the provider's set of supported oEmbed response formats.
 *
 * @param array $formats
 *   Format handlers keyed by format name.
 */
function hook_oembedprovider_formats_alter(&$formats) {
  $formats['jsonp'] = array(
    'mime' => 'text/javascript',
    'callback' => '_oembedprovider_formats_jsonp',
  );
}
