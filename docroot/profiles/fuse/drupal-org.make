; Drush make api version.
api = 2
core = 7.x

; Contrib Modules
projects[admin_menu][subdir] = "contrib"
projects[admin_menu][version] = "3.0-rc4"

projects[ctools][subdir] = "contrib"
projects[ctools][version] = "1.4"

projects[features][subdir] = "contrib"
projects[features][version] = "2.0"

projects[strongarm][subdir] = "contrib"
projects[strongarm][version] = "2.0"

projects[views][subdir] = "contrib"
projects[views][version] = "3.7"

projects[views_bulk_operations][subdir] = "contrib"
projects[views_bulk_operations][version] = "3.2"

projects[entity][subdir] = "contrib"
projects[entity][version] = "1.3"

projects[libraries][subdir] = "contrib"
projects[libraries][version] = "2.1"

projects[devel][subdir] = "devel"
projects[devel][version] = "1.3"

projects[diff][subdir] = "devel"
projects[diff][version] = "3.2"

projects[colorbox][subdir] = "contrib"
projects[colorbox][version] = "2.5"

projects[autocomplete_deluxe][subdir] = "contrib"
projects[autocomplete_deluxe][version] = "2.0-beta3"

projects[backup_migrate][subdir] = "contrib"
projects[backup_migrate][version] = "2.8"

projects[multiform][subdir] = "contrib"
projects[multiform][version] = "1.0"

projects[file_entity][subdir] = "contrib"
projects[file_entity][version] = "2.0-alpha3"

projects[media][subdir] = "contrib"
projects[media][version] = "2.0-alpha3"

projects[media_colorbox][subdir] = "contrib"
projects[media_colorbox][version] = "1.0-rc4"

projects[media_vimeo][subdir] = "contrib"
projects[media_vimeo][version] = "2.0-rc1"

projects[media_youtube][subdir] = "contrib"
projects[media_youtube][version] = "2.0-rc4"

projects[pdf_reader][subdir] = "contrib"
projects[pdf_reader][version] = "1.0-rc4"

projects[plupload][subdir] = "contrib"
projects[plupload][version] = "1.6"

projects[videojs][subdir] = "contrib"
projects[videojs][version] = "3.x-dev"

projects[file_download_count][subdir] = "contrib"
projects[file_download_count][version] = "1.0-rc1"

projects[gdoc_field][subdir] = "contrib"
projects[gdoc_field][version] = "1.0"

projects[views_fluidgrid][subdir] = "contrib"
projects[views_fluidgrid][version] = "1.x-dev"

projects[front][subdir] = "contrib"
projects[front][version] = "2.4"

projects[job_scheduler][subdir] = "contrib"
projects[job_scheduler][version] = "2.0-alpha3"

projects[simplehtmldom][subdir] = "contrib"
projects[simplehtmldom][version] = "1.12"

; Feeds dev version alpha8-dev-13
projects[feeds][type] = "module"
projects[feeds][download][type] = "git"
projects[feeds][download][url] = "http://git.drupal.org/project/feeds.git"
projects[feeds][download][branch] = 7.x-2.x
projects[feeds][download][revision] = bf49063664b990908c1c0d2447f8efecc009e2a1
projects[feeds][subdir] = "contrib"

projects[feeds_xpathparser][subdir] = "contrib"
projects[feeds_xpathparser][version] = "1.0-beta4"

; Feature modules
; Later we should stablize this
projects[fuse_features][subdir] = "features"
projects[fuse_features][version] = "1.x-dev"

; Patched
projects[oembed][subdir] = "patched"
projects[oembed][type] = "module"
projects[oembed][download][type] = "git"
projects[oembed][download][url] = "http://git.drupal.org/project/oembed.git"
projects[oembed][download][branch] = 7.x-1.x
projects[oembed][download][revision] = 489db0fa4cf5cf8a5e57e27cfc69a576d83ce002

projects[soundmanager2][subdir] = "patched"
projects[soundmanager2][version] = "2.0-beta1"

; Custom
projects[oembedoutput][subdir] = "contrib"
projects[oembedoutput][version] = "1.0-alpha1"

projects[vbo_tag][subdir] = "contrib"
projects[vbo_tag][version] = "1.0-beta1"

; Theme.
projects[shiny][type] = "theme"
projects[shiny][version] = "1.4"

; Libraries
libraries[colorbox][download][type] = "git"
libraries[colorbox][download][url] = "https://github.com/jackmoore/colorbox.git"
libraries[colorbox][download][tag] = "1.4.26"
libraries[colorbox][type] = "libraries"

libraries[soundmanager2][download][type] = "git"
libraries[soundmanager2][download][url] = "https://github.com/scottschiller/SoundManager2.git"
libraries[soundmanager2][download][tag] = "V2.97a.20131201"
libraries[soundmanager2][type] = "libraries"

libraries[plupload][download][type] = "get"
libraries[plupload][download][url] = "https://github.com/moxiecode/plupload/archive/v1.5.8.tar.gz"
libraries[plupload][type] = "libraries"

; Patches
projects[oembed][patch][2154937] = https://drupal.org/files/issues/oembed-2154937-1.patch

;patch for provider file
projects[oembed][patch][] = https://drupal.org/files/issues/fuse-2193107-1.patch
projects[soundmanager2][patch][2162017] = https://drupal.org/files/issues/soundmanager-2162017-1.patch