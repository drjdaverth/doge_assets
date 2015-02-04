<?php

/**
 * @file media_vimeo/includes/themes/media-vimeo-video.tpl.php
 *
 * Template file for theme('media_vimeo_video').
 *
 * Variables available:
 *  $uri - The media uri for the Vimeo video (e.g., vimeo://v/xsy7x8c9).
 *  $video_id - The unique identifier of the Vimeo video (e.g., xsy7x8c9).
 *  $id - The file entity ID (fid).
 *  $url - The full url including query options for the Vimeo iframe.
 *  $options - An array containing the Media Vimeo formatter options.
 *  $api_id_attribute - A required id attribute if the Javascript API
 *  is enabled; otherwise NULL.
 *  $width - The width value set in Media: Vimeo file display options.
 *  $height - The height value set in Media: Vimeo file display options.
 *
 */

?>
<div class="<?php print $classes; ?> media-vimeo-<?php print $id; ?>">
  <iframe class="media-vimeo-player" <?php print $api_id_attribute; ?>width="<?php print $width; ?>" height="<?php print $height; ?>" src="<?php print $url; ?>" frameborder="0" webkitAllowFullScreen mozallowfullscreen allowFullScreen></iframe>
</div>
