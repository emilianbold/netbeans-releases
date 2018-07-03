<?php

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

namespace Synergy\App;

use Synergy\Controller\ConfigurationCtrl;

//require_once '../lib/htmllawed/htmLawed.php'; // include here to avoid conflict with production config.php
/**
 * Description of Synergy
 *
 * @author lada
 */
class Synergy {

    private static $providers;
    public static $mediatorEnabled = true;

    public static function init() {
        Synergy::initProviders();
        spl_autoload_register('Synergy\App\Synergy::autoload_NS');
        Synergy::initSettings();
        ini_set('session.gc_maxlifetime', SESSION_TIMEOUT);
        //  Synergy::setAnonymousCreateDelete();
    }

    public static function initPHPUnit() {
        Synergy::initProviders();
        Synergy::$mediatorEnabled = false;
        ini_set('session.gc_maxlifetime', SESSION_TIMEOUT);
    }

    /**
     * Inits all providers - simply registers names of classes to assoc array where
     * key custom name
     */
    private static function initProviders() {
        Synergy::$providers = array();
        Synergy::$providers['email'] = 'Synergy\\Providers\\EmailCtrl';
        Synergy::$providers['issue_bugzilla'] = 'Synergy\\Providers\\IssueCtrl';
        Synergy::$providers['issue_other'] = 'Synergy\\Providers\\IssueOtherCtrl';
        Synergy::$providers['logger'] = 'Synergy\\Providers\\LoggerCtrl';
        Synergy::$providers['session'] = 'Synergy\\Providers\\SessionCtrl';
        Synergy::$providers['products'] = 'Synergy\\Providers\\ProductCtrl';
        Synergy::$providers['review'] = 'Synergy\\Providers\\TutorialFormatter';
        Synergy::$providers['reviewsParser'] = 'Synergy\\Providers\\ReviewImporterCtrl';
    }

    /**
     * Set variables_order = "EGPCS" and request_order = "EGP" in php.ini so that http_proxy is found
     * @return type
     */
    public static function hasProxy() {
        return getenv('http_proxy') !== false;
    }

    public static function getProxy() {
        return getenv('http_proxy');
    }

    /**
     * Returns instance of provider or null if not found
     * @param String $name name of provider
     * @return null|\Synergy\App\provider
     */
    public static function getProvider($name) {
        if (array_key_exists($name, Synergy::$providers)) {
            $provider = Synergy::$providers[$name];
            return new $provider;
        }
        return null;
    }

    /**
     * Factory method to get instance of SessionProvider. Due to limitation of PHP <=5.3 (no variables functions),
     * it returns instance instead of class name which makes it easier to call it's (static) methods
     * @return \Synergy\Interfaces\SessionProvider
     */
    public static function getSessionProvider() {
        return new Synergy::$providers['session'];
    }

    public static function log($msg) {
        $logger = Synergy::getProvider('logger');
        $logger->log($msg);
    }

