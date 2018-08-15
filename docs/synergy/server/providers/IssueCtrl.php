<?php

namespace Synergy\Providers;

use Synergy\DB\IssueDAO;
use Synergy\Interfaces\IssueProvider;
use Synergy\Model\Bug;

/**
 * This provider resolves issues, generally it has 3 public methods. If you don't want to 
 * use this provider, you must modify the methods below to return same data type, even if it should
 * be "empty" objects.
 *
 */
class IssueCtrl implements IssueProvider {

    private static $issueDao;
    private static $displayStatusFilters = array(0 => "closed", 1 => "verified");
    private static $displayResolutionFilters = array(0 => "resolved fixed", 1 => "resolved duplicate", 2 => "resolved invalid");

    const BUG_TRACKING_SYSTEM_NAME = "bugzilla";

    /**
     * Returns information (id, title, resolution) about given issue
     * @param int $id issue ID
     * @return Bug
     */
    public static function getIssue($id, $filtered = false) {
        $issue = IssueCtrl::getIssueDao()->getIssue($id);
        if ($filtered && ((in_array(strtolower($issue->status), IssueCtrl::$displayStatusFilters)) || (in_array(strtolower($issue->resolution), IssueCtrl::$displayResolutionFilters)))) {
            $issue->isStillValid = false;
        }
        return $issue;
    }

    /**
     * 
     * @return IssueDAO
     */
    private static function getIssueDao() {
        if (is_null(IssueCtrl::$issueDao))
            IssueCtrl::$issueDao = new IssueDAO();
        return IssueCtrl::$issueDao;
    }

    /**
     * Returns indexed based array of Bug instances matching bug ids given by $issues param. If you don't want to
     * use this method, you must modify it to return again Bug[], even if each instance of Bug[] should be 
     * "empty" object with only bug ID
     * @param int[] $issues
     * @return Bug[] indexed based array of Bug instances matching bug ids given by $issues param
     */
    public static function validateIssuesWithRun($issues) {
        return IssueCtrl::getIssueDao()->validateIssues($issues);
    }

    /**
     * Returns associative array of Bug instances matching bug ids given by $issues param, key is string "id".bugId.
     * If you don't want to use this method, you must modify it to return again Bug[], even if each instance of Bug[] should be 
     * "empty" object with only bug ID
     * @param int[] $issues
     * @return Bug[] associative array of Bug instances matching bug ids given by $issues param
     */
    public static function validateIssuesWithRunAssoc($issues) {
        return IssueCtrl::getIssueDao()->validateIssuesAssoc($issues);
    }

}

?>