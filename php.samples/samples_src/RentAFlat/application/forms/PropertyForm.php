<?php

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

        $element = new Zend_Form_Element_Text("title_en", array(
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
            $options[$value->getId()] = $value->getText_en();
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
            $options[$value->getId()] = $value->getText_en();
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


        $element = new Zend_Form_Element_Textarea("text_en", array(
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

