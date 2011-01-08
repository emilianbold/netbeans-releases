<?php

include_once 'SellEstateController.php';

class MyFavoritesController extends SellEstateController {

    public function indexAction() {
        $session = new Zend_Session_Namespace('favorites');
        
        if (count($session->favorites) > 0) {
            for ($i = 0; $i < count($session->favorites); $i++) {
                if (is_numeric($session->favorites[$i])) {
                    if ($i > 0)
                        $query .= " OR ";
                    $query .= " id = " . $session->favorites[$i] . " ";
                }
            }

            
            $this->view->count = $i;
        }
        
        
        $properties = new Application_Model_PropertyMapper();
        if ($this->view->count > 0)
            $this->view->properties = $properties->fetchAll($query);
        
        if ($this->view->properties == null) 
                $this->renderScript ("my-favorites/no-results.phtml");
        
    }

}

