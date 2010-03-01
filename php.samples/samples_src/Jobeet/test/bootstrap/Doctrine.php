<?php
include(dirname(__FILE__).'/unit.php');
 
$configuration = ProjectConfiguration::getApplicationConfiguration( 'frontend', 'test', true);
 
new sfDatabaseManager($configuration);
 
Doctrine::loadData(sfConfig::get('sf_test_dir').'/fixtures');