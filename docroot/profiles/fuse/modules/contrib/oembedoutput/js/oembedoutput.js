
/**
 * JavaScript behaviors for the front-end display of webforms.
 */

(function ($) {
Drupal.behaviors.oembedoutput = {};
Drupal.behaviors.oembedoutput.attach = function(context) {
  // Add embed code
  $('#oembed_code_html').hide();
  $('input.oembed_attr').each(function() {
      var attr = $(this).attr('name');
      $("#oembed_code_html").children().attr(attr, $(this).val());
  });
  $('textarea.oembed_code').html($.trim($("#oembed_code_html").html()));
  // select all the code from the textarea
  $('#oembed_code_form input[type=submit]').click(function() {
      $('#oembed_code_form textarea').select();
  });
  $('#oembed_code_form textarea').click(function() {
     $(this).select();
  });
  // update the embed code
  $('input.oembed_attr').change(function () {
    // var html = $('textarea.oembed_code').text();
    var attr = $(this).attr('name');
    $("#oembed_code_html").children().attr(attr, $(this).val());
    $('textarea.oembed_code').html($.trim($("#oembed_code_html").html()));
  });
};
})(jQuery);
