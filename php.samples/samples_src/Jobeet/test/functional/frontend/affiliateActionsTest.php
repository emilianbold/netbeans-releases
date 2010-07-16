<?php

include(dirname(__FILE__).'/../../bootstrap/functional.php');
 
$browser = new JobeetTestFunctional(new sfBrowser());
$browser->loadData();
 
$browser->
  info('1 - An affiliate can create an account')->
 
  get('/en/affiliate/new')->
  click('Submit', array('jobeet_affiliate' => array(
    'url'                            => 'http://www.example.com/',
    'email'                          => 'foo@example.com',
    'jobeet_categories_list'         => array(Doctrine::getTable('JobeetCategory')->findOneBySlug('programming')->getId()),
  )))->
  isRedirected()->
  followRedirect()->
  with('response')->checkElement('#content h1', 'Your affiliate account has been created')->
 
  info('2 - An affiliate must at leat select one category')->
 
  get('/en/affiliate/new')->
  click('Submit', array('jobeet_affiliate' => array(
    'url'   => 'http://www.example.com/',
    'email' => 'foo@example.com',
  )))->
  with('form')->isError('jobeet_categories_list')
;