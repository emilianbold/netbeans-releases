<?php

namespace Synergy\Model;

use Synergy\App\Synergy;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of Setting
 *
 * @author lada
 */
class Setting {

    public $key;
    public $value;
    public $label;

    function __construct($key, $value, $label) {
        $this->key = $key;
        $this->value = $value;
        $this->label = $label;
    }

    public static function isValid($s) {
        if (isset($s->key) && strlen($s->key) > 0 && isset($s->value) && strlen($s->value) > 0)
            return true;
        return false;
    }

    public static function canEdit() {
        $role = Synergy::getSessionProvider()->getUserRole();
        switch ($role) {
            case "manager":
            case "admin" :
                return true;
            default :
                return false;
        }
    }

}

?>
