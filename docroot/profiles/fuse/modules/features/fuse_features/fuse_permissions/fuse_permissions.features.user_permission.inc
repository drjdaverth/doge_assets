<?php
/**
 * @file
 * fuse_permissions.features.user_permission.inc
 */

/**
 * Implements hook_user_default_permissions().
 */
function fuse_permissions_user_default_permissions() {
  $permissions = array();

  // Exported permission: 'access administration menu'.
  $permissions['access administration menu'] = array(
    'name' => 'access administration menu',
    'roles' => array(
      'media user' => 'media user',
    ),
    'module' => 'admin_menu',
  );

  // Exported permission: 'access administration pages'.
  $permissions['access administration pages'] = array(
    'name' => 'access administration pages',
    'roles' => array(
      'media user' => 'media user',
    ),
    'module' => 'system',
  );

  // Exported permission: 'access content overview'.
  $permissions['access content overview'] = array(
    'name' => 'access content overview',
    'roles' => array(
      'media user' => 'media user',
    ),
    'module' => 'node',
  );

  // Exported permission: 'add media from remote sources'.
  $permissions['add media from remote sources'] = array(
    'name' => 'add media from remote sources',
    'roles' => array(
      'media user' => 'media user',
    ),
    'module' => 'media_internet',
  );

  // Exported permission: 'administer files'.
  $permissions['administer files'] = array(
    'name' => 'administer files',
    'roles' => array(
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'create files'.
  $permissions['create files'] = array(
    'name' => 'create files',
    'roles' => array(
      'authenticated user' => 'authenticated user',
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'delete any image files'.
  $permissions['delete any image files'] = array(
    'name' => 'delete any image files',
    'roles' => array(
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'delete any pdf files'.
  $permissions['delete any pdf files'] = array(
    'name' => 'delete any pdf files',
    'roles' => array(
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'delete own audio files'.
  $permissions['delete own audio files'] = array(
    'name' => 'delete own audio files',
    'roles' => array(
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'delete own document files'.
  $permissions['delete own document files'] = array(
    'name' => 'delete own document files',
    'roles' => array(
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'delete own image files'.
  $permissions['delete own image files'] = array(
    'name' => 'delete own image files',
    'roles' => array(
      'authenticated user' => 'authenticated user',
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'delete own pdf files'.
  $permissions['delete own pdf files'] = array(
    'name' => 'delete own pdf files',
    'roles' => array(
      'authenticated user' => 'authenticated user',
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'delete own video files'.
  $permissions['delete own video files'] = array(
    'name' => 'delete own video files',
    'roles' => array(
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'download any audio files'.
  $permissions['download any audio files'] = array(
    'name' => 'download any audio files',
    'roles' => array(
      'authenticated user' => 'authenticated user',
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'download any document files'.
  $permissions['download any document files'] = array(
    'name' => 'download any document files',
    'roles' => array(
      'authenticated user' => 'authenticated user',
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'download any image files'.
  $permissions['download any image files'] = array(
    'name' => 'download any image files',
    'roles' => array(
      'authenticated user' => 'authenticated user',
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'download any pdf files'.
  $permissions['download any pdf files'] = array(
    'name' => 'download any pdf files',
    'roles' => array(
      'authenticated user' => 'authenticated user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'download any video files'.
  $permissions['download any video files'] = array(
    'name' => 'download any video files',
    'roles' => array(
      'authenticated user' => 'authenticated user',
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'download own audio files'.
  $permissions['download own audio files'] = array(
    'name' => 'download own audio files',
    'roles' => array(
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'download own document files'.
  $permissions['download own document files'] = array(
    'name' => 'download own document files',
    'roles' => array(
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'download own image files'.
  $permissions['download own image files'] = array(
    'name' => 'download own image files',
    'roles' => array(
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'download own pdf files'.
  $permissions['download own pdf files'] = array(
    'name' => 'download own pdf files',
    'roles' => array(
      'authenticated user' => 'authenticated user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'download own video files'.
  $permissions['download own video files'] = array(
    'name' => 'download own video files',
    'roles' => array(
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'edit any files'.
  $permissions['edit any files'] = array(
    'name' => 'edit any files',
    'roles' => array(
      'media user' => 'media user',
    ),
    'module' => 'fuse_config',
  );

  // Exported permission: 'edit any pdf files'.
  $permissions['edit any pdf files'] = array(
    'name' => 'edit any pdf files',
    'roles' => array(
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'edit own audio files'.
  $permissions['edit own audio files'] = array(
    'name' => 'edit own audio files',
    'roles' => array(
      'authenticated user' => 'authenticated user',
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'edit own document files'.
  $permissions['edit own document files'] = array(
    'name' => 'edit own document files',
    'roles' => array(
      'authenticated user' => 'authenticated user',
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'edit own image files'.
  $permissions['edit own image files'] = array(
    'name' => 'edit own image files',
    'roles' => array(
      'authenticated user' => 'authenticated user',
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'edit own pdf files'.
  $permissions['edit own pdf files'] = array(
    'name' => 'edit own pdf files',
    'roles' => array(
      'authenticated user' => 'authenticated user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'edit own video files'.
  $permissions['edit own video files'] = array(
    'name' => 'edit own video files',
    'roles' => array(
      'authenticated user' => 'authenticated user',
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'edit terms in tags'.
  $permissions['edit terms in tags'] = array(
    'name' => 'edit terms in tags',
    'roles' => array(
      'media user' => 'media user',
    ),
    'module' => 'taxonomy',
  );

  // Exported permission: 'import media'.
  $permissions['import media'] = array(
    'name' => 'import media',
    'roles' => array(
      'media user' => 'media user',
    ),
    'module' => 'media',
  );

  // Exported permission: 'output oembed'.
  $permissions['output oembed'] = array(
    'name' => 'output oembed',
    'roles' => array(
      'media user' => 'media user',
    ),
    'module' => 'oembedoutput',
  );

  // Exported permission: 'view files'.
  $permissions['view files'] = array(
    'name' => 'view files',
    'roles' => array(
      'anonymous user' => 'anonymous user',
      'authenticated user' => 'authenticated user',
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'view own files'.
  $permissions['view own files'] = array(
    'name' => 'view own files',
    'roles' => array(
      'authenticated user' => 'authenticated user',
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'view own private files'.
  $permissions['view own private files'] = array(
    'name' => 'view own private files',
    'roles' => array(
      'authenticated user' => 'authenticated user',
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  // Exported permission: 'view private files'.
  $permissions['view private files'] = array(
    'name' => 'view private files',
    'roles' => array(
      'media user' => 'media user',
    ),
    'module' => 'file_entity',
  );

  return $permissions;
}
