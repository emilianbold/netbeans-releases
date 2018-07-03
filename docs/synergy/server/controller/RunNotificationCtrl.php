<?php

namespace Synergy\Controller;

use Synergy\App\Synergy;
use Synergy\DB\RunNotificationDAO;
use Synergy\Model\Run\RunNotification;
use Synergy\Model\TestAssignment;
use Synergy\Providers\EmailCtrl;
use Synergy\Misc\Util;
use Synergy\DB\DB_DAO;

/**
 * Description of RunNotificationCtrl
 *
 * @author vriha
 */
class RunNotificationCtrl {

    private $rnDao;
    private $emailsEnabled = true;

    /** @var EmailCtrl */
    private $emailProvider;

    function __construct() {
        $this->rnDao = new RunNotificationDAO();
        $this->emailProvider = Synergy::getProvider("email");
        if (defined('SEND_EMAIL') && intval(SEND_EMAIL) === 0) {
            $this->emailsEnabled = false;
        }
    }

    /**
     * Sends email notifications to all users with incomplete test assignments regardless if it has or hasn't been already sent or how many days are left
     * @param type $testRunId
     * @return int number of notified users
     */
    public function sendNotificationsNoLimits($testRunId) {
        if (!$this->emailsEnabled) {
            return 0;
        }
        $processed = array();

        $meta = $this->getRunEndDateAndNotificationLimit($testRunId);
        $deadline = $meta[0];
        date_default_timezone_set('UTC');
        $now = time();
        $runEnd = strtotime($deadline);
        $daysLeft = round(($runEnd - $now) / (60 * 60 * 24));

        $allAssignmentsIds = array();
        $allReviewsIds = array();
        $assignments = $this->getUnfinishedAssignments($testRunId);

        $reviewCtrl = new ReviewCtrl();
        $reviews = $reviewCtrl->getAssignments($testRunId);

        foreach ($assignments as $assignment) {
            if (!array_key_exists($assignment->username, $processed)) {
                $n = new RunNotification($assignment->username, $assignment->runTitle, $testRunId);
                $n->email = $assignment->getEmail();
                $processed[$n->username] = $n;
            }
            $n->addAssignment($assignment);
            array_push($allAssignmentsIds, $assignment->id);
        }

        foreach ($reviews as $review) {
            if (!array_key_exists($review->username, $processed)) {
                $n = new RunNotification($review->username, $review->runTitle, $testRunId);
                $n->email = $review->getEmail();
                $processed[$n->username] = $n;
            }
            $n->addReview($review);
            array_push($allReviewsIds, $review->id);
        }

        $notified = 0;
        $ctrl = new UserCtrl();

        foreach ($processed as $user) {
            if ($ctrl->getUser($user->username)->emailNotifications) {
                $body = ($this->emailProvider->useHTML) ? $user->getEmailBodyHTML($daysLeft, $deadline) : $user->getEmailBody($daysLeft, $deadline);
                if (!is_null($user->email) && strlen($user->email) > 0) {
                    $this->emailProvider->send($this->emailProvider->compose($body, $user->getEmailSubject($daysLeft), $user->email));
                } else {
                    $this->emailProvider->send($this->emailProvider->compose($body, $user->getEmailSubject($daysLeft), $user->username . "@" . DOMAIN));
                }
                $notified++;
            }
        }

        $this->markAssignmentsNotified($allAssignmentsIds);

        return $notified;
    }

