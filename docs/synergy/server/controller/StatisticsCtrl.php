<?php

namespace Synergy\Controller;

use Synergy\DB\CaseDAO;
use Synergy\DB\ReviewDAO;
use Synergy\DB\SpecificationDAO;
use Synergy\DB\SuiteDAO;
use Synergy\DB\TribeDAO;
use Synergy\DB\UserDAO;
use Synergy\Misc\Util;
use Synergy\Model\StatRecord;
use Synergy\Model\UserStatistics;

/**
 * Description of StatisticsCtrl
 *
 * @author vriha
 */
class StatisticsCtrl {

    private $specDao;
    private $caseDao;
    private $suiteDao;
    private $tribeDao;
    private $userDao;
    private $runCtrl;
    private $tribeCtrl;

    function __construct() {
        $this->caseDao = new CaseDAO();
        $this->suiteDao = new SuiteDAO();
        $this->specDao = new SpecificationDAO();
        $this->tribeDao = new TribeDAO();
        $this->userDao = new UserDAO();
    }

    /**
     * 
     * @return RunCtrl
     */
    public function getRunCtrl() {
        if (is_null($this->runCtrl)) {
            $this->runCtrl = new RunCtrl();
        }
        return $this->runCtrl;
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
     * Returns array of statistic data
     * @return StatRecord[] Description
     */
    public function getStatistics() {
        $stats = array();
        array_push($stats, new StatRecord("Specifications", $this->specDao->getSpecificationsCount()));
        array_push($stats, new StatRecord("Suites", $this->suiteDao->getSuitesCount()));
        array_push($stats, new StatRecord("Cases", $this->caseDao->getCasesCount()));
        array_push($stats, new StatRecord("Tribes", $this->tribeDao->getTribesCount()));
        array_push($stats, new StatRecord("Users", $this->userDao->getUsersCount()));
        return $stats;
    }

    /**
     * Writes statistics to /data directory in form of JSON dump so data for test run are preserved and
     * not dependent on records in DB
     * @param type $testRunId
     */
    public function cacheStatistics($testRunId) {
        $tr = $this->getRunCtrl()->getRunWithIssues(intval($testRunId), false);
        $tr->assigneesOverview = $this->getRunCtrl()->getUserCentricData($tr);
        $tr->reviews = $this->getReviewsStats($testRunId);
        $tribesId = UserStatistics::getDistinctTribeIds($tr->assigneesOverview);
        $tr->tribes = $this->getTribeCtrl()->getTribesSpecificationsForTribes($tribesId);
        $tr->testRun->assignments = array();
        Util::writeTxtFile("statistics.json", ".." . DIRECTORY_SEPARATOR . "data" . DIRECTORY_SEPARATOR . "test_runs" . DIRECTORY_SEPARATOR . "run" . $testRunId . DIRECTORY_SEPARATOR, json_encode($tr));
    }

    private function getReviewsStats($testRunId) {
        $reviewCtrl = new ReviewCtrl();
        $reviews = $reviewCtrl->getAssignments($testRunId);
        $result = array();
        if (count($reviews) < 1) {
            return $result;
        }

        $ids = array();
        foreach ($reviews as $b) {
            $ids[] = $b->id;
        }
        $cond = Util::arrayToSQLOR($ids, "ra.id");
        $mapping = ReviewDAO::getCommentCounts($cond);
        foreach ($reviews as $b) {
            if (!is_null($mapping)) {
                $b->numberOfComments = $mapping["id" . $b->id];
            } else {
                $b->numberOfComments = 0;
            }
        }
        return $reviews;
    }

}

?>
