<?php

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

namespace Synergy\Controller;

use Synergy\App\Synergy;
use Synergy\DB\RegistrationDAO;
use Synergy\DB\SessionDAO;
use Synergy\Model\Exception\UserException;

/**
 * Description of RegistrationCtrl
 *
 */
class RegistrationCtrl {

    private $regDao;

    function __construct() {
        $this->regDao = new RegistrationDAO();
    }

    public function register($reg) {
        
        $userCtrl = new UserCtrl();
        $uid = $userCtrl->getUserIDbyUsername($reg->username);
        if($uid > 0){
            throw new UserException("Registration failed", "Username is already taken, please select different one");
        }
                
        $uid = $userCtrl->getUserIdByEmail($reg->email);
        
        if($uid > 0){
            throw new UserException("Registration failed", "Email is already taken, please select different one");
        }
        
        $r = $this->regDao->register($reg);
        if ((bool) $r) {
            date_default_timezone_set('UTC');
            $md5psw = md5($reg->password . SALT);
            $s = SessionDAO::authenticate($reg->username, $md5psw);
            if (!is_null($s)) {
                $sessionCtrl = Synergy::getProvider("session");
                $sessionCtrl->startSession($reg->username);
            }
        }


        return $r;
    }

}
