<?php
namespace Synergy\Model;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of Label
 *
 * @author vriha
 */
class Label {

    public $id;
    public $label;
    
    function __construct($label,$id) {
        $this->id = intval($id);
        $this->label = strtolower($label);
    }

    
}

?>
