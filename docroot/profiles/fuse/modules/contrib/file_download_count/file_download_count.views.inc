<?php

/**
 * @file
 * Views integration for the File download count module.
 */

/**
 * Implements hook_views_data().
 */
function file_download_count_views_data() {
  $data['file_download_count']['table'] = array(
    'group' => t('File downloads'),
    'join' => array(
      'file_managed' => array(
        'field' => 'fid',
        'left_field' => 'fid',
      ),
      'users' => array(
        'field' => 'uid',
        'left_field' => 'uid',
      ),
    ),
  );

  $data['file_download_count']['fid'] = array(
    'title' => t('File'),
    'help' => t('The file that was downloaded.'),
    'relationship' => array(
      'title' => t('File'),
      'label' => t('File'),
      'base' => 'file_managed',
      'base field' => 'fid',
      'handler' => 'views_handler_relationship',
    ),
  );
  $data['file_download_count']['timestamp'] = array(
    'title' => t('Download date'),
    'help' => t('The date the file was downloaded.'),
    'field' => array(
      'handler' => 'views_handler_field_date',
      'click sortable' => TRUE,
    ),
    'sort' => array(
      'handler' => 'views_handler_sort_date',
    ),
    'filter' => array(
      'handler' => 'views_handler_filter_date',
    ),
  );
  $data['file_download_count']['uid'] = array(
    'title' => t('User who downloaded'),
    'help' => t('The user that downloaded the file.'),
    'relationship' => array(
      'title' => t('User who downloaded'),
      'label' => t('User who downloaded'),
      'base' => 'users',
      'base field' => 'uid',
      'handler' => 'views_handler_relationship',
    ),
  );
  $data['file_download_count']['ip_address'] = array(
    'title' => t('IP address'),
    'help' => t('The IP address of the user that downloaded the file.'),
    'field' => array(
      'handler' => 'views_handler_field',
      'click sortable' => TRUE,
     ),
    'filter' => array(
      'handler' => 'views_handler_filter_string',
    ),
    'argument' => array(
      'handler' => 'views_handler_argument_string',
    ),
    'sort' => array(
      'handler' => 'views_handler_sort',
    ),
  );

  return $data;
}
