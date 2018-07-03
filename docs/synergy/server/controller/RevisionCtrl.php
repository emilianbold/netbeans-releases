<?php

namespace Synergy\Controller;

use Synergy\App\Synergy;
use Synergy\DB\RevisionDAO;
use Synergy\Interfaces\Observer;
use Synergy\Model\Revision;

/**
 * Description of RevisionCtrl
 *
 * @author vriha
 */
class RevisionCtrl implements Observer {

    public static $listening = array('addRevision', 'removeRevisions', 'addRevisionSuite', 'addRevisionCase');
    private $specCtrl;
    private $suiteCtrl;
    private $revisionDao;
    
    public static function on($name, $data) {
        if (in_array($name, RevisionCtrl::$listening)) {
            $instance = new self();
            $instance->handleEvent($name, $data);          
        }
    }

    private function getSpecCtrl() {
        if (is_null($this->specCtrl)) {
            $this->specCtrl = new SpecificationCtrl();
        }
        return $this->specCtrl;
    }
    private function getSuiteCtrl() {
        if (is_null($this->suiteCtrl)) {
            $this->suiteCtrl = new SuiteCtrl();
        }
        return $this->suiteCtrl;
    }
    
    private function getRevisionDao() {
        if (is_null($this->revisionDao)) {
            $this->revisionDao = new RevisionDAO();
        }
        return $this->revisionDao;
    }
    /**
     * Returns list of revisions for given specification
     * @param type $specificationId
     * @return Revision[]
     */
    public function getListOfRevisions($specificationId) {
        return $this->getRevisionDao()->getListOfRevisions($specificationId);
    }
    
    private function handleEvent($name, $data) {
        if(isset($_REQUEST['minorEdit']) && $_REQUEST['minorEdit']=== "true"){
            return;
        }
        switch ($name) {
            case 'addRevision':
                $this->specificationUpdated($data);
                break;
            case 'removeRevisions':
                $this->specificationDeleted($data);
                break;
            case 'addRevisionSuite':
                $specificationId = $this->getSuiteCtrl()->getSpecificationId($data);
                $this->specificationUpdated($specificationId);
                break;
            case 'addRevisionCase':
                $this->caseUpdated($data);
                break;
            default:
                break;
        }
    }

    private function caseUpdated($caseId){
        $specs = $this->getSuiteCtrl()->getAllSpecificationsForCase($caseId);
        for ($i = 0, $max = count($specs); $i < $max; $i++) {
            $this->specificationUpdated($specs[$i]);
        }
    }
    
    private function specificationUpdated($specificationId) {
        $specification = $this->getSpecCtrl()->getSpecificationFull($specificationId);
        $who = Synergy::getSessionProvider()->getUsername();
        date_default_timezone_set('UTC');
        $when = date('Y-m-d H:i:s');
        $this->getRevisionDao()->addNewRevision($specificationId, $specification->toString(), $who, $when);        
    }
    private function specificationDeleted($specificationId) {
        $this->getRevisionDao()->deleteRevisions($specificationId);
    }
    /**
     * Returns revision with given ID for given specification
     * @param type $id
     * @param type $specificationId
     * @return \Synergy\Model\Revision
     */
    public function getRevisionById($id, $specificationId) {
        if($id === -1){
            $specification = $this->getSpecCtrl()->getSpecificationFull($specificationId);
            return new Revision(-1, $id, $specification->toString(), $specification->author, $specification->lastUpdated);
        }else{
            return $this->getRevisionDao()->getRevisionById($id);
        }
    }

}