    /**
     * Sends email notifications to all users with incomplete test assignments 
     * @param type $testRunId
     * @return int number of notified users
     */
    public function sendNotifications($testRunId) {
        if (!$this->emailsEnabled) {
            return 0;
        }
        $processed = array();

        $meta = $this->getRunEndDateAndNotificationLimit($testRunId);
        $deadline = $meta[0];
        date_default_timezone_set('UTC');
        $now = time();
        $runEnd = strtotime($deadline);
        $daysLeft = round(($runEnd - $now ) / (60 * 60 * 24));

        if (round(($runEnd - $now ) / (60 * 60)) > ($meta[1] * 24) || $daysLeft < 0 || $meta[1] < 0) {
            return 0;
        }

        $allAssignmentsIds = array();
        $allReviewsIds = array();
        $assignments = $this->getUnfinishedAssignmentsWONotif($testRunId);

        $reviewCtrl = new ReviewCtrl();
        $reviews = $reviewCtrl->getAssignments($testRunId);

        foreach ($assignments as $assignment) {
            if (!array_key_exists($assignment->username, $processed)) {
                $n = new RunNotification($assignment->username, $assignment->runTitle, $testRunId);
                $n->email = $assignment->getEmail();
                $processed[$n->username] = $n;
            }
            $n->addAssignment($assignment);
            array_push($allAssignmentsIds, $assignment->id);
        }
        foreach ($reviews as $review) {
            if (!array_key_exists($review->username, $processed)) {
                $n = new RunNotification($review->username, $review->runTitle, $testRunId);
                $n->email = $review->getEmail();
                $processed[$n->username] = $n;
            }
            $n->addReview($review);
            array_push($allReviewsIds, $review->id);
        }

        $notified = 0;
        $ctrl = new UserCtrl();
        foreach ($processed as $user) {
            if ($ctrl->getUser($user->username)->emailNotifications) {
                $body = ($this->emailProvider->useHTML) ? $user->getEmailBodyHTML($daysLeft, $deadline) : $user->getEmailBody($daysLeft, $deadline);
                if (!is_null($user->email) && strlen($user->email) > 0) {
                    $this->emailProvider->send($this->emailProvider->compose($body, $user->getEmailSubject($daysLeft), $user->email));
                } else {
                    $this->emailProvider->send($this->emailProvider->compose($body, $user->getEmailSubject($daysLeft), $user->username . "@" . DOMAIN));
                }
                $notified++;
            }
        }

        $this->markAssignmentsNotified($allAssignmentsIds);

        return $notified;
    }

    public function countNotifications($testRunId) {
        if (!$this->emailsEnabled) {
            return 0;
        }
        $processed = array();

        $meta = $this->getRunEndDateAndNotificationLimit($testRunId);
        $deadline = $meta[0];
        date_default_timezone_set('UTC');
        $now = time();
        $runEnd = strtotime($deadline);
        $daysLeft = round(($runEnd - $now ) / (60 * 60 * 24));

//        if (round(($runEnd - $now ) / (60 * 60)) > ($meta[1]*24) || $daysLeft < 0 || $meta[1] < 0) {
//            return 0;
//        }
        $testers = "";
        $assignments = $this->getUnfinishedAssignmentsWONotif($testRunId);
        foreach ($assignments as $assignment) {
            if (!array_key_exists($assignment->username, $processed)) {
                $n = new RunNotification($assignment->username, $assignment->runTitle, $testRunId);
                $processed[$n->username] = $n;
                $testers = $testers . ";" . $n->username;
            }
            $n->addAssignment($assignment);
        }

        $notified = 0;
        $ctrl = new UserCtrl();
        foreach ($processed as $user) {
            if ($ctrl->getUser($user->username)->emailNotifications) {
                $notified++;
            }
        }
        $e = array(0 => $notified, 1 => $testers);
        return $e;
    }

    /**
     * 
     * @param TestAssignment[] $testRunId
     */
    private function getUnfinishedAssignments($testRunId) {
        return $this->rnDao->getUnfihisedAssignments($testRunId);
    }

    /**
     * Returns only those assignments that are not finished and at the same time notification_sent is set to 0
     * @param TestAssignment[] $testRunId
     */
    private function getUnfinishedAssignmentsWONotif($testRunId) {
        return $this->rnDao->getUnfinishedAssignmentsWONotif($testRunId);
    }

    private function getRunEndDateAndNotificationLimit($testRunId) {
        return $this->rnDao->getRunEndDateAndNotificationLimit($testRunId);
    }

    /**
     * Marks given assignments as notified to prevent future notifications
     * @param int[] $allAssignmentsIds array of assignments ID to be marked as notified
     */
    private function markAssignmentsNotified($allAssignmentsIds) {
        if (count($allAssignmentsIds) > 0) {
            $sqlString = Util::arrayToSQLOR($allAssignmentsIds, "id");
            DB_DAO::executeQuery("UPDATE test_assignement SET notification_sent=1 WHERE " . $sqlString);
        }
    }

}