    /**
     * Autoload for PHP < 5.3 (no namespaces)
     * @param type $class
     */
    public static function autoload($class) {
        $path = array(
            __DIR__ . DIRECTORY_SEPARATOR . '..' . DIRECTORY_SEPARATOR . $class . '.php',
            __DIR__ . DIRECTORY_SEPARATOR . '..' . DIRECTORY_SEPARATOR . 'model' . DIRECTORY_SEPARATOR . $class . '.php',
            __DIR__ . DIRECTORY_SEPARATOR . '..' . DIRECTORY_SEPARATOR . 'model' . DIRECTORY_SEPARATOR . 'exception'. DIRECTORY_SEPARATOR . $class . '.php',
            __DIR__ . DIRECTORY_SEPARATOR . '..' . DIRECTORY_SEPARATOR . 'interfaces' . DIRECTORY_SEPARATOR . $class . '.php',
            __DIR__ . DIRECTORY_SEPARATOR . '..' . DIRECTORY_SEPARATOR . 'observer' . DIRECTORY_SEPARATOR . $class . '.php',
            __DIR__ . DIRECTORY_SEPARATOR . '..' . DIRECTORY_SEPARATOR . 'misc' . DIRECTORY_SEPARATOR . $class . '.php',
            __DIR__ . DIRECTORY_SEPARATOR . '..' . DIRECTORY_SEPARATOR . 'providers' . DIRECTORY_SEPARATOR . $class . '.php',
            __DIR__ . DIRECTORY_SEPARATOR . '..' . DIRECTORY_SEPARATOR . 'controller' . DIRECTORY_SEPARATOR . $class . '.php',
            __DIR__ . DIRECTORY_SEPARATOR . '..' . DIRECTORY_SEPARATOR . 'db' . DIRECTORY_SEPARATOR . $class . '.php',
            __DIR__ . DIRECTORY_SEPARATOR . '..' . DIRECTORY_SEPARATOR . 'extensions' . DIRECTORY_SEPARATOR . 'specification' . DIRECTORY_SEPARATOR . $class . '.php',
            __DIR__ . DIRECTORY_SEPARATOR . '..' . DIRECTORY_SEPARATOR . 'extensions' . DIRECTORY_SEPARATOR . 'tribe' . DIRECTORY_SEPARATOR . $class . '.php',
            __DIR__ . DIRECTORY_SEPARATOR . '..' . DIRECTORY_SEPARATOR . 'extensions' . DIRECTORY_SEPARATOR . 'suite' . DIRECTORY_SEPARATOR . $class . '.php',
            __DIR__ . DIRECTORY_SEPARATOR . '..' . DIRECTORY_SEPARATOR . 'extensions' . DIRECTORY_SEPARATOR . 'testcase' . DIRECTORY_SEPARATOR . $class . '.php'
        );
        foreach ($path as $v) {
            if (file_exists($v)) {
                require($v);
                break;
            }
        }
    }

    /**
     * Autoload for PHP 5.3+ (namespaces)
     * @param type $className
     */
    public static function autoload_NS($className) {
        $className = ltrim($className, '\\');
        $fileName = '';
        $namespace = '';

        if ($lastNsPos = strripos($className, '\\')) {
            $namespace = substr($className, 0, $lastNsPos);
            $namespace = strtolower($namespace);
            $className = substr($className, $lastNsPos + 1);
            $fileName = str_replace('\\', DIRECTORY_SEPARATOR, $namespace) . DIRECTORY_SEPARATOR;
            if (strpos($fileName, "synergy" . DIRECTORY_SEPARATOR) === 0) {
                $fileName = substr($fileName, 8);
            }
        }

        $fileName .= $className . '.php';
        require ".." . DIRECTORY_SEPARATOR . $fileName;
    }

    /**
     * Loads settings from DB
     */
    private static function initSettings() {
        $setCtrl = new ConfigurationCtrl();
        $settings = $setCtrl->loadSettings();
        for ($i = 0, $max = count($settings); $i < $max; $i++) {
            if (!is_null($settings[$i]) && strlen($settings[$i]->key) > 0 && strlen($settings[$i]->value) > 0) {
                define(strtoupper($settings[$i]->key), $settings[$i]->value);
            }
        }
    }

    /**
     * if is defined constant ANONYM (aka it is loaded from DB in initSettings(), then create fake anonymous user (to allow automated import) 
     */
    public static function setAnonymousCreateDelete() {
        $anonym = Synergy::getSessionProvider()->getUsername();
        if ($anonym === ANONYM) { // discard old ANONYM session
            $ct = Synergy::getProvider("session");
            $ct->logout(false);
        }

        if (defined('ANONYM') && $anonym === "" && isset($_GET['anonym']) && $_GET['anonym'] === ANONYM) {
            Synergy::getSessionProvider()->startAnonymousSession();
        }
    }

}

?>