<?php use_helper('Text') ?>
 
<?php foreach ($jobs as $job): ?>
  <entry>
    <title><?php echo $job->getPosition() ?> (<?php echo $job->getLocation() ?>)</title>
    <link href="<?php echo url_for('job_show_user', $job, true) ?>" />
    <id><?php echo sha1($job->getId()) ?></id>
      <updated><?php echo gmstrftime('%Y-%m-%dT%H:%M:%SZ', strtotime($job->getCreatedAt())) ?></updated>
    <summary type="xhtml">
     <div xmlns="http://www.w3.org/1999/xhtml">
       <?php if ($job->getLogo()): ?>
         <div>
           <a href="<?php echo $job->getUrl() ?>">
             <img src="http://<?php echo $sf_request->getHost().'/uploads/jobs/'.$job->getLogo() ?>"
               alt="<?php echo $job->getCompany() ?> logo" />
           </a>
         </div>
       <?php endif; ?>
 
       <div>
         <?php echo simple_format_text($job->getDescription()) ?>
       </div>
 
       <h4>How to apply?</h4>
 
       <p><?php echo $job->getHowToApply() ?></p>
     </div>
    </summary>
    <author>
      <name><?php echo $job->getCompany() ?></name>
    </author>
  </entry>
<?php endforeach; ?>