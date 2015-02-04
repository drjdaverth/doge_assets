<?php

/**
  * Implementation of views_plugin_style().
  */
class views_fluidgrid_style_plugin extends views_plugin_style {

  /**
   * Set default options
   */
  function option_definition() {
    $options = parent::option_definition();

    $options += array(
      'column_width' => array('default' => ''),
      'gutter_width' => array('default' => ''),
      'resizable' => array('default' => TRUE),
      'rtl' => array('default' => FALSE),
      'fit_width' => array('default' => FALSE),
      'animate' => array('default' => FALSE),
	  'animation_options' => array(
        'use_css' => array('default' => FALSE),
        'queue' => array('default' => FALSE),
        'duration' => array('default' => 500)
      ),
      'reload_on_window_load' => array('default' => FALSE),
    );

    return $options;
  }

  /**
   * Render the given style.
   */
  function options_form(&$form, &$form_state) {
    parent::options_form($form, $form_state);
    
    ctools_include('dependent');

    $form['column_width'] = array(
      '#type' => 'textfield',
      '#title' => t('Column width'),
      '#default_value' => $this->options['column_width'],
      '#description' => t('Width in pixels of 1 column of your grid. default: outer width of the first floated element.'),
    );
    
    $form['gutter_width'] = array(
      '#type' => 'textfield',
      '#title' => t('Gutter width'),
      '#default_value' => $this->options['gutter_width'],
      '#description' => t('Adds additional spacing between columns.'),
    );

    $form['resizable'] = array(
      '#type' => 'checkbox',
      '#title' => t('Resizable'),
      '#description' => t('Binds a Masonry call to window resizes.'),
      '#default_value' => !empty($this->options['resizable']),
    );

    $form['rtl'] = array(
      '#type' => 'checkbox',
      '#title' => t('Right to Left'),
      '#default_value' => $this->options['rtl'],
      '#description' => t('Enables right-to-left layout for languages like Hebrew and Arabic.'),
    );

    $form['fit_width'] = array(
      '#type' => 'checkbox',
      '#title' => t('Fit width'),
      '#default_value' => $this->options['fit_width'],
      '#description' => t('If enabled, Masonry will size the width of the container to the nearest column. When enabled, Masonry will measure the width of the container\'s parent element, not the width of the container. This option is ideal for centering Masonry layouts.'),
    );
    
    $form['animate'] = array(
      '#type' => 'checkbox',
      '#title' => t('Animate'),
      '#description' => t('You can use jQuery\'s animation features for animating your grid.'),
      '#default_value' => !empty($this->options['animate']),
    );
    
    $form['animation_options'] = array(
      '#type' => 'fieldset',
      '#title' => t('Animation options'),
      '#id' => 'edit-animation-options',
      '#prefix' => '<div id="edit-animation-options-wrapper">',
      '#suffix' => '</div>',
      '#process' => array('ctools_dependent_process'),
      '#dependency' => array('edit-style-options-animate' => array(TRUE)),
      '#input' => TRUE,
    );
    
    $form['animation_options']['use_css'] = array(
      '#type' => 'checkbox',
      '#title' => t('Use CSS'),
      '#description' => t('Attempt to use CSS for animations (requires Modernizr), otherwise fallback to using jQuery.'),
      '#default_value' => !empty($this->options['animation_options']['use_css']),
    ); 

    $form['animation_options']['queue'] = array(
      '#type' => 'checkbox',
      '#title' => t('Queue'),
      '#description' => t('If unchecked, the animation will begin immediately.'),
      '#default_value' => !empty($this->options['animation_options']['queue']),
    ); 
    
    $form['animation_options']['duration'] = array(
      '#title' => t('Duration'),  
      '#type' => 'textfield',
      '#description' => t('A string or number determining how long the animation will run.'),
      '#default_value' => $this->options['animation_options']['duration'],    
    );

    $form['reload_on_window_load'] = array(
      '#type' => 'checkbox',
      '#title' => t('Reload items on window load'),
      '#description' => t('Call the reload method on $(window).load(), this helps to deal with external media such as images or @font-face kits.'),
      '#default_value' => !empty($this->options['reload_on_window_load']),
    );
  }
}