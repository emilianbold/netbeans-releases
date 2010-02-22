<td colspan="6">
  <?php echo __('%%is_activated%% <small>%%JobeetCategory%%</small> - %%company%% (<em>%%email%%</em>) is looking for a %%position%% (%%location%%)', array('%%is_activated%%' => get_partial('job/list_field_boolean', array('value' => $jobeet_job->getIsActivated())), '%%JobeetCategory%%' => $jobeet_job->getJobeetCategory(), '%%company%%' => $jobeet_job->getCompany(), '%%email%%' => $jobeet_job->getEmail(), '%%position%%' => link_to($jobeet_job->getPosition(), 'jobeet_job_edit', $jobeet_job), '%%location%%' => $jobeet_job->getLocation()), 'messages') ?>
</td>
