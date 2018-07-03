<?php

namespace Synergy\Providers;

use Synergy\DB\SessionDAO;
use Synergy\Interfaces\SessionProvider;
use Synergy\Model\Session;

/**
 * Description of SessionCtrl
 *
 * @author vriha
 */
class SessionCtrl implements SessionProvider {

    public static $userId = -1; // TODO refactor to private
    public static $userRole = "undefined";
    public static $username = "";
    public static $user;

    public function authenticate() {
        if (is_null(SessionCtrl::$user)) {
            return false;
        }
        return true;
    }

    public function login() {
        
    }

    public function startSession($username) {
        session_start();
        $s = SessionDAO::getUser($username);
        if (is_null($s)) {
            return false;
        }
        if (!isset($_SESSION['user'])) {
            $_SESSION['user'] = $s;
            SessionCtrl::$user = clone $s;
        }
        session_write_close();
        return true;
    }

    public function loginPost($username, $password) {
        date_default_timezone_set('UTC');
        $md5psw = md5($password . SALT);
        error_log($username . "   " . $md5psw);
        $s = SessionDAO::authenticate($username, $md5psw);
        if (!is_null($s)) {
            $this->startSession($username);
            return SessionCtrl::$user;
        }
        return null;
    }

    public function logout($deleteCookies = false) {
        SessionCtrl_Production::$userId = -1;
        SessionCtrl_Production::$username = "";
        SessionCtrl_Production::$userRole = "undefined";
        SessionCtrl_Production::$user = null;
        unset($_SESSION['user']);
        session_unset();
        session_destroy();

        if ($deleteCookies && isset($_SERVER['HTTP_COOKIE'])) {
            $cookies = explode(';', $_SERVER['HTTP_COOKIE']);
            foreach ($cookies as $cookie) {
                $parts = explode('=', $cookie);
                $name = trim($parts[0]);
                setcookie($name, '', time() - 3600);
                setcookie($name, '', time() - 3600, '/');
            }
        }
    }

    public function setOrigin($origin) {
        
    }

    public static function getUserRole() {
        if (SessionCtrl::$userRole === "undefined") {
            if (!is_null(SessionCtrl::getUser())) {
                SessionCtrl::$userRole = SessionCtrl::getUser()->role;
            }
        }
        return SessionCtrl::$userRole;
    }

    public static function getUser() {
        if (is_null(SessionCtrl::$user)) {
            if (isset($_SESSION['user']) && !is_null($_SESSION['user'])) {
                SessionCtrl::$user = clone $_SESSION['user'];
            }
        }

        return SessionCtrl::$user;
    }

    public static function getUsername() {
        if (SessionCtrl::$username === "") {
            if (!is_null(SessionCtrl::getUser())) {
                SessionCtrl::$username = SessionCtrl::getUser()->username;
            }
        }
        return SessionCtrl::$username;
    }

    public static function getUserId() {
        if (SessionCtrl::$userId === -1) {
            if (!is_null(SessionCtrl::getUser())) {
                SessionCtrl::$userId = intval(SessionCtrl::getUser()->userId);
            }
        }
        return SessionCtrl::$userId;
    }

    public static function sessionExists() {
        if (!is_null(SessionCtrl::getUser())) {
            return true;
        }
        return false;
    }

    public static function startAnonymousSession() {
        date_default_timezone_set('UTC');
        $_SESSION['user'] = new Session(ANONYM, 'admin', date("Y-m-d H:i:s"), -1);
    }

//put your code here
}

?>
