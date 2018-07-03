<?php

namespace Synergy\Extensions\Specification;

use Synergy\DB\RemovalDAO;
use Synergy\Interfaces\ExtensionInterface;
use Synergy\Model\Specification;

/**
 * Description of RemovalRequestExention
 *
 * @author vriha
 */
class RemovalRequestExtension implements ExtensionInterface {

    private $removalDao;

    function __construct() {
        $this->removalDao = new RemovalDAO();
    }

    public function create($object, $newId) {
        
    }

    public function delete($specificationId) {
        $this->removalDao->removeRequestsForSpecification($specificationId);
    }

    public function edit($object) {
        
    }

    /**
     * 
     * @param Specification $object
     */
    public function get($object) {
        if (!is_null($object)) {
            $object->ext['removalRequests'] = $this->getRequests($object->id);
            return $object;
        }
        return null;
    }

    private function getRequests($specificationId) {
        return $this->removalDao->getRequestsForSpecification($specificationId);
    }

    public static function createRemovalRequest($specificationId, $userId) {
        $i = new RemovalRequestExtension();
        if(!$i->removalDao->requestExists($specificationId, $userId)){
            $i->removalDao->createRequest($specificationId, $userId);
        }
    }

    public static function getRequestsForSpecification($specificationId) {
        $i = new RemovalRequestExtension();
        return $i->removalDao->getRequestsForSpecification($specificationId);
    }

}
