<?php
namespace Synergy\Model;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of Membership
 *
 * @author lada
 */
class Membership {
    
    public $id;
    public $name;
    public $role;
    public $username;
    
    function __construct($tribeId, $tribeName) {
        $this->id = intval($tribeId);
        $this->name = $tribeName;
    }

}

?>
