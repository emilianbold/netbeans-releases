<?php

class Bootstrap extends Zend_Application_Bootstrap_Bootstrap {

    protected function _initSession() {
        // uff need to include SSO definition before session starts
        require_once('NbSsoUser.php');
        Zend_Session::start();
    }

    protected function _initAutoloader() {
        $autoLoader = Zend_Loader_Autoloader::getInstance();
        //$autoLoader->suppressNotFoundWarnings(false);
        $autoLoader->setFallbackAutoloader(true);
    }

    protected function _initModuleModels() {
        $this->bootstrap('FrontController');
        $front = $this->getResource('FrontController');
        $front->registerPlugin(new ZendCustom_ModelLoader());
    }

    protected function _initLayouts() {
        $front = $this->getResource('FrontController');
        $front->registerPlugin(new ZendCustom_LayoutLoader());
    }

}

