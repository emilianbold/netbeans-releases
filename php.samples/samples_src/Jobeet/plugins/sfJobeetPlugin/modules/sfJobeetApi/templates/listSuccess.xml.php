<?xml version="1.0" encoding="utf-8"?>
<jobs>
<?php foreach ($jobs as $url => $job): ?>
  <job url="<?php echo $url ?>">
<?php foreach ($job as $key => $value): ?>
    <<?php echo $key ?>><?php echo $value ?></<?php echo $key ?>>
<?php endforeach; ?>
  </job>
<?php endforeach; ?>
</jobs>