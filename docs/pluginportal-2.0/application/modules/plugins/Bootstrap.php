<?php

class Plugins_Bootstrap extends Zend_Application_Module_Bootstrap {
  /*
   * some special routing setup
   */

  protected function _initRoutes() {
    //die(var_dump($this->_application->getOptions()));
    $appOptions = $this->_application->getOptions();
    if ($appOptions['fromCli'] != 1) {
      $front = $this->_application->getResource('FrontController');
      try {
        $router = $front->getRouter();
        // routes definition
        $route = new Zend_Controller_Router_Route('admin/:action/*', array('module' => 'plugins', 'controller' => 'admin', 'action' => 'index'));
        $route2 = new Zend_Controller_Router_Route(':action/*', array('module' => 'plugins', 'controller' => 'index'));
        $route3 = new Zend_Controller_Router_Route('plugin/:id/*', array('module' => 'plugins', 'controller' => 'index', 'action' => 'plugin'));
        $route4 = new Zend_Controller_Router_Route('PluginPortal/faces/PluginDetailPage.jsp', array('module' => 'plugins', 'controller' => 'index', 'action' => 'plugin'));
        $route5 = new Zend_Controller_Router_Route('PluginPortal/faces/PluginListPage.jsp', array('module' => 'plugins', 'controller' => 'index', 'action' => 'index'));
        $route6 = new Zend_Controller_Router_Route('PluginPortal/', array('module' => 'plugins', 'controller' => 'index', 'action' => 'index'));
        $route7 = new Zend_Controller_Router_Route('PluginPortal/faces/CategoryPage.jsp', array('module' => 'plugins', 'controller' => 'index', 'action' => 'index'));
        // routing setup
        $router->addRoute('public', $route2); // this one last as it's most generic
        $router->addRoute('plugin-detail', $route3);
        $router->addRoute('plugin-detail-legacy', $route4);
        $router->addRoute('plugin-list-legacy', $route5);
        $router->addRoute('frontpage-legacy', $route6);
        $router->addRoute('categ-list-legacy', $route7);
        $router->addRoute('admin', $route);
      } catch (Exception $e) {
        die('Routing setup problem' . $e->getMessage());
      }
    }
  }

}

