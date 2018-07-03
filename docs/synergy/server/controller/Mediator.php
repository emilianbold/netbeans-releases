<?php

namespace Synergy\Controller;

use Exception;
use ReflectionClass;
use Synergy\App\Synergy;
use Synergy\Misc\Util;

/**
 * Description of Mediator
 *
 * @author lada
 */
class Mediator {

    private static $listeners = array();
    private static $isReady = false;

    /**
     * Emits event. Target listener has to implement method on($name, $object) from Observer interface
     * @param String $name event name
     * @param Object $object data to be send with this event
     */
    public static function emit($name, $object) {
        if(!Synergy::$mediatorEnabled){ // for tests
            return;
        }
        if (!Mediator::$isReady) {
            Mediator::init();
        }
        foreach (Mediator::$listeners as $c) {
            try {
                $c->getMethod('on')->invoke(null, $name, $object);
            } catch (Exception $e) {
                $logger = Synergy::getProvider("logger");
                $logger::log($e->getMessage());
            }
        }
    }

    /**
     * Initializes array of listeners.
     */
    public static function init() {
        $path = array(
            '../model/',
            '../interfaces/',
            '../misc/',
            '../providers/',
            '../listeners/',
            '../observer/',
            '../controller/',
            '../db/',
            '../extensions/specification/',
            '../extensions/tribe/',
            '../extensions/suite/',
            '../extensions/testcase/'
        );
        foreach ($path as $p) {
            if (is_dir(dirname(__FILE__) . '/' . $p)) {
                foreach (scandir($p) as $file) {
                    if (is_file($p . $file) && Util::endsWith($file, ".php") && $file != "Observer.php") {
                        $class = str_replace(".php", "", $file);
                        $ns = (substr($p, 3));
                        $ns = str_replace(DIRECTORY_SEPARATOR, '\\', $ns);
                        $ns = "\\Synergy\\" . $ns;
                        $reflectionClass = new ReflectionClass($ns . $class);
                        if ($reflectionClass->implementsInterface('\\Synergy\Interfaces\\Observer')) {
                            array_push(Mediator::$listeners, $reflectionClass);
                        }
                    }
                }
            }
        }
        Mediator::$isReady = true;
    }

}

?>
