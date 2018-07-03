<?php

namespace Synergy\Observer;

use Synergy\Controller\SpecificationCtrl;
use Synergy\Interfaces\Observer;

/**
 * @author vriha
 */
class SpecificationObserver implements Observer {

    private static $listening = array('specificationUpdated');

    /** @var SpecificationCtrl  */
    private $specificationCtrl;

    public function getSpecificationCtrl() {
        if (is_null($this->specificationCtrl)) {
            $this->specificationCtrl = new SpecificationCtrl();
        }
        return $this->specificationCtrl;
    }

    /**
     * Listens on events and if necessary handles the event
     * @param String $name message name
     * @param mixed $data data 
     */
    public static function on($name, $data) {
        if (in_array($name, SpecificationObserver::$listening)) {
            $instance = new self();
            $instance->handleEvent($name, $data);
        }
    }

    /**
     * Handles particular event
     * @param String $name event name
     * @param mixed $data received data with event
     */
    public function handleEvent($name, $data) {
        switch ($name) {
            case 'specificationUpdated':
                $this->getSpecificationCtrl()->refreshSpecification($data);
                break;
            default:
                break;
        }
    }
}

?>
