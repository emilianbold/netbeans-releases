<?php
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

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

