<?php

namespace Synergy\Providers;

use Synergy\Controller\UserCtrl;
use Synergy\DB\SessionDAO;
use Synergy\Interfaces\SessionProvider;
use Synergy\Model\Exception\UserException;
use Synergy\Model\Session;
use Synergy\App\Synergy;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of Session
 *
 * @author vriha
 */
class SessionCtrl_Production implements SessionProvider {

    private $validationServiceUrl = 'https://netbeans.org/api/login/validate/';
    private $loginServiceUrl = 'https://netbeans.org/people/login';
    private $logoutServiceUrl = 'https://netbeans.org/people/logout';
    public $origin;
    public static $userId = -1; // TODO refactor to private
    public static $userRole = "undefined";
    public static $username = "";
    public static $user;
    private static $sso_checked = false;
    
    public function setOrigin($origin) {
        $this->origin = $origin;
    }

    /**
     * Returns true or false if user is logged in
     * @return boolean
     */
    public function authenticate() {
        if (isset($_COOKIE['SSO']) && !is_null($_COOKIE['SSO'])) {
            $username = explode(":", $_COOKIE['SSO']); // TODO extract token and username

            $ctrl = new UserCtrl();
            if ($ctrl->getUserIDbyUsername($username[0]) === -1) {
                throw new UserException("User not found", "User account does not exist in Synergy", "SSO");
            }

            if (isset($_SESSION['user']) && !is_null($_SESSION['user'])) { // skip SSO checking on nb.org
                return $this->startSession($username[0]);
            }

            $result = $this->makeRequest($_COOKIE['SSO']);
            if ($result->info['http_code'] !== 200) { // authentication failed 
                $this->logout();
                return false;
            } else {
                return $this->startSession($username[0]);
            }
        } else {
            return false;
        }
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
            SessionCtrl_Production::$user = clone $s;
        }
        session_write_close();
        return true;
    }

    /**
     * Redirects to login page
     */
    public function login() {
        header("location:" . $this->loginServiceUrl . '?original_uri=' . urlencode($this->origin . "&return=1"));
    }

    /**
     * No need for current implementation
     */
    public function loginPost($username, $password) {
        
    }

    public function logout($deleteCookies = false) {
        session_start();
        unset($_SESSION['user']);
        session_unset();
        session_destroy();
        session_write_close();
        SessionCtrl_Production::$userId = -1;
        SessionCtrl_Production::$username = "";
        SessionCtrl_Production::$userRole = "undefined";
        SessionCtrl_Production::$user= null;
        if ($deleteCookies) {
            setcookie("SSO", "", time() - 3600, '/', '.netbeans.org'); // remove cookie as well
            setcookie("SSO", "", time() - 3600, '/', 'netbeans.org');
            setcookie("_junction2_session", "", time() - 3600, '/', 'netbeans.org');
            setcookie("_junction2_session", "", time() - 3600, '/', '.netbeans.org');
            setcookie("SSO_EXPIRATION", "", time() - 3600, '/', 'netbeans.org');
            setcookie("SSO_EXPIRATION", "", time() - 3600, '/', '.netbeans.org');

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
        //header("location:" . $this->logoutServiceUrl . '?original_uri=' . urlencode("http://services.netbeans.org/synergy"));
    }

    /**
     * Validates existing token. Makes request to target authentication endpoint to get information about session
     * @return AuthenticationResult
     */
    private function makeRequest($token) {
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_HEADER, 0);
        curl_setopt($ch, CURLOPT_URL, $this->validationServiceUrl . $token);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
        curl_setopt($ch, CURLOPT_TIMEOUT, 30);
        curl_setopt($ch, CURLOPT_FOLLOWLOCATION, 1);
        if (Synergy::hasProxy()) {
            curl_setopt($ch, CURLOPT_PROXY, Synergy::getProxy());
        }
//        curl_setopt($ch, CURLOPT_PROXYPORT, 80);

        $data = curl_exec($ch);
        $result = new AuthenticationResult($data, curl_getinfo($ch));
        curl_close($ch);
        return $result;
    }

    public static function getUserRole() {
        if (SessionCtrl_Production::$userRole === "undefined") {
            if(!is_null(SessionCtrl_Production::getUser())){
                SessionCtrl_Production::$userRole = SessionCtrl_Production::getUser()->role;
            }
        }
        return SessionCtrl_Production::$userRole;
    }

    public static function getUser() {
        if (is_null(SessionCtrl_Production::$user)) {
            if (isset($_SESSION['user']) && !is_null($_SESSION['user'])) {
                SessionCtrl_Production::$user = clone $_SESSION['user'];
            } else {
                if (!SessionCtrl_Production::$sso_checked) {
                    SessionCtrl_Production::$sso_checked = true;
                    $instance = new self;
                    try {
                        $instance->authenticate();
                    } catch (UserException $e) {
                        // ignore here
                    }
                }
            }
        }

        return SessionCtrl_Production::$user;
    }

    public static function sessionExists() {
        if (!is_null(SessionCtrl_Production::getUser())) {
            return true;
        }
        return false;
    }

    public static function getUsername() {
        if (SessionCtrl_Production::$username === "") {
            if(!is_null(SessionCtrl_Production::getUser())){
                SessionCtrl_Production::$username= SessionCtrl_Production::getUser()->username;
            }
        }
        return SessionCtrl_Production::$username;
    }

    public static function getUserId() {
        if (SessionCtrl_Production::$userId === -1) {
            if(!is_null(SessionCtrl_Production::getUser())){
                SessionCtrl_Production::$userId = intval(SessionCtrl_Production::getUser()->userId);
            }
        }
        return SessionCtrl_Production::$userId;
    }

    public static function startAnonymousSession() {
        date_default_timezone_set('UTC');
        $_SESSION['user'] = new Session(ANONYM, 'admin', date("Y-m-d H:i:s"), -1);
    }

}

class AuthenticationResult {

    public $text;
    public $info;

    function __construct($text, $headers) {
        $this->text = $text;
        $this->info = $headers;
    }

}

?>