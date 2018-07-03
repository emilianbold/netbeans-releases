<?php

namespace Synergy\Model;

/**
 * Description of AssignmentComments
 *
 * @author vriha
 */
class AssignmentComments {
   
    public $testRunId;
    public $testRunTitle;
    public $comments;
    
    function __construct($testRunId, $testRunTitle, $comments) {
        $this->testRunId = $testRunId;
        $this->testRunTitle = $testRunTitle;
        $this->comments = $comments;
    }

    
}
