<?php

/**
  * Implementation of hook_views_plugin().
  */
function views_fluidgrid_views_plugins() {
  return array(
    'style' => array(
      'views_fluidgrid' => array(
        'title' => t('Views Fluid Grid - jQuery Masonry'),
        'theme' => 'views_view_fluidgrid',
        'theme path' => drupal_get_path('module', 'views_fluidgrid') . '/includes',
        'help' => t('Provide a fluid grid display style for Views.'),
        'handler' => 'views_fluidgrid_style_plugin',
        'uses row plugin' => TRUE,
        'uses options' => TRUE,
        'type' => 'normal',
      ),
    ),
  );
}


