<?php

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of UniqueValidator
 *
 * @author Filip Zamboj (fzamboj@netbeans.org)
 */
class Zend_Validate_UniqueRefNo extends Zend_Validate_Abstract {
    const VALUE = 'value';
    protected $_messageTemplates = array(
        self::VALUE => "'%value%' is not unique"
    );

    //put your code here
    public function isValid($value) {
        $this->_setValue($value);
        $mapper = new Application_Model_PropertyMapper();
        foreach ($mapper->fetchAll() as $property) {
            if ($property->getReference_no() == $value) {
                $this->_error(self::VALUE);
                return false;
            }
        }
        return true;
    }

}

?>
