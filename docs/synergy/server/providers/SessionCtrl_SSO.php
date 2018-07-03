<?php

namespace Synergy\Providers;

use Synergy\Interfaces\SessionProvider;
use Synergy\DB\SessionDAO;

/**
 * Description of SessionCtrl_SSO
 *
 * @author vriha
 */
class SessionCtrl_SSO implements SessionProvider {

    private $validationServiceUrl = 'https://netbeans.org/api/login/validate/';
    private $loginServiceUrl = 'https://netbeans.org/people/login';
    private $logoutServiceUrl = 'https://netbeans.org/people/logout';
    public $origin;
    public static $userId = -1; // TODO refactor to private
    public static $userRole = "undefined";
    public static $username = "";
    public static $user;
    private static $sso_checked = false;

    public function authenticate() {
        
    }

    /**
     * Redirects to login page
     */
    public function login() {
        header("location:" . $this->loginServiceUrl . '?original_uri=' . urlencode($this->origin . "&return=1"));
    }

    /**
     * No need for current implementation
     * @
     */
    public function loginPost($username, $password) {
        
    }

    public function logout($deleteCookies = false) {
        session_start();
        unset($_SESSION['user']);
        session_unset();
        session_destroy();
        session_write_close();
        SessionCtrl_SSO::$userId = -1;
        SessionCtrl_SSO::$username = "";
        SessionCtrl_SSO::$userRole = "undefined";
        SessionCtrl_SSO::$user = null;
        if ($deleteCookies) {

//            setcookie("SSO", "", time() - 3600, '/', '.netbeans.org'); // remove cookie as well
//            setcookie("SSO", "", time() - 3600, '/', 'netbeans.org');
//            setcookie("_junction2_session", "", time() - 3600, '/', 'netbeans.org');
//            setcookie("_junction2_session", "", time() - 3600, '/', '.netbeans.org');
//            setcookie("SSO_EXPIRATION", "", time() - 3600, '/', 'netbeans.org');
//            setcookie("SSO_EXPIRATION", "", time() - 3600, '/', '.netbeans.org');

            if (isset($_SERVER['HTTP_COOKIE'])) {
                $cookies = explode(';', $_SERVER['HTTP_COOKIE']);
                foreach ($cookies as $cookie) {
                    $parts = explode('=', $cookie);
                    $name = trim($parts[0]);
                    setcookie($name, '', time() - 3600);
                    setcookie($name, '', time() - 3600, '/');
                }
            }
        }
    }

    public function setOrigin($origin) {
        $this->origin = $origin;
    }

    public static function getUser() {
        if (is_null(SessionCtrl_SSO::$user)) {
            if (isset($_SESSION['user']) && !is_null($_SESSION['user'])) {
                SessionCtrl_SSO::$user = clone $_SESSION['user'];
            } else {
                if (!SessionCtrl_SSO::$sso_checked) {
                    SessionCtrl_SSO::$sso_checked = true;
                    $instance = new self;
                    try {
                        $instance->authenticate();
                    } catch (UserException $e) {
                        // ignore here
                    }
                }
            }
        }

        return SessionCtrl_SSO::$user;
    }

    public static function getUserId() {
        if (SessionCtrl_SSO::$userId === -1) {
            if (!is_null(SessionCtrl_SSO::getUser())) {
                SessionCtrl_SSO::$userId = intval(SessionCtrl_SSO::getUser()->userId);
            }
        }
        return SessionCtrl_SSO::$userId;
    }

    public static function getUserRole() {
        if (SessionCtrl_SSO::$userRole === "undefined") {
            if (!is_null(SessionCtrl_SSO::getUser())) {
                SessionCtrl_SSO::$userRole = SessionCtrl_SSO::getUser()->role;
            }
        }
        return SessionCtrl_SSO::$userRole;
    }

    public static function getUsername() {
        if (SessionCtrl_SSO::$username === "") {
            if (!is_null(SessionCtrl_SSO::getUser())) {
                SessionCtrl_SSO::$username = SessionCtrl_SSO::getUser()->username;
            }
        }
        return SessionCtrl_SSO::$username;
    }

    public static function sessionExists() {
        if (!is_null(SessionCtrl_SSO::getUser())) {
            return true;
        }
        return false;
    }

    public static function startAnonymousSession() {
        
    }

    public function startSession($username) {
        session_start();
        $s = SessionDAO::getUser($username);
        if (is_null($s)) {
            return false;
        }
        if (!isset($_SESSION['user'])) {
            $s->token = $_COOKIE['SSO'];
            $_SESSION['user'] = $s;
            SessionCtrl_SSO::$user = clone $s;
        }
        session_write_close();
        return true;
    }

//put your code here
}
