<?xml version="1.0" encoding="utf-8"?>
<feed xmlns="http://www.w3.org/2005/Atom">
  <title>Jobeet</title>
  <subtitle>Latest Jobs</subtitle>
  <link href="<?php echo url_for('@job?sf_format=atom', true) ?>" rel="self"/>
  <link href="<?php echo url_for('@homepage', true) ?>"/>
  <updated><?php echo gmstrftime('%Y-%m-%dT%H:%M:%SZ', strtotime(Doctrine::getTable('JobeetJob')->getLatestPost()->getCreatedAt())) ?></updated>
  <author>
    <name>Jobeet</name>
  </author>
  <id><?php echo sha1(url_for('@job?sf_format=atom', true)) ?></id>
 
<?php foreach ($categories as $category): ?>
  <?php include_partial('sfJobeetJob/list', array('jobs' => $category->getActiveJobs(sfConfig::get('app_max_jobs_on_homepage')))) ?>
<?php endforeach; ?>
</feed>