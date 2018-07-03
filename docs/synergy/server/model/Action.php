<?php
namespace Synergy\Model;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of Action
 *
 * @author lada
 */
class Action {

    //put your code here
    public $title;
    public $onClick;
    public $desc;
    public $iconName;
    public $isEnabled;

    function __construct($title, $onClick, $iconName, $desc = "") {
        $this->title = $title;
        $this->onClick = $onClick;
        $this->iconName = $iconName;
        $this->desc = $desc;
        $this->isEnabled = true;
    }

}

?>
