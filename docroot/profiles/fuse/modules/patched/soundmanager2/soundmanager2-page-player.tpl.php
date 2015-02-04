<?php
/**
 * @file
 * Page player template
 */
?>
<ul class="playlist">
  <?php foreach ($tracks as $delta => $track): ?>
    <li><?php print $track; ?></li>
  <?php endforeach; ?>
</ul>