<?php

namespace Synergy\Controller;

use Synergy\App\Synergy;
use Synergy\Interfaces\Observer;
use Synergy\Providers\EmailCtrl;
use Synergy\Extensions\Specification\RemovalRequestExtension;

/**
 * Description of NotificationCtrl
 *
 * @author vriha
 */
class NotificationCtrl implements Observer {

    /** @var EmailCtrl */
    public $emailProvider;
    private static $listening = array("assignmentCreated", "testRunEdited", "assignmentRemovedByLeader", "requestedRemoval", "reviewAssignmentCreated", "reviewFinished");
    private $ready = false;
    private $emailsEnabled = true;

    /** @var RunCtrl  */
    private $runCtrl;
    private $reviewCtrl;
    private $specificationCtrl;
    private $userCtrl;

    function __construct() {
        $this->emailProvider = Synergy::getProvider("email");
        $this->ready = true;
        if (defined('SEND_EMAIL') && intval(SEND_EMAIL) === 0) {
            $this->emailsEnabled = false;
        }
    }

    public function getRunCtrl() {
        if (is_null($this->runCtrl)) {
            $this->runCtrl = new RunCtrl();
        }
        return $this->runCtrl;
    }

    public function getReviewCtrl() {
        if (is_null($this->reviewCtrl)) {
            $this->reviewCtrl = new ReviewCtrl();
        }
        return $this->reviewCtrl;
    }

    /**
     * 
     * @return SpecificationCtrl
     */
    public function getSpecificationCtrl() {
        if (is_null($this->specificationCtrl)) {
            $this->specificationCtrl = new SpecificationCtrl();
        }
        return $this->specificationCtrl;
    }

    /**
     * 
     * @return UserCtrl
     */
    public function getUserCtrl() {
        if (is_null($this->userCtrl)) {
            $this->userCtrl = new UserCtrl();
        }
        return $this->userCtrl;
    }

    /**
     * Listens on events and if necessary handles the event
     * @param String $name message name
     * @param mixed $data data 
     */
    public static function on($name, $data) {
        if (in_array($name, NotificationCtrl::$listening)) {
            $instance = new self();
            if ($instance->ready) {
                $instance->handleEvent($name, $data);
            }
        }
    }

    /**
     * Handles particular event
     * @param String $name event name
     * @param mixed $data received data with event
     */
    public function handleEvent($name, $data) {
        switch ($name) {
            case "assignmentCreated":
                $this->sendNewAssignmentEmail($data);
                break;
            case "testRunEdited":
                $this->sendTestRunUpdatedEmail($data);
                break;
            case "assignmentRemovedByLeader":
                $this->sendTestAssignmentRemovedEmail($data);
                break;
            case "requestedRemoval":
                $this->sendRemovalRequest($data);
                break;
            case "reviewAssignmentCreated":
                $this->sendNewReviewAssignmentEmail($data);
                break;
            case "reviewFinished":
                $this->sendReviewFinishedEmail($data);
                break;
            default:
                break;
        }
    }

    private function sendNewAssignmentEmail($assignmentId) {
        if (!$this->emailsEnabled) {
            return;
        }
        $assignment = $this->getRunCtrl()->getBasicAssignment($assignmentId);
        $ctrl = new UserCtrl();
        $user = $ctrl->getUser($assignment->username);
        if (!is_null($assignment) && $user->emailNotifications) {
            $body = ($this->emailProvider->useHTML) ? $assignment->getCreatedEmailBodyHTML() : $assignment->getCreatedEmailBody();
            if (!is_null($user->email) && strlen($user->email) > 0) {
                $email = $this->emailProvider->compose($body, $assignment->getCreatedEmailSubject(), $user->email);
            } else {
                $email = $this->emailProvider->compose($body, $assignment->getCreatedEmailSubject(), $assignment->username . '@' . DOMAIN);
            }

            $this->emailProvider->send($email);
        }
    }

    private function sendNewReviewAssignmentEmail($assignmentId) {
        if (!$this->emailsEnabled) {
            return;
        }
        $assignment = $this->getReviewCtrl()->getBasicAssignment($assignmentId);
        $ctrl = new UserCtrl();
        $user = $ctrl->getUser($assignment->username);
        if (!is_null($assignment) && $user->emailNotifications) {
            $body = ($this->emailProvider->useHTML) ? $assignment->getCreatedEmailBodyHTML() : $assignment->getCreatedEmailBody();
            if (!is_null($user->email) && strlen($user->email) > 0) {
                $email = $this->emailProvider->compose($body, $assignment->getCreatedEmailSubject(), $user->email);
            } else {
                $email = $this->emailProvider->compose($body, $assignment->getCreatedEmailSubject(), $assignment->username . '@' . DOMAIN);
            }
            $this->emailProvider->send($email);
        }
    }

