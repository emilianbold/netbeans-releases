<?php

namespace Synergy\Extensions\Tribe;

use Synergy\Controller\VersionCtrl;
use Synergy\Model\Version;
use Synergy\DB\TribeExtensionDAO;
use Synergy\Interfaces\ExtensionInterface;
use Synergy\Interfaces\Observer;

/**
 * Description of TribeSpecificationExtension
 *
 * @author vriha
 */
class TribeSpecificationExtension implements ExtensionInterface, Observer {

    private $tribeExtDao;
    private static $listening = array('specificationDeleted');

    function __construct() {
        $this->tribeExtDao = new TribeExtensionDAO();
    }

    public function create($object, $newId) {
        
    }

    public function delete($tribeId) {
        $this->tribeExtDao->removeAllSpecifications($tribeId);
    }

    public function removeSpecificationFromTribes($specificationId) {
        $this->tribeExtDao->removeSpecificationFromTribes($specificationId);
    }

    public function edit($object) {
        
    }

    public function get($object) {
        if (!is_null($object)) {
            $specs = $this->getSpecifications($object->id);
            $versionCtrl = new VersionCtrl();
            $latestVersion = $versionCtrl->getLatestVersion();
            $_lv = Version::toFloat($latestVersion->name);
            for ($i = 0, $max = count($specs); $i < $max; $i++) {
                if (Version::toFloat($specs[$i]->version) < $_lv) {
                    $specs[$i]->newerId = $this->tribeExtDao->getLatestSpecification($specs[$i]->id, $specs[$i]->getSimpleName(), $specs[$i]->version);
                }
            }

            $object->ext['specifications'] = $specs;
            return $object;
        }
        return null;
    }

    public function getSpecifications($tribeId) {
        return $this->tribeExtDao->getSpecifications($tribeId);
    }

    public function addSpecificationToTribe($tribeId, $specificationId) {
        return $this->tribeExtDao->addSpecificationToTribe($tribeId, $specificationId);
    }

    public function removeSpecificationFromTribe($tribeId, $specificationId) {
        return $this->tribeExtDao->removeSpecificationFromTribe($tribeId, $specificationId);
    }

    public static function on($name, $data) {
        if (in_array($name, TribeSpecificationExtension::$listening)) {
            $instance = new self();
            $instance->removeSpecificationFromTribes($data);
        }
    }

}
