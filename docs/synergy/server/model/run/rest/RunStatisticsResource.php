<?php

namespace Synergy\Model\Run\Rest;

use Synergy\Model\Bug\Rest\BugResource;
use Synergy\Model\Statistics\Rest\StatisticsLineResource;
use Synergy\Model\TestRunStatistics;
use Synergy\Model\Review\Rest\ReviewStatisticsResource;

/**
 * Description of RunStatistics
 *
 * @author vriha
 */
class RunStatisticsResource {

    public $issues;
    public $testRun;
    public $assigneesOverview;
    public $reviews;
    public $tribes;

    /**
     * 
     * @param TestRunStatistics $testRunStatistics
     */
    public static function create($testRunStatistics) {
        $i = new RunStatisticsResource();
        $i->testRun = RunListItemResource::createFromTestRun($testRunStatistics->testRun);
        $i->tribes = $testRunStatistics->tribes;
        $i->issues = BugResource::createFromBugs($testRunStatistics->issues);
        $i->assigneesOverview = StatisticsLineResource::createFromUsers($testRunStatistics->assigneesOverview);
        $i->reviews = ReviewStatisticsResource::createAssignments($testRunStatistics->reviews);
        return $i;
    }

}
