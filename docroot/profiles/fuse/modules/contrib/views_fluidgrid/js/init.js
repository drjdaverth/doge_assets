(function ($) {
  var pageLoaded = false;

  Drupal.behaviors.views_fluidgrid = {
    attach: function (context) {
      $('.views-fluidgrid-wrapper:not(.views-fluidgrid-processed)', context).each(function (index) {
        var $this = $(this),
        id = $(this).attr('id'),
        settings = Drupal.settings.viewsFluidGrid[id];

        $this
        .addClass('views-fluidgrid-processed')
        .masonry({
          columnWidth: settings.columnWidth != '' ? parseInt(settings.columnWidth) : undefined,
          gutterWidth: settings.gutterWidth != '' ? parseInt(settings.gutterWidth) : undefined,
          isResizable: settings.resizable,
          isRTL: settings.rtl,
          isFitWidth: settings.fitWidth,
          isAnimated: settings.animate && settings.animationOptions.useCss ? !Modernizr.csstransitions : settings.animate,
          animationOptions: {
            duration: parseInt(settings.animationOptions.duration),
            queue: settings.animationOptions.queue
          },
          itemSelector: '.views-fluidgrid-item:visible'
        });

        // this solves problems where the user is loading images of unknown sizes, or perhaps @font-face kits
        if (settings.reloadOnWindowLoad &&
            pageLoaded == false) {
          $(window)
          .unbind('.fluidgrid') // it's possible this is called again before the initial $(window).load() is fired
          .one('load.fluidgrid', function () {
            pageLoaded = true;
            $this.masonry('reload');
          });
        }
      });
    }
  }
})(jQuery);