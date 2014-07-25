<?php
/**
 * @file
 * Provide the HTML output of the Video.js video player.
 *
 * Available variables:
 *
 * $items
 *   Array of video or audio items. Each item in the array is an array with
 *   keys:
 *   - uri: URI to the file, like public://myfile.mp4
 *   - filemime: original mime type, like video/mp4
 *   - videotype: resolved mime type
 *   - description: title, if given
 * $tracks
 *   Array of track items, for instance for subtitles, chapters, etc.
 *   Each item in the array is an array with keys:
 *   - uri: URI to the subtitle file
 *   - filemime: mime type of the subtitle file, currently always text/vtt
 *   - kind: one of subtitles, captions, descriptions, chapters or metadata
 *   - charset: character set of the file, such as utf-8
 *   - srclang: language code
 *   - description: label for the subtitle
 * $poster
 *   URL to poster image.
 * $autoplay
 *   Boolean indicating whether to start playback at page load.
 * $loop
 *   Boolean indicating whether to loop video playback continuously.
 * $hidecontrols
 *   Boolean indicating whether to not show the controls.
 * $preload
 *   String containing none, auto or metadata.
 * $width
 *   Integer containing width of the video.
 * $height
 *   Integer containing height of the video.
 * $class
 *   String containing concatenated classes of the video.
 */

$attrs = '';
if (!empty($autoplay)) {
  $attrs .= ' autoplay="autoplay"';
}
if (!empty($poster)) {
  $attrs .= ' poster="'. check_plain($poster) .'"';
}
if (!empty($loop)) {
  $attrs .= ' loop="loop"';
}
if (empty($hidecontrols)) {
  $attrs .= ' controls="controls"';
}
if (!empty($preload) && ($preload === 'none' || $preload === 'auto' || $preload === 'metadata')) {
  $attrs .= ' preload="' . $preload . '"';
}

if(!empty($class)) {
  $class .= ' video-js vjs-default-skin';
}
else {
  $class = 'video-js vjs-default-skin';
}

if (!empty($items)): ?>
<video id="<?php print $player_id; ?>-video" data-setup="{}" class="<?php print htmlspecialchars($class); ?>" width="<?php print $width; ?>" height="<?php print $height; ?>"<?php echo $attrs; ?>>
<?php foreach ($items as $item): ?>
  <source src="<?php print $item['src']['safe']; ?>" type="<?php print $item['videotype']['safe'] ?>" />
<?php endforeach; ?>
<?php foreach ($tracks as $track):
  $default = $track['default'] ? ' default="default"' : '';
?>
  <track src="<?php print $track['src']['safe'] ?>" type="<?php print $track['filemime']['safe'] ?>" kind="<?php print check_plain($track['kind']) ?>" label="<?php print check_plain($track['label']) ?>" srclang="<?php print check_plain($track['langcode']) ?>"<?php print $default; ?> />
<?php endforeach; ?>
</video>
<?php endif;
