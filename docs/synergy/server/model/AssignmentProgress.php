<?php
namespace Synergy\Model;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of AssignmentProgress
 *
 * @author lada
 */
class AssignmentProgress {
    public $assignmentId;
    /**
     *
     * @var SpecificationSkeleton 
     */
    public $specification;
    public $ownerId;
    public $status;
    
    function __construct($assignmentId, $suites, $owner) {
        $this->assignmentId = $assignmentId;
        $this->specification = $suites;
        $this->ownerId = $owner;
    }

    
}

?>
