<?php

/**
 * This is the CLI frontend to the framework, usable eg in cronjobs...
 *
 * It parses cli options, fires up Zend_Application, Controller_Front and load it
 * with appropriate params. It then returns the MVC output as would for browser.
 *
 * NOTE: at this moment it only accepts module, controller, action params, NO other
 *
 * If you do not spec those params, default ones will be used as configured in application.ini
 */
// Define path to application directory
defined('APPLICATION_PATH')
        || define('APPLICATION_PATH', realpath(dirname(__FILE__) . '/../application'));

// Define application environment
defined('APPLICATION_ENV')
        || define('APPLICATION_ENV', (getenv('APPLICATION_ENV') ? getenv('APPLICATION_ENV') : 'production'));

// Ensure library/ is on include_path
set_include_path(implode(PATH_SEPARATOR, array(
            realpath(APPLICATION_PATH . '/../library'),
            get_include_path(),
        )));

/** Zend_Application */
require_once 'Zend/Application.php';
require_once 'Zend/Session.php';

Zend_Session::setOptions(array('save_path'=>'/tmp'));
// Create application, bootstrap, and run
$application = new Zend_Application(
                APPLICATION_ENV,
                APPLICATION_PATH . '/configs/application.ini'
);

// now parse cli params and set them to front controller
try {
  $opts = new Zend_Console_Getopt(
                  array(
                      'module|m-s' => 'Module option, with required string parameter',
                      'controller|c-s' => 'Controller option, with required string parameter',
                      'action|a-s' => 'Action option, with required string parameter',
                      'help|h' => 'Display help mesasge',
                  )
  );
  $opts->parse();
  // help message requested, just print out and die
  if ($opts->getOption('h')) die ($opts->getUsageMessage ());

  // now bootstrap front controller so we can assign action etc later to it
  $application->bootstrap('FrontController');
  // also add flag to bootstrap opts so we know we are using cli and can disable routing setup in moduleSetup
  $appOptions = $application->getBootstrap()->getOptions();
  $appOptions['fromCli']=1;
  $application->getBootstrap()->setOptions($appOptions);
  // disable output buffering so we can print immediately from scripts
  Zend_Controller_Front::getInstance()->setParam('disableOutputBuffering', true);
  // now set cli values as default for the front controller
  if ($opts->getOption('m'))
    Zend_Controller_Front::getInstance()->setDefaultModule($opts->getOption('m'));
  if ($opts->getOption('c'))
    Zend_Controller_Front::getInstance()->setDefaultControllerName($opts->getOption('c'));
  if ($opts->getOption('a'))
    Zend_Controller_Front::getInstance()->setDefaultAction($opts->getOption('a'));
} catch (Zend_Console_Getopt_Exception $e) {
  die($e->getUsageMessage());
}

// and finally fire up MVC
try {
  $application->bootstrap()->run();
} catch (Exception $e) {
  echo $e->getMessage();
}
?>
