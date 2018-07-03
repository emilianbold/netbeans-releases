<?php
namespace Synergy\Controller;

use Synergy\DB\PlatformDAO;
use Synergy\DB\RunDAO;
use Synergy\Model\Platform;



/**
 * Description of PlatformCtrl
 *
 * @author lada
 */
class PlatformCtrl {

    private $platformDao;
    private $runDao;

    function __construct() {
        $this->platformDao = new PlatformDAO();
        $this->runDao = new RunDAO();
    }

    /**
     * Updates platform name
     * @param int $id platform ID
     * @param string $name new name
     * @return boolean true on success
     */
    public function updatePlatform($id, $name, $isActive = true) {
        if ($this->platformDao->isNameUsed($name, $id))
            return false;
        return $this->platformDao->updatePlatform($id, $name, $isActive);
    }

    /**
     * Returns all platforms
     * @return Platform[]
     */
    public function getPlatforms() {
        return $this->platformDao->getPlatforms();
    }

    /**
     * Creates a new platform
     * @param string $name new platform name
     * @return boolean true on success
     */
    public function createPlatform($name) {
        if ($this->platformDao->isNameUsed($name, -1))
            return false;
        return $this->platformDao->createPlatform($name);
    }

    /**
     * Removes platform and all test assignments for it
     * @param int $id platform ID
     * @return boolean true on success
     */
    public function deletePlatform($id) {
        $this->runDao->deleteAssignmentProgressForPlatform($id); // this is ok to delete
        $this->runDao->deleteAssignmentsForPlatform($id);
        Mediator::emit("refreshLocks", -1);
        return $this->platformDao->deletePlatform($id);
    }
    
    /**
     * Returns platforms that matches given paramenter
     * @param string $query platform name
     * @return Platform[]
     */
    public function findMatchingPlatform($query) {
        return $this->platformDao->findMatchingPlatform($query);
    }
}

?>
