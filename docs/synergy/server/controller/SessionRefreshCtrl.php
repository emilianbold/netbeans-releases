<?php

namespace Synergy\Controller;

use Synergy\DB\SessionRefreshDAO;

/**
 * Description of SessionRefreshCtrl
 *
 * @author vriha
 */
class SessionRefreshCtrl {

    private $refreshDao;

    function __construct() {
        $this->refreshDao = new SessionRefreshDAO();
    }

    public function saveToken($token) {
        return $this->refreshDao->saveToken($token);
    }

    public function getToken($token) {
        return $this->refreshDao->getToken($token);
    }

    public function removeToken($token) {
        return $this->refreshDao->removeToken($token);
    }

}
