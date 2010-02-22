<?php use_helper('I18N', 'Date') ?>
<?php include_partial('job/assets') ?>

<div id="sf_admin_container">
  <h1><?php echo __('Job Creation', array(), 'messages') ?></h1>

  <?php include_partial('job/flashes') ?>

  <div id="sf_admin_header">
    <?php include_partial('job/form_header', array('jobeet_job' => $jobeet_job, 'form' => $form, 'configuration' => $configuration)) ?>
  </div>

  <div id="sf_admin_content">
    <?php include_partial('job/form', array('jobeet_job' => $jobeet_job, 'form' => $form, 'configuration' => $configuration, 'helper' => $helper)) ?>
  </div>

  <div id="sf_admin_footer">
    <?php include_partial('job/form_footer', array('jobeet_job' => $jobeet_job, 'form' => $form, 'configuration' => $configuration)) ?>
  </div>
</div>
