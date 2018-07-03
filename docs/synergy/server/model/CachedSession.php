<?php

namespace Synergy\Model;

/**
 * Description of CachedSession
 *
 * @author vriha
 */
class CachedSession {
    public $username;
    public $timestamp;
    public $cookie;
    
    function __construct($username, $timestamp, $cookie) {
        $this->username = $username;
        $this->timestamp = $timestamp;
        $this->cookie = $cookie;
    }

}
