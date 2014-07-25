<?php

/**
 * @file
 * Admin file for PDF Reader
 */

/**
 * Settings
 */
function pdf_reader_admin_settings() {
  $form['about'] = array(
    '#markup' => t('Configure PDF Reader'),
  );
  return system_settings_form($form, FALSE);
}