<?php

/**
 * Base Controller is base class that overrides 
 * method from zend controller. These methodes are 
 * then inheritted in other Controllers and so we 
 * don't have to override these methodes in every 
 * controller
 *
 * @author Filip Zamboj (fzamboj@netbeans.org)
 * @version 1.0 
 * 
 * @abstract
 */
abstract class BaseController extends Zend_Controller_Action {
    
    /**
     *
     * @param string $method
     * @param string $args 
     */
    public function __call($method, $args) {
        $this->_redirect("index");
    }

    public function init() {
        $this->view->controller = $this->getRequest()->getControllerName();
        $this->view->action = $this->getRequest()->getActionName();
    }
    
    public function preDispatch() {
        $this->view->render('index/_menu.phtml');
    }

    public function postDispatch() {
        $this->view->render('index/_footer.phtml');
    }

}

?>
