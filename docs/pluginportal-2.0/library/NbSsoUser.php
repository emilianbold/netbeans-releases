<?php

/**
 * Implementation of the NetBeans SSO authentication
 *
 * @author janpirek
 *
 * Process:
 * 1) check session if there is NbSsoUser instance already, if not create new one
 *    (singleton here)
 * 2) run authentication() next
 * 3) authentication do the http verification of SSO cookie only if
 *    such cookie exists and user is not authenticated already
 * 4) save state and object itself in session so we do not do auth requests every time
 */
class NbSsoUser {

    protected $_SsoCookieName = null;
    protected $_isAuthenticated = false;
    protected $_username = 'guest';
    protected $_email = null;
    protected $_isIgnoredUserAgent = false;
    protected $_httpClientClass = null;
    protected $_validationServiceUrl = null;
    protected $_verifier = null;
    protected $_log = null;
    protected $_logPool = array();
    protected $_lastRefreshed = 0;
    protected $_refreshRate = null;
    protected $_proxyHost;
    protected $_proxyPort;
    protected static $_instance = null;

    /**
     * singleton instance getter
     *
     * @param string $cookieName
     * @param string $ignoredAgents
     * @param string $validationServiceUrl
     * @param string $httpClientClass
     * @param integer $refreshRate Number of seconds to forced auth so we do not keep expired session
     * @param boolean $log
     * @return NbSsoUser
     */
    public static function getInstance($ssoCookieName = 'SSO', $ignoredAgents = 'curl,java,wget,robot', $validationServiceUrl = 'https://netbeans.org/api/login/validate/', $httpClientClass = "Zend_Http_Client", $refreshRate = 3600, $log = false, $proxyHost = '', $proxyPort = '') {
        if (null === self::$_instance) {
            // create instace of Zend Session, use namespace 'netbeansSso'
            $authSession = new Zend_Session_Namespace('netbeansSso');
            if (isset($authSession->user)) {
                // we have user in session, let's use that one
                self::$_instance = $authSession->user;
                self::$_instance->log('-------------------------');
                self::$_instance->log('Creating User form session');
                // run authentication
                if (self::$_instance->_isIgnoredUserAgent == false)
                    self::$_instance->authenticate();
            } else {
                // forst time access, create user
                self::$_instance = new self($ssoCookieName, $ignoredAgents, $httpClientClass, $validationServiceUrl, $refreshRate, $log, $proxyHost, $proxyPort);
                $authSession->user = self::$_instance;
            }
        }
        return self::$_instance;
    }

    /**
     * Is user authenticated or guest?
     * @return boolean
     */
    public function isAuthenticated() {
        if ($this->_username != 'guest' && $this->_isAuthenticated) {
            return true;
        } else {
            return false;
        }
    }

    public function isVerifier() {
        if ($this->_verifier !== null) {
            return $this->_verifier;
        } else {
            $v = Doctrine_Query::create()->from('PpVerifier')->where('userid=?', $this->getUsername())->execute();
            if ($v->count() > 0) {
                $this->_verifier = true;
            } else {
                $this->_verifier = false;
            }
            return $this->_verifier;
        }
    }

    /**
     *  Constructor
     *
     * @param strings $cookieName
     * @param string $ignoredAgents
     * @param type $httpClientClass
     * @param type $validationServiceUrl
     * @param integer $refreshRate Number of seconds to forced auth so we do not keep expired session
     * @param type $log
     * @return type
     */
    protected function __construct($ssoCookieName, $ignoredAgents, $httpClientClass, $validationServiceUrl, $refreshRate, $log, $proxyHost, $proxyPort) {
        $this->_httpClientClass = $httpClientClass;
        $this->_log = $log;
        $this->_SsoCookieName = $ssoCookieName;
        $this->_validationServiceUrl = $validationServiceUrl;
        $this->_refreshRate = $refreshRate;
        $this->_proxyHost = $proxyHost;
        $this->_proxyPort = $proxyPort;
        $this->log('-------------------------');
        $this->log("There is no User in Session, calling __construct()");
        // check if user agent is on blacklist so we do not auth robots etc...
        if ($this->isUserAgentIgnored($ignoredAgents))
            return true;
        // run authentication
        $this->authenticate();
        $this->_lastRefreshed = time();
    }

