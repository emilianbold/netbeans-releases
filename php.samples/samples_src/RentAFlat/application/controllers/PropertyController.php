<?php

include_once 'BaseController.php';

class PropertyController extends BaseController {

    public function preDispatch() {
        $controller = $this->getRequest()->getControllerName();
        $action = $this->getRequest()->getActionName();

        $id = $this->getRequest()->getParam("property");
        $pm = new Application_Model_PropertyMapper();
        if ($id !== NULL) {
            $properties = $pm->fetchAll("id = " . $id);
            if (count($properties) == 0) {
                $this->_redirect("property/error");
            }

            //create meta tags
            foreach ($properties as $property) {
                $disp = $property->getDisposition();
                $area = $property->getArea();
                $text = $property->getTitle_en();

                $temp = $property->getTextLocation();
                
                $price = $property->getFormattedPrice();
                $desc = $property->getText_en();
            }

            $this->view->title = $offer . " | " . $location . ", " . $disp . ", " . $area . " m2, " . $price . " Kč";
            $this->view->description = substr($desc, 0, 180) . " ... " . $offer . " | " . $location . ", " . $disp . ", " . $area . " m2";
        }
        $this->view->render('index/_menu.phtml');
    }

    public function init() {

        $this->view->action = $this->getRequest()->getActionName();

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

    public function detailAction() {

        $this->view->nothingFound = true;
        if ($this->_hasParam("property")) {
            $id = $this->_getParam("property");

            $param = new Application_Model_PropertyMapper();
            $property = $param->fetchAll("id = " . $id);

            if (count($property) == 0) {
                $this->_redirect("property/error");
            }



            $this->view->property = $property;
           

        } else {
            $this->renderScript("property/error.phtml");
        }
    }

    public function addAction() {
        $form = new Application_Form_PropertyForm();
        $this->view->form = $form;
    }

    public function saveFormAction() {

        $request = $this->getRequest();
        $form = new Application_Form_PropertyForm($options);

        
        $form->populate($request->getParams());
        $this->view->form = $form;

        
        
        if ($this->getRequest()->isPost()) {
            
            if ($form->isValid($_POST)) {
                
                $values = new Application_Model_Property($form->getValues());
                $mapper = new Application_Model_PropertyMapper();
                
                
                if ($request->getParam("id") != null)
                    $values->setId($request->getParam("id"));

                $values->setArea(str_replace(",", ".", $values->getArea()));
                $values->setPrice(str_replace(",", ".", $values->getPrice()));
                

                $newId = $mapper->save($values);
                $this->_redirect("/property/detail/property/" . $newId);
                return;
            } 
        }

        
        $this->renderScript("property/add.phtml");

    }

    public function editAction() {

        if (is_numeric($this->getRequest()->getParam("id"))) {

            $id = $this->getRequest()->getParam("id");
            $top = new Application_Model_PropertyMapper();
            $item = $top->fetchAllFilterToArray("id = " . $id);



            $form = new Application_Form_PropertyForm($options);
            $element = new Zend_Form_Element_Hidden("id", array(
                        "value" => $id
                    ));
            $form->addElement($element);

            $form->populate($item);
            $this->view->id = $id;
            $this->view->form = $form;
            $this->view->enablePhoto = true;

            $items = $top->fetchAll("id = $id");
            foreach ($items as $item) {
                $this->view->cover = $item->getCoverObject();
                $this->view->pictures = $item->getAllPictures();
            }
            $this->renderScript("property/add.phtml");
        } else {
            $this->_redirect("sell-estate/index");
            exit;
        }
    }

}