    public function sendTestRunUpdatedEmail($testRunId) {
        if (!$this->emailsEnabled) {
            return;
        }
        $assignees = $this->getRunCtrl()->getAssignees($testRunId);
        $testRun = $this->getRunCtrl()->getRunOverview($testRunId);
        foreach ($assignees as $assignee) {
            if ($assignee->emailNotifications) {
                $body = ($this->emailProvider->useHTML) ? $testRun->getUpdatedEmailBodyHTML() : $testRun->getUpdatedEmailBody();
                if (!is_null($assignee->email) && strlen($assignee->email) > 0) {
                    $email = $this->emailProvider->compose($body, $testRun->getCUpdatedEmailSubject(), $assignee->email);
                } else {
                    $email = $this->emailProvider->compose($body, $testRun->getCUpdatedEmailSubject(), $assignee->username . '@' . DOMAIN);
                }
                $this->emailProvider->send($email);
            }
        }
    }

    /**
     * Sends notification about removed assignment to assignees
     * @param \Synergy\Model\TestAssignment $assignment
     * @return type
     */
    private function sendTestAssignmentRemovedEmail($assignment) {
        if (!$this->emailsEnabled) {
            return;
        }

        $ctrl = new UserCtrl();
        $user = $ctrl->getUser($assignment->username);
        if (!is_null($assignment) && $user->emailNotifications) {
            $body = ($this->emailProvider->useHTML) ? "<b>Message from tribe leader:</b> " . $assignment->getRemovalComment() . "<br/>" : "Message from tribe leader: " . $assignment->getRemovalComment() . "\r\n";
            $body = $body . (($this->emailProvider->useHTML) ? $assignment->getCreatedEmailBodyHTML() : $assignment->getCreatedEmailBody());
            if (!is_null($user->email) && strlen($user->email) > 0) {
                $email = $this->emailProvider->compose($body, $assignment->getRemovedEmailSubject(), $user->email);
            } else {
                $email = $this->emailProvider->compose($body, $assignment->getRemovedEmailSubject(), $assignment->username . '@' . DOMAIN);
            }
            $this->emailProvider->send($email);
        }
    }

    /**
     * Sends email notification to specification owner asking to delete the specification
     * @param type $specificationId
     */
    public function sendRemovalRequest($specificationId) {
        $specification = $this->getSpecificationCtrl()->getSpecificationOverview($specificationId);
        $ctrl = new UserCtrl();
        $owner = $ctrl->getUser($specification->owner);
        if (!is_null($specification)) {
            $users = RemovalRequestExtension::getRequestsForSpecification($specificationId);
            $_u = "";
            foreach ($users as $u) {
                $_u = $_u . $u->username . ", ";
            }
            $_u = substr($_u, 0, strlen($_u) - 2);
            $body = ($this->emailProvider->useHTML) ? $specification->removalRequestBodyHTML($_u) : $specification->removalRequestBody($_u);
            if (!is_null($specification->owner) && $specification->owner !== "import") {
                if (!is_null($owner->email) && strlen($owner->email) > 0) {
                    $email = $this->emailProvider->compose($body, $specification->getRemovalEmailSubject(), $owner->email);
                } else {
                    $email = $this->emailProvider->compose($body, $specification->getRemovalEmailSubject(), $specification->owner . '@' . DOMAIN);
                }
                $this->emailProvider->send($email);
            } else {
                // send to manager
                $managers = $this->getUserCtrl()->getManagers();
                foreach ($managers as $man) {
                    if (!is_null($man->email) && strlen($man->email) > 0) {
                        $email = $this->emailProvider->compose($body, $specification->getRemovalEmailSubject(), $man->email);
                    } else {
                        $email = $this->emailProvider->compose($body, $specification->getRemovalEmailSubject(), $man->username . '@' . DOMAIN);
                    }
                    $this->emailProvider->send($email);
                }
            }
        }
    }

    /**
     * Sends email notification about transferring ownership of specification to specification owner
     * @param \Synergy\Model\Specification $specification
     * @param String $msg message with explanation
     * @param String $displayName requester display name
     * @param String $username requester username
     */
    public function sendOwnershipRequest($specification, $msg, $displayName, $username) {
        $ctrl = new UserCtrl();
        $owner = $ctrl->getUser($specification->owner);
        $body = ($this->emailProvider->useHTML) ? $specification->ownershipRequestBodyHTML($msg, $displayName, $username) : $specification->ownershipRequestBody($msg, $displayName, $username);
        if (!is_null($owner->email) && strlen($owner->email) > 0) {
            $email = $this->emailProvider->compose($body, $specification->getOwnershipRequestSubject(), $owner->email);
        } else {
            $email = $this->emailProvider->compose($body, $specification->getOwnershipRequestSubject(), $specification->owner . '@' . DOMAIN);
        }

        $this->emailProvider->send($email);
    }

    private function sendReviewFinishedEmail($assignmentId) {
        if (!$this->emailsEnabled) {
            return;
        }
        $assignment = $this->getReviewCtrl()->getAssignmentWithoutComments($assignmentId, false);
        $body = ($this->emailProvider->useHTML) ? $assignment->getFinishedEmailBodyHTML() : $assignment->getFinishedEmailBody();
        $email = $this->emailProvider->compose($body, $assignment->getFinishedEmailSubject(), $assignment->owner);
        $this->emailProvider->send($email);
    }

}

?>
