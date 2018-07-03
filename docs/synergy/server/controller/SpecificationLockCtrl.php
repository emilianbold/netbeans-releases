<?php

namespace Synergy\Controller;

use Synergy\DB\LockDAO;
use Synergy\Interfaces\Observer;

/**
 * Description of SpecificationLockCtrl
 *
 * @author vriha
 */
class SpecificationLockCtrl implements Observer {

    const LOCK_TIMEOUT = 10800; // in seconds
    public static $locksRefreshed = false;
    public static $listening = array("assignmentDeleted", "refreshLocks");
    private $lockDao;

    function __construct() {
        $this->lockDao = new LockDAO();
    }

    /**
     * Adds lock record to specification, if there is already lock, refreshes id
     * @param type $specificationId
     */
    public function addLock($specificationId, $assignmentId) {
        if ($this->getLock($specificationId, $assignmentId) < 0) {
            $this->lockDao->addLock($specificationId, $assignmentId);
        } else {
            $this->lockDao->refreshLock($specificationId, $assignmentId);
        }
    }

    /**
     * Returns lock (timestamp in seconds) for given specification or -1 if there's none
     * @param type $specificationId
     * @return type
     */
    public function getLock($specificationId, $assignmentId) {
        return $this->lockDao->getLock($specificationId, $assignmentId);
    }

    /**
     * Returns true or false if there is a lock on specification and it is active (aka not old)
     * @param type $specificationId
     * @return type
     */
    public function isSpecificationLocked($specificationId) {
        $isLocked = false;
        $locks = $this->getLocks($specificationId);
        foreach ($locks as $lockTime) {
            if ((time() - $lockTime) <= SpecificationLockCtrl::LOCK_TIMEOUT) {
                $isLocked = true;
            }
        }
        $this->lockDao->removeOldLocks(time() - SpecificationLockCtrl::LOCK_TIMEOUT);
        return $isLocked;
    }

    public static function on($name, $data) {
        if (in_array($name, SpecificationLockCtrl::$listening)) {
            $instance = new self();
            $instance->handleEvent($name, $data);
        }
    }

    private function handleEvent($name, $data) {
        switch ($name) {
            case "assignmentDeleted":
                $this->lockDao->removeLocksForAssignment($data);
                break;
            case "refreshLocks":
                if(!SpecificationLockCtrl::$locksRefreshed){ // in case of multiple events in single request, let's save some load
                    $this->lockDao->removeLocksWithoutAssignment();    
                    SpecificationLockCtrl::$locksRefreshed = true;
                }
                break;
            default:
                break;
        }
    }

    public function removeLock($lockId) {
        $this->lockDao->removeLock($lockId);
    }
    public function removeLockForAssignment($assignmentId) {
        $this->lockDao->removeLocksForAssignment($assignmentId);
    }

    /**
     * Returns array of integeres representing IDs for locks in given specification
     * @param type $specificationId
     * @return array
     */
    public function getLocks($specificationId) {
        return $this->lockDao->getLocks($specificationId);
    }
}
