SoundManager 2 module

= Description =

This modules provides Drupal with the soundmanager2 (http://www.schillmania.com/projects/soundmanager2/)
player and with two styles: page-player and ui360.

Basically there are three modules:

* Sound Manager 2 module: adds the required swf and js for embedding the player. Other modules should use this module
    to load the soundmanager2 player.
* A CCK formatter: for filefield and nodereference for audio.module nodes.
* An SM2 audio modules integration: provides a new player with both styles for audio.module nodes

The styles are "muxtape" (page-player)
http://www.schillmania.com/projects/soundmanager2/demo/page-player/

and ui360.
http://www.schillmania.com/projects/soundmanager2/demo/360-player/canvas-visualization.html

= Instructions =

* Download soundmanager2 from http://www.schillmania.com/projects/soundmanager2/doc/download/
and uncompress to sites/all/libraries/soundmanager2. (or anywhere else accesible from the web)
* Install module as usual. If you changed the loction of soundmanager2, you must configure the path
admin/settings/soundmanager2

If you want to use these players with audio module, in audio settings, choose page-player or ui360.
You may use it as nodereference with audio attach. (CCK required)

You may also use it with filefield module, if you do not need the extra id3 information.

= More =

For full instructions, visit http://drupal.org/project/soundmanager2
