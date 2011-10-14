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

include_once 'Zend_Validate_UniqueRefNo.php';
include_once 'Zend_Validate_IsNumber.php';

class Application_Form_PropertyForm extends Zend_Form {

    public function init() {
        $this->setMethod("post");

        $element = new Zend_Form_Element_Text("reference_no", array(
                    "label" => "Ref. No",
                ));
        $element->addValidator(new Zend_Validate_UniqueRefNo(), true);
        $element->setRequired(true);
        $element->setDecorators(array(
            array('ViewScript',
                array(
                    'viewScript' => 'formElements/property/_textInput.phtml'
                )
            )
        ));
        $this->addElement($element);


        $element = new Zend_Form_Element_Text("price", array(
                    "label" => "Price",
                ));
        $element->setDecorators(array(
            array('ViewScript',
                array(
                    'viewScript' => 'formElements/property/_textInput.phtml'
                )
            )
        ));
        $element->setRequired(true);
        $element->addValidator(new Zend_Validate_IsNumber(), true);
        $this->addElement($element);

        $element = new Zend_Form_Element_Text("title", array(
                    "label" => "Title",
                ));
        $element->setDecorators(array(
            array('ViewScript',
                array(
                    'viewScript' => 'formElements/property/_textInput.phtml'
                )
            )
        ));
        $element->setRequired(true);
        $this->addElement($element);


        $locations = new Application_Model_PropertyLocationMapper();
        foreach ($locations->fetchAll() as $value) {
            $options[$value->getId()] = $value->getCity() . " - " . $value->getCityPart();
        }

        $options = array();
        $type = new Application_Model_PropertyBuildTypeMapper();
        $options[''] = '';
        foreach ($type->fetchAll() as $value) {
            $options[$value->getId()] = $value->getText();
        }
        $element = new Zend_Form_Element_Select("property_build_id", array(
                    'label' => 'Building type',
                ));
        $element->setMultiOptions($options);
        $element->setDecorators(array(
            array('ViewScript', array(
                    'viewScript' => 'formElements/property/_select.phtml'
                )
            )
        ));

        $element->setRequired(true);
        $element->addValidator(new Zend_Validate_NotEmpty());
        $this->addElement($element);

        $options = array();

        for ($i = 0; $i < 25; $i++) {
            $options[$i] = $i;
        }
        $element = new Zend_Form_Element_Select("floor", array(
                    'label' => 'Floor',
                ));
        $element->setMultiOptions($options);
        $element->setDecorators(array(
            array('ViewScript', array(
                    'viewScript' => 'formElements/property/_select.phtml'
                )
            )
        ));

        $element->setRequired(true);
        $element->addValidator(new Zend_Validate_NotEmpty());
        $this->addElement($element);

        $options = array();
        $dis = new Application_Model_DispositionMapper();
        $options[''] = '';
        foreach ($dis->fetchAll() as $value) {
            $options[$value->getId()] = $value->getText();
        }
        $element = new Zend_Form_Element_Select("disposition_id", array(
                    'label' => 'Disposition',
                ));
        $element->setMultiOptions($options);
        $element->setDecorators(array(
            array('ViewScript', array(
                    'viewScript' => 'formElements/property/_select.phtml'
                )
            )
        ));
        $element->setRequired(true);
        $element->addValidator(new Zend_Validate_NotEmpty());
        $this->addElement($element);

        $element = new Zend_Form_Element_Text("area", array(
                    "label" => "Area (m&sup2;)",
                ));
        $element->setDecorators(array(
            array('ViewScript',
                array(
                    'viewScript' => 'formElements/property/_textInput.phtml'
                )
            )
        ));
        $element->addValidator(new Zend_Validate_IsNumber(), true);
        $this->addElement($element);


        $element = new Zend_Form_Element_Text("cellar", array(
                    "label" => "Cellar (m&sup2;)",
                ));
        $element->setDecorators(array(
            array('ViewScript',
                array(
                    'viewScript' => 'formElements/property/_textInput.phtml'
                )
            )
        ));
        $element->addValidator(new Zend_Validate_IsNumber(), true);
        $this->addElement($element);

        $element = new Zend_Form_Element_Text("balcony", array(
                    "label" => "Balcony (m&sup2;)",
                ));
        $element->setDecorators(array(
            array('ViewScript',
                array(
                    'viewScript' => 'formElements/property/_textInput.phtml'
                )
            )
        ));
        $element->addValidator(new Zend_Validate_IsNumber(), true);
        $this->addElement($element);

        $element = new Zend_Form_Element_Text("terace", array(
                    "label" => "Terrace (m&sup2;)",
                ));
        $element->setDecorators(array(
            array('ViewScript',
                array(
                    'viewScript' => 'formElements/property/_textInput.phtml'
                )
            )
        ));
        $element->addValidator(new Zend_Validate_IsNumber(), true);
        $this->addElement($element);

        $element = new Zend_Form_Element_Text("loggia", array(
                    "label" => "Loggia (m&sup2;)",
                ));
        $element->setDecorators(array(
            array('ViewScript',
                array(
                    'viewScript' => 'formElements/property/_textInput.phtml'
                )
            )
        ));
        $element->addValidator(new Zend_Validate_IsNumber(), true);
        $this->addElement($element);

        $element = new Zend_Form_Element_Text("garage", array(
                    "label" => "Garage (m&sup2;)",
                ));
        $element->setDecorators(array(
            array('ViewScript',
                array(
                    'viewScript' => 'formElements/property/_textInput.phtml'
                )
            )
        ));
        $element->addValidator(new Zend_Validate_IsNumber(), true);
        $this->addElement($element);

        $element = new Zend_Form_Element_Text("garden", array(
                    "label" => "Garden (m&sup2;)",
                ));
        $element->setDecorators(array(
            array('ViewScript',
                array(
                    'viewScript' => 'formElements/property/_textInput.phtml'
                )
            )
        ));
        $element->addValidator(new Zend_Validate_IsNumber(), true);
        $this->addElement($element);

        $element = new Zend_Form_Element_Checkbox("lift", array(
                    "label" => "Lift"
                ));
        $this->addElement($element);

        $element = new Zend_Form_Element_Checkbox("parking_place", array(
                    "label" => "Parking place:"
                ));
        $this->addElement($element);


        $options = array();
        $dis = new Application_Model_PropertyLocationMapper();
        $options[''] = '';
        foreach ($dis->fetchAll() as $value) {
            $options[$value->getId()] = $value->getCity() . " - " . $value->getCityPart();
        }
        $element = new Zend_Form_Element_Select("location_id", array(
                    'label' => 'Location',
                ));
        $element->setMultiOptions($options);
        $element->setDecorators(array(
            array('ViewScript', array(
                    'viewScript' => 'formElements/property/_select.phtml'
                )
            )
        ));

        $element->setRequired(true);
        $element->addValidator(new Zend_Validate_NotEmpty());
        $this->addElement($element);

        $element = new Zend_Form_Element_Text("street", array(
                    "label" => "Address",
                ));
        $element->setDecorators(array(
            array('ViewScript',
                array(
                    'viewScript' => 'formElements/property/_textInput.phtml'
                )
            )
        ));
        $this->addElement($element);

        $element = new Zend_Form_Element_Text("c1", array());
        $element->setDecorators(array(array('ViewScript', array('viewScript' => 'formElements/property/_clear.phtml'))));
        $this->addElement($element);


        $element = new Zend_Form_Element_Textarea("text", array(
                    "label" => "Description",
                ));
        $element->setDecorators(array(
            array('ViewScript',
                array(
                    'viewScript' => 'formElements/property/_textArea.phtml'
                )
            )
        ));
        $element->setRequired(true);
        $this->addElement($element);


        $element = new Zend_Form_Element_Submit("submit", array(
                    "value" => "Save",
                    "class" => "button"
                ));
        $element->setDecorators(array(
            array('ViewScript',
                array(
                    'viewScript' => 'formElements/property/_submit.phtml'
                )
            )
        ));
        $this->addElement($element);
    }

}

