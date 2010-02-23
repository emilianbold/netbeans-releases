<?php use_stylesheet('jobs.css') ?>
 
<div id="jobs">
  <?php include_partial('sfJobeetJob/list', array('jobs' => $jobs)) ?>
</div>