<?php

namespace Synergy\Interfaces;

use Synergy\Model\Session;

/**
 *
 * @author vriha
 */
interface SessionProvider {

    /**
     * Checks if user is logged in
     * @return boolean true if user is logged on, false otherwise
     */
    public function authenticate();

    public function setOrigin($origin);

    /**
     * Logs user in - it could. This is done if user submits his credentials - so login in procedure is initiated with HTTP POST
     * @return Session|Null Description
     */
    public function loginPost($username, $password);

    /**
     * Logs user in - this is expected to use for e.g. SSO - so login in procedure is initiated with HTTP GET
     */
    public function login();

    /**
     * Destroys user session
     */
    public function logout($deleteCookies);

    /**
     * Returns current user's role. If user is not logged in, returns string "undefined"
     * @return string
     */
    public static function getUserRole();

    /**
     * Returns Session instance representing current user or null if not defined
     * @return Session|null
     */
    public static function getUser();

    /**
     * Returns current user's username. If user is not logged in, returns empty string
     * @return string
     */
    public static function getUsername();

    /**
     * Returns current user's id. If user is not logged in, returns string -1
     * @return int
     */
    public static function getUserId();
    
    /**
     * Indicates if user session exists
     * @return boolean
     */
    public static function sessionExists();
    
    public static function startAnonymousSession();
    
    /**
     * Starts a new session for user
     * @param type $username
     */
    public function startSession($username);
}

?>
