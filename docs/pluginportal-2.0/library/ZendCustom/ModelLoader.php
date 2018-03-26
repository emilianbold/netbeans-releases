<?php
class ZendCustom_ModelLoader extends Zend_Controller_Plugin_Abstract
{ 
  public function preDispatch(Zend_Controller_Request_Abstract $request)
  {
    // set the default connection to the requested module name so the DoctrineCore::getTable() works
    if (Doctrine_Manager::getInstance()->contains($request->getModuleName())) {
      Doctrine_Manager::getInstance()->setCurrentConnection($request->getModuleName());
    }
    // set the include path to the module's models folder
		set_include_path(get_include_path() .	PATH_SEPARATOR . APPLICATION_PATH. '/modules/' . $request->getModuleName() . '/models/');
  }
}