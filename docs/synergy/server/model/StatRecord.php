<?php
namespace Synergy\Model;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of StatRecord
 *
 * @author vriha
 */
class StatRecord {

    public $label;
    public $value;

    function __construct($label, $value) {
        $this->label = $label;
        $this->value = $value;
    }

}

?>
