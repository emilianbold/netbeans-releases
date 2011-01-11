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
class Zend_Validate_IsNumber extends Zend_Validate_Abstract {
    const VALUE = 'value';
    protected $_messageTemplates = array(
        self::VALUE => "'%value%' is not a number"
    );

    //put your code here
    public function isValid($value) {
        $this->_setValue($value);
        //replace , delimiter for . delimiter
        $value = str_replace(",", ".", $value);
        if (!is_numeric($value)) {
            $this->_error(self::VALUE);
            return false; 
        }
        
        return true;
    }

}

?>
