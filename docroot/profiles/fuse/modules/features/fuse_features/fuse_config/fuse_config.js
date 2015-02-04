
/**
 * JavaScript behaviors for the front-end display of webforms.
 */
(function ($) {
Drupal.behaviors.fuse = {};
Drupal.behaviors.fuse.attach = function(context) {
    // adding some card overlay css
//    $('.views-field-filename').each(function() {
//        // get the height of it's icon
//        // dynamically set the css of this class to that height
//        var height = $(this).parent().find('.views-field-rendered img').attr('height');
//        $(this).css('height', height);
//    });
    $('.views-field-rendered').mouseover(function() {
        $(this).siblings('.views-field-filename').fadeIn();
    }).mouseout(function() {
        $(this).siblings('.views-field-filename').fadeOut();
    });

    // checkbox behavior
    $('input.vbo-select').click(function() {
        if ($(this).attr('checked') == true) {
           $(this).closest('.views-fluidgrid-item-inner').css('background', '#ffffaa');
           $(this).closest('.views-fluidgrid-item-inner').css('border', '1px solid #333');
        } else {
            $(this).closest('.views-fluidgrid-item-inner').css('background', '#FFF');
            $(this).closest('.views-fluidgrid-item-inner').css('border', '1px solid grey');
        }
    });

    // check all
    $('input.vbo-select-this-page').add('.vbo-select-all-pages').click(function() {
        if ($(this).attr('checked') == true) {
            $('.views-fluidgrid-item-inner').css('background', '#ffffaa');
            $('.views-fluidgrid-item-inner').css('border', '1px solid #333');
        } else {
            $('.views-fluidgrid-item-inner').css('background', '#FFF');
            $('.views-fluidgrid-item-inner').css('border', '1px solid grey');
        }
    });

    // our vbo checkbox should trigger original VBO checkbox
    $('span.visible_vbo input').click(function(){
       if ($(this).attr('checked') == false) {
           $(this).parents('.views-fluidgrid-item-inner').find('input').attr('checked', false);
       }
    });
};
})(jQuery);
