<?php

namespace Synergy\Model\Run;

/**
 * Description of RunNotification
 *
 * @author vriha
 */
class RunNotification {

    public $username;
    public $runId;
    public $runTitle;
    private $assignmentsIds;
    private $reviewsIds;
    private $reviewUrls;
    private $specificationsTitles;
    public $message;
    public $email;

    function __construct($username, $runTitle, $runId) {
        $this->username = $username;
        $this->runId = intval($runId);
        $this->runTitle = $runTitle;
        $this->assignmentsIds = array();
        $this->reviewUrls = array();
        $this->reviewsIds = array();
        $this->specificationsTitles = array();
    }

    /**
     * 
     * @param \Synergy\Model\TestAssignment $assignment
     */
    public function addAssignment($assignment) {
        if (!in_array($assignment->id, $this->assignmentsIds)) {
            array_push($this->assignmentsIds, $assignment->id);
            array_push($this->specificationsTitles, $assignment->specification . " (" . $assignment->platform . ")");
        }
    }

    public function getAssignmentsIds() {
        return $this->assignmentsIds;
    }

    public function getEmailBodyHTML($daysLeft, $deadline) {
        $msg = (intval($daysLeft) === 0 ) ? "Test Run <a href='" . SYNERGY_URL . "client/app/#/run/" . $this->runId . "'>" . $this->runTitle . "</a> ends in less than a day, deadline is " . $deadline : "Test Run <a href='" . SYNERGY_URL . "client/app/#/run/" . $this->runId . "'>" . $this->runTitle . "</a> ends in " . $daysLeft . " day(s), deadline is " . $deadline;
        $msg = $msg . "<br/>You have " . count($this->assignmentsIds) . " test assignment(s) that are not yet finished for following specifications:<ul>";
        foreach ($this->specificationsTitles as $title) {
            $msg = $msg . "<li>" . $title . "</li>";
        }
        $msg = $msg . "</ul>";

        if (count($this->reviewsIds) > 0) {
            $msg = $msg . "<br/> Please make sure you have completed review of following documents:<ul>";
            foreach ($this->reviewUrls as $title) {
                $msg = $msg . "<li>" . $title . "</li>";
            }
        }

        return $msg . "</ul>";
    }

    public function getEmailBody($daysLeft, $deadline) {
        $msg = (intval($daysLeft) === 0 ) ? "Test Run " . $this->runTitle . " (see [1]) ends in less than a day, deadline is " . $deadline : "Test Run " . $this->runTitle . " (see [1]) ends in " . $daysLeft . " day(s), deadline is " . $deadline;
        $msg = $msg . "<br/>You have " . count($this->assignmentsIds) . " test assignment(s) that are not yet finished for following specifications:";
        foreach ($this->specificationsTitles as $title) {
            $msg = $msg . "\r\n" . $title . "";
        }

        if (count($this->reviewsIds) > 0) {
            $msg = $msg . "\r\n Please make sure you have completed review of following documents:";
            foreach ($this->reviewUrls as $title) {
                $msg = $msg . "\r\n" . $title;
            }
        }

        return $msg . "\r\n \r\n [1] " . SYNERGY_URL . "client/app/#/run/" . $this->runId;
    }

    public function getEmailSubject($daysLeft) {
        return (intval($daysLeft) === 0) ? "Test run '" . $this->runTitle . "' ends in less than a day" : "Test run " . $this->runTitle . " ends in " . $daysLeft . " day(s)";
    }

    public function addReview($review) {
        if (!in_array($review->id, $this->reviewsIds)) {
            array_push($this->reviewsIds, $review->id);
            array_push($this->reviewUrls, $review->reviewUrl);
        }
    }

}