    /**
     * Check if User Agent string of the request client is on the blasklist by config
     * to speed things up so services like crawlers, IDE etc does not have to wait for SSO...
     *
     * @param string $ignoredAgents List of ignorefd agents separated by comma
     * @return boolean
     */
    protected function isUserAgentIgnored($ignoredAgents) {
        if (isset($this->_isIgnoredUserAgent))
            return $this->_isIgnoredUserAgent;
        if ($ignoredAgents) {
            $agents = explode(',', $ignoredAgents);
            foreach ($agents as $agent) {
                if (stristr($_SERVER['HTTP_USER_AGENT'], trim($agent))) {
                    $this->_isIgnoredUserAgent = true;
                    $this->log('User agent is recognized for blacklist: ' . $agent);
                    return true;
                }
            }
        }
        $this->_isIgnoredUserAgent = false;
        $this->log('User agent is not on blacklist, proceeding.');
        return false;
    }

    /**
     * Log message
     * @param string $msg Message to log
     */
    protected function log($msg) {
        if ($this->_log)
            $this->_logPool[] = array(date("Y-m-d H:i:s"), $msg);
    }

    /**
     * Get nicely formated log for eg browser
     * @return string nice formated log
     */
    public function getLog() {
        if ($this->_log) {
            $out = '<b>USER Log</b><br>';
            foreach ($this->_logPool as $l) {
                $out.=$l[0] . ' : ' . $l[1] . '<br>';
            }
        } else {
            $out = 'Log disabled by config';
        }
        return $out;
    }

    /**
     * Authentication method
     * @return void
     */
    public function authenticate() {
        $this->log('Authentication starts');
        // check when last refreshed occured, so we do not keep user logged when nb.org session expired
        $checkAgain = ((time() - $this->_lastRefreshed - $this->_refreshRate) > 0) ? true : false;
        if ($checkAgain) {
            $this->log('Forced authentication as refresh time is over. Last refreshed: ' . date('Y-m-d H:i:s', $this->_lastRefreshed));
        }

        if ($this->_isAuthenticated == true && !$checkAgain) {
            $this->log('Already authenticated as ' . $this->_username . '. Bye.');
            return;
        }
        if (empty($_COOKIE[$this->_SsoCookieName])) {
            $this->log('Missing SSO cookie, giving up authentication.');
            return;
        }
        if ((!$this->_isAuthenticated && !empty($_COOKIE[$this->_SsoCookieName])) || $checkAgain) {
            $this->log('Trying to authenticate as we have SSO cookie and not authenticated yet or forced auth after refresh time');
            // fire up http client and try to validate sso cookie, woohoo
            $this->_lastRefreshed = time();
            try {
                $client = new $this->_httpClientClass();
                $client->setUri($this->_validationServiceUrl . $_COOKIE[$this->_SsoCookieName]);
                $client->setConfig(array(
                    'strict' => false,
                    'adapter' => 'Zend_Http_Client_Adapter_Proxy',
                    'proxy_host' => $this->_proxyHost,
                    'proxy_port' => $this->_proxyPort
                ));
                $this->log('Client: ' . $this->_httpClientClass . ', uri: ' . $this->_validationServiceUrl . $_COOKIE[$this->_SsoCookieName]);
                $response = $client->request("GET");
                if (!$response->isSuccessful()) {
                    $this->log('Auhentication Request not successfull, user not authenticated, clearing user up and  bailing out, Status: ' . $response->getStatus());
                    $this->_isAuthenticated = false;
                    $this->_username = 'guest';
                    $this->_email = null;
                    // invalidate cookie so we do not auth it again
                    setcookie('SSO', 'zero', time() - 36000, '/');
                    unset($_COOKIE['SSO']);
                    return;
                }
                list($username, $chunk, $chunk2) = explode(':', urldecode($_COOKIE[$this->_SsoCookieName]));
                $this->log('Authentication successfull, setting up username: ' . $username . ', email: ' . $username . '@netbeans.org');
                if (!empty($username)) {
                    $this->_username = $username;
                    $this->_email = $username . '@netbeans.org';
                    $this->_isAuthenticated = true;
                }
            } catch (Exception $e) {
                $this->log('Authentication failed with exception: ' . $e->getMessage().($response ? $response->getBody() : ''));
            }
        }
    }

    /**
     * Set the ath refresh rate to enforce cookie validity check
     * @param int $rate Refresh rate
     */
    public function setRefreshRate($rate) {
        $this->_refreshRate = (int) $rate;
    }

    /**
     * Set log to enable 1 /disable 0 logging
     * @param int $log
     */
    public function setLog($log) {
        $this->_log = $log;
    }

    /**
     * Get username
     * @return string
     */
    public function getUsername() {
        return $this->_username;
    }

    /**
     * Get user's email
     * @return string
     */
    public function getEmail() {
        return $this->_email;
    }

    // Do not allow the clone operation: $x = clone $v;
    final private function __clone() {

    }

}

?>
