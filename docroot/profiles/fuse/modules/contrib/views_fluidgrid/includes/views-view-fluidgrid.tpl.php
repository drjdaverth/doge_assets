<div id="<?php print $views_fluidgrid_id ?>" class="views-fluidgrid-wrapper clear-block">
    <?php foreach ($rows as $row): ?>
      <div class="views-fluidgrid-item">
        <div class="views-fluidgrid-item-inner">
          <?php print $row ?>
        </div>
      </div>
    <?php endforeach; ?>
</div>