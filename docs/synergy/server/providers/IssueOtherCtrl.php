<?php

namespace Synergy\Providers;

use Synergy\Interfaces\IssueProvider;
use Synergy\Model\Bug;

/**
 * Description of IssueOtherCtrl
 *
 */
class IssueOtherCtrl implements IssueProvider {

    const BUG_TRACKING_SYSTEM_NAME = "other";

    /**
     * Returns information (id, title, resolution) about given issue
     * @param int $id issue ID
     * @return Bug
     */
    public static function getIssue($id, $filtered = false) {
        return new Bug($id, $id);
    }

    /**
     * Returns indexed based array of Bug instances matching bug ids given by $issues param. If you don't want to
     * use this method, you must modify it to return again Bug[], even if each instance of Bug[] should be 
     * "empty" object with only bug ID
     * @param int[] $issues
     * @return Bug[] indexed based array of Bug instances matching bug ids given by $issues param
     */
    public static function validateIssuesWithRun($issues) {
        $bugs = array();
        foreach ($issues as $i) {
            $b = new Bug($i, $i);
            $b->status = "unknown";
            $b->priority = "unknown";
            $bugs[] = $b;
        }
        return $bugs;
    }

    /**
     * Returns associative array of Bug instances matching bug ids given by $issues param, key is string "id".bugId.
     * If you don't want to use this method, you must modify it to return again Bug[], even if each instance of Bug[] should be 
     * "empty" object with only bug ID
     * @param int[] $issues
     * @return Bug[] associative array of Bug instances matching bug ids given by $issues param
     */
    public static function validateIssuesWithRunAssoc($issues) {
        $bugs = array();
        foreach ($issues as $i) {
            $b = new Bug($i, $i);
            $b->status = "unknown";
            $b->priority = "unknown";
            $bugs["id".$i] = $b;
        }
        return $bugs;
    }

}
