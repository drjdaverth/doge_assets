<?php
/**
 * @file
 * Soundmanager 2 UI 360 template
 */
?>
<?php if ($settings['inline']): ?>
  <div class="sm2-inline-list clearfix">
    <?php foreach ($tracks as $track): ?>
      <div class="ui360<?php print $settings['visualization'] ? ' ui360-vis' : ''; ?>"><?php print $track; ?></div>
    <?php endforeach; ?>
  </div>
<?php else: ?>
  <?php foreach ($tracks as $track): ?>
    <div class="ui360<?php print $settings['visualization'] ? ' ui360-vis' : ''; ?>"><?php print $track; ?></div>
  <?php endforeach; ?>
<?php endif; ?>
