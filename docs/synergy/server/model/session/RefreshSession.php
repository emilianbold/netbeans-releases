<?php

namespace Synergy\Model\Session;

/**
 * Description of RefreshSession
 *
 * @author vriha
 */
class RefreshSession {

    public $created;
    public $token;

    function __construct($created, $token) {
        $this->created = $created;
        $this->token = $token;
    }

}
