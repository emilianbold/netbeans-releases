<?php

namespace Synergy\Controller;

use Synergy\DB\SpecificationRelationDAO;
use Synergy\Extensions\Tribe\TribeSpecificationExtension;

/**
 * Description of SpecRelationCtrl
 *
 * @author vriha
 */
class SpecRelationCtrl {

    private $specRelationDao;
    private $tribeCtrl;
    private $suiteCtrl;
    private $tribeSpecExt;

    function __construct() {
        $this->specRelationDao = new SpecificationRelationDAO();
    }

    /**
     * 
     * @return TribeCtrl
     */
    public function getTribeCtrl() {
        if (is_null($this->tribeCtrl)) {
            $this->tribeCtrl = new TribeCtrl();
        }
        return $this->tribeCtrl;
    }

    /**
     * 
     * @return SuiteCtrl
     */
    public function getSuiteCtrl() {
        if (is_null($this->suiteCtrl)) {
            $this->suiteCtrl = new SuiteCtrl();
        }
        return $this->suiteCtrl;
    }

    /**
     * 
     * @return TribeSpecificationExtension
     */
    public function getTribeSpecExt() {
        if (is_null($this->tribeSpecExt)) {
            $this->tribeSpecExt = new TribeSpecificationExtension();
        }
        return $this->tribeSpecExt;
    }

    /**
     * Checks if given user is somehow related to specification. User is 
     * related if any of following is true:
     * <ul>
     * <li>user is author of specification</li>
     * <li>user is owner of specification</li>
     * <li>user is member of tribe that "has" given specification</li>
     * </ul>
     * @param int $specificationId
     * @param String $username
     * @return boolean true if he is related, false otherwise
     */
    public function isUserRelatedToSpec($specificationId, $username) {
        if ($this->specRelationDao->isDirectlyRelated($username, $specificationId)) {
            return true;
        }

        $memberships = $this->getTribeCtrl()->getUserMembership($username);

        foreach ($memberships as $m) {
            $specs = $this->getTribeSpecExt()->getSpecifications($m->id);
            foreach ($specs as $s) {
                if ($s->id === $specificationId) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if given user is somehow related to specification of given suite. User is 
     * related if any of following is true:
     * <ul>
     * <li>user is author of specification</li>
     * <li>user is owner of specification</li>
     * <li>user is member of tribe that "has" given specification</li>
     * </ul>
     * @param int $suiteId
     * @param String $username
     * @return boolean true if he is related, false otherwise
     */
    public function isUserRelatedToSuite($suiteId, $username) {
        return $this->isUserRelatedToSpec($this->getSuiteCtrl()->getSpecificationId($suiteId), $username);
    }

}
