<?php

/**
 * symfony on include path example:
 *      require_once 'symfony/autoload/sfCoreAutoload.class.php';
 * symfony not on include path example:
 *      require_once dirname(__FILE__).'/../lib/vendor/symfony/lib/autoloadsfCoreAutoload.class.php;
 *
 * how to check your include path?
 * run phpinfo() command and search for include_path directive
 *
 **/
require_once 'symfony/autoload/sfCoreAutoload.class.php';
sfCoreAutoload::register();

class ProjectConfiguration extends sfProjectConfiguration
{
  static protected $zendLoaded = false;
 
  static public function registerZend()
  {
    if (self::$zendLoaded)
    {
      return;
    }
 
    set_include_path(sfConfig::get('sf_lib_dir').'/vendor'.PATH_SEPARATOR.get_include_path());
    require_once sfConfig::get('sf_lib_dir').'/vendor/Zend/Loader.php';
    Zend_Loader::registerAutoload();
    self::$zendLoaded = true;
  }

  public function setup()
  {
    // for compatibility / remove and enable only the plugins you want
    $this->enableAllPluginsExcept(array('sfPropelPlugin', 'sfCompat10Plugin'));
  }
}