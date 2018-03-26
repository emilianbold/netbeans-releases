<?php

class ZendCustom_LayoutLoader extends Zend_Controller_Plugin_Abstract {

  public function preDispatch(Zend_Controller_Request_Abstract $request) {
    //set_include_path(get_include_path() .	PATH_SEPARATOR . APPLICATION_PATH. '/modules/' . $request->getModuleName() . '/models/');
    Zend_Layout::startMvc(array(
                'layoutPath' => (APPLICATION_PATH . '/modules/' . $request->getModuleName() . '/layouts/'),
                'layout' => 'blank'
            ));
  }

}