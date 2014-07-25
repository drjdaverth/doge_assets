; Drush make Api Vversion.
api = 2
core = 7.x

; Drupal core
projects[drupal][version] = "7.26"

; Installation profile
projects[fuse][type] = "profile"
projects[fuse][download][type] = git
projects[fuse][download][url] = "git@git.drupal.org:project/fuse.git"
projects[fuse][download][branch] = "master"

; Assets
;libraries[fuse][type] = "libraries"
;libraries[fuse][download][type] = git
;libraries[fuse][download][url] = "https://github.com/xcf33/fuse-assets.git"
