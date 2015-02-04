(function ($, soundManager, threeSixtyPlayer, settings) {

if (typeof soundManager != 'undefined' && settings.soundmanager2) {
  threeSixtyPlayer.config = {
    scaleFont: (navigator.userAgent.match(/msie/i) ? false : true),
    playNext: false,
    autoPlay: false,
    allowMultiple: false,
    loadRingColor: '#ccc',
    playRingColor: '#000',
    backgroundRingColor: '#eee',
    circleDiameter: 50,
    circleRadius: 25,
    animDuration: 500,
    animTransition: Animator.tx.bouncy,
    showHMSTime: true,
    scaleArcWidth: 1,

    useWaveformData: settings.soundmanager2.waveform,
    waveformDataColor: '#0099ff',
    waveformDataDownsample: 3,
    waveformDataOutside: false,
    waveformDataConstrain: false,
    waveformDataLineRatio: 0.64,

    useEQData: settings.soundmanager2.eq,
    eqDataColor: '#339933',
    eqDataDownsample: 4,
    eqDataOutside: true,
    eqDataLineRatio: 0.54,

    usePeakData: true,
    peakDataColor: '#ff33ff',
    peakDataOutside: true,
    peakDataLineRatio: 0.5,

    useAmplifier: settings.soundmanager2.amplifier
  }

  soundManager.flash9Options.useWaveformData = true;
  soundManager.flash9Options.useEQData = true;
  soundManager.flash9Options.usePeakData = true;
}
 
})(jQuery, soundManager, threeSixtyPlayer, Drupal.settings);
