<?php

include_once 'BaseController.php';

class SellEstateController extends BaseController {

    public function init() {

        if ($this->getRequest()->getParam("removeFromFavorites") != null) {
            General::removeFromCookie($this->getRequest()->getParam("removeFromFavorites"));
            $this->_redirect("/my-favorites");
            exit;
        }

        if ($this->getRequest()->getParam("addToFavorites") != null) {
            General::addToCookie($this->getRequest()->getParam("addToFavorites"));
            $this->_redirect("/my-favorites");
            exit;
        }
    }

    public function indexAction() {

        $properties = new Application_Model_PropertyMapper();

        $this->view->controller = "sell-estate";
        $this->view->action = "index";  
        $this->view->properties = $properties->fetchAll();

        $this->renderScript("/sell-estate/index.phtml");
    }

}

?>
