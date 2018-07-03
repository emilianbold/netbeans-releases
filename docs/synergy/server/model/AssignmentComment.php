<?php

namespace Synergy\Model;

use Synergy\App\Synergy;

/**
 * Description of AssignmentComment
 *
 * @author vriha
 */
class AssignmentComment {

    public $id;
    public $assignmentId;
    public $resolution;
    public $caseId;
    public $suiteId;
    public $resolverId;
    public $commentTypeId;
    public $commentText;
    public $specificationTitle;
    public $specificationId;
    public $caseTitle;
    public $authorUsername;
    public $authorDisplayName;
    public $commentFreeText;
    private $hash;

    function __construct($id, $assignmentId, $resolution, $caseId, $suiteId, $resolverId, $commentTypeId, $commentFreeText) {
        $this->id = intval($id);
        $this->assignmentId = intval($assignmentId);
        $this->resolution = $resolution;
        $this->caseId = intval($caseId);
        $this->suiteId = intval($suiteId);
        $this->commentTypeId = intval($commentTypeId);
        $this->resolverId = intval($resolverId);
        $this->commentFreeText = $commentFreeText;
    }

    public static function canEdit() {
        $role = Synergy::getSessionProvider()->getUserRole();
        switch ($role) {
            case "tester":
            case "admin":
            case "manager":
            case "privilegedTester":
                return true;
            default:
                return false;
        }
    }
    
    public function getHash() {
        return $this->hash;
    }

    public function setHash($hash) {
        $this->hash = $hash;
    }



}
