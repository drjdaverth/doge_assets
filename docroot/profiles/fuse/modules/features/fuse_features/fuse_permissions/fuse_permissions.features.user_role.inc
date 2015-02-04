<?php
/**
 * @file
 * fuse_permissions.features.user_role.inc
 */

/**
 * Implements hook_user_default_roles().
 */
function fuse_permissions_user_default_roles() {
  $roles = array();

  // Exported role: media user.
  $roles['media user'] = array(
    'name' => 'media user',
    'weight' => 2,
  );

  return $roles;
}
