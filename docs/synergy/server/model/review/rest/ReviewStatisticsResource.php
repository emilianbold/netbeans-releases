<?php

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

namespace Synergy\Model\Review\Rest;

use Exception;
use Synergy\DB\ReviewDAO;
use Synergy\Misc\Util;
use Synergy\Model\Review\ReviewAssignment;

/**
 * Description of ReviewStatisticsResource
 *
 * @author vriha
 */
class ReviewStatisticsResource {

    public $username;
    public $userDisplayName;
    public $weight;
    public $timeTaken;
    public $numberOfComments;
    public $isFinished;
    public $id;
    public $lastUpdated;

    /**
     * 
     * @param ReviewAssignment $assignment
     */
    public static function createAssignment($assignment, $mapping = null) {
        $i = new ReviewStatisticsResource();
        $i->userDisplayName = $assignment->userDisplayName;
        $i->username = $assignment->username;
        $i->weight = $assignment->weight;
        $i->timeTaken = $assignment->timeTaken;
        $i->isFinished = $assignment->isFinished;
        $i->id = $assignment->id;
        $i->lastUpdated = $assignment->lastUpdated;
        if (!is_null($mapping)) {
            $i->numberOfComments = $mapping["id" . $assignment->id];
        } else {
            throw new Exception("Not enough data to get number of comments", "", "");
        }
        return $i;
    }

    /**
     * 
     * @param ReviewAssignment[] $assignments
     */
    public static function createAssignments($assignments) {
        $result = array();
        if (count($assignments) < 1) {
            return $result;
        }

        $ids = array();
        foreach ($assignments as $b) {
            $ids[] = $b->id;
        }
        $cond = Util::arrayToSQLOR($ids, "ra.id");
        $mapping = ReviewDAO::getCommentCounts($cond);

        foreach ($assignments as $a) {
            $result[] = ReviewStatisticsResource::createAssignment($a, $mapping);
        }
        return $result;
    }

    //put your code here
}
