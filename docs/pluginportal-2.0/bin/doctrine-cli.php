<?php
 
error_reporting(E_ALL);

// Define path to application directory
defined('APPLICATION_PATH')
    || define('APPLICATION_PATH', realpath(dirname(__FILE__) . '/../application'));

// Define application environment
defined('APPLICATION_ENV')
    || define('APPLICATION_ENV', (getenv('APPLICATION_ENV') ? getenv('APPLICATION_ENV') : 'development'));

define('DOCTRINE_PATH', APPLICATION_PATH.DIRECTORY_SEPARATOR.'doctrine');

// Ensure library/ is on include_path
set_include_path(implode(PATH_SEPARATOR, array(
    realpath(APPLICATION_PATH . '/../library'),
    get_include_path(),
)));



/** Zend_Application and Loader */
require_once 'Zend/Loader/Autoloader.php'; 
require_once 'Zend/Application.php'; 
require_once 'Doctrine/Parser/sfYaml/sfYaml.php'; 

$autoLoader=Zend_Loader_Autoloader::getInstance();
$autoLoader->setFallbackAutoloader(true);

 
// Create application, bootstrap, and run
$application = new Zend_Application(
    APPLICATION_ENV, 
    APPLICATION_PATH . '/configs/application.ini'
);
 
//$application->getBootstrap()->bootstrap("doctrine");
 
// Configure Doctrine Cli
// Normally these are arguments to the cli tasks but if they are set here the arguments will be auto-filled
$options = $application->getOptions();

$config = array(
            'models_path'         => isset( $options['doctrine']['models_path'] ) ? $options['doctrine']['models_path'] : APPLICATION_PATH.DIRECTORY_SEPARATOR.'models',
            'data_fixtures_path'  => isset( $options['doctrine']['data_fixtures_path'] ) ? $options['doctrine']['data_fixtures_path'] : DOCTRINE_PATH.DIRECTORY_SEPARATOR.'data'.DIRECTORY_SEPARATOR.'fixtures',
            'sql_path'            => isset( $options['doctrine']['sql_path'] ) ? $options['doctrine']['sql_path'] : DOCTRINE_PATH.DIRECTORY_SEPARATOR.'data'.DIRECTORY_SEPARATOR.'sql',
            'migrations_path'     => isset( $options['doctrine']['migrations_path'] ) ? $options['doctrine']['migrations_path'] : DOCTRINE_PATH.DIRECTORY_SEPARATOR.'migrations',
            'yaml_schema_path'    => isset( $options['doctrine']['yaml_schema_path'] ) ? $options['doctrine']['yaml_schema_path'] : DOCTRINE_PATH.DIRECTORY_SEPARATOR.'schema'
);
 
$cli = new Doctrine_Cli( $config );
try {
	$cli->run( $_SERVER['argv'] );
} catch (Exception $e) {
	echo $e->getMessage();
}
?>