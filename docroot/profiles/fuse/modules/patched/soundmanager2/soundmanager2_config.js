(function ($, soundManager, settings) {

// Setup the soundManager with reasonable defaults
if (typeof soundManager != 'undefined' && settings.soundmanager2) {
  soundManager.url = settings.soundmanager2.mod_url;
  soundManager.flashVersion = 9; // version of flash to require, either 8 or 9. Some API features require Flash 9.
  soundManager.debugMode = settings.soundmanager2.debug; // enable debugging output (div#soundmanager-debug, OR console if available+configured)
  soundManager.useHighPerformance = true; // position:fixed flash movie for faster JS/flash callbacks
  soundManager.useFastPolling = true; // uses lower flash timer interval for higher callback frequency, best combined with useHighPerformance
  soundManager.wmode = 'transparent'; // string: flash rendering mode - null, transparent, opaque (last two allow layering of HTML on top)
}

})(jQuery, soundManager, Drupal.settings);