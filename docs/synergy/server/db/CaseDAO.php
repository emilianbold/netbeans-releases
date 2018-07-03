<?php

namespace Synergy\DB;

use PDO;
use Synergy\Controller\Mediator;
use Synergy\Misc\Util;
use Synergy\Model\Bug;
use Synergy\Model\Label;
use Synergy\Model\LabelResult;
use Synergy\Model\TestCase;
use Synergy\Model\TestCaseImage;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of CaseDAO
 *
 * @author lada
 */
class CaseDAO {

    public function getCaseForSuite($id, $suiteId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT c.id, c.order, c.duration_count, c.duration, c.title,c.steps, c.result, GROUP_CONCAT(k.keyword SEPARATOR '|') as keywords FROM (`case` c, suite_has_case sc) LEFT JOIN (case_has_keyword ck, keyword k) ON (ck.case_id = c.id AND k.id=ck.keyword_id)  WHERE c.id=:id AND c.id=sc.case_id AND sc.suite_id=:sid GROUP BY c.id");
        $handler->bindParam(':id', $id);
        $handler->bindParam(':sid', $suiteId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $duration = round(intval($row['duration']) / intval($row['duration_count']));
            $t = new TestCase($row['title'], $duration, $row['id'], $row['order']);
            $t->setKeywords($row['keywords'], '|');
            $t->steps = $row['steps'];
            $t->result = $row['result'];

            $t->url = BASER_URL . "case.php?id=" . $id . "&suite=" . $suiteId;

            return $t;
        }
        return null;
    }

    public function getCase($id) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT c.id, c.duration_count, c.duration,c.order, c.title,c.steps, c.result, GROUP_CONCAT(k.keyword SEPARATOR '|') as keywords FROM (`case` c) LEFT JOIN (case_has_keyword ck, keyword k) ON (ck.case_id = c.id AND k.id=ck.keyword_id)  WHERE c.id=:id GROUP BY c.id");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $duration = round(intval($row['duration']) / intval($row['duration_count']));
            $t = new TestCase($row['title'], $duration, $row['id'], $row['order']);
            $t->setKeywords($row['keywords'], '|');
            $t->steps = $row['steps'];
            $t->result = $row['result'];
            $t->url = BASER_URL . "case.php?id=" . $id . "&suite=-1";

            return $t;
        }
        return null;
    }

    public function getIssues($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT id, bug_id FROM bug WHERE case_id=:id");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $data = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($data, new Bug($row['id'], $row['bug_id']));
        }
        return $data;
    }

    public function getCasesByFilter($label, $page) {
        $start = intval((($page - 1) * LABEL_PAGE));
        $stop = intval((LABEL_PAGE));

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT c.title, c.id, k.keyword, s.title as stitle, s.id as sid, v.id as vid, v.version FROM (`case` c, case_has_keyword ck, keyword k) JOIN (suite s, suite_has_case sc, specification sp, version v) ON (sc.suite_id=s.id AND sc.case_id=c.id AND sp.id=s.specification_id AND v.id=sp.version_id) WHERE ck.case_id=c.id AND ck.keyword_id=k.id AND k.keyword=:label ORDER BY c.title ASC LIMIT " . $start . "," . $stop);
        $handler->bindParam(':label', $label);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $data = new LabelResult($page, $label);
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $t = new TestCase($row['title'], 0, $row['id'], $row['vid']);
            $t->version = $row['version'];
            $t->suiteTitle = $row['stitle'];
            $t->suiteId = intval($row['sid']);
            array_push($data->cases, $t);
        }
        if (count($data->cases) < LABEL_PAGE) {
            $data->nextUrl = "";
        }
        return $data;
    }

    /**
     * Add issues to case - if issue is already recorded, nothing happens
     * @param int $bugId
     * @param int $caseId
     * @return boolean true if success
     */
    public function addIssue($bugId, $caseId) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT bug_id FROM bug WHERE bug_id=:bid AND case_id=:cid");
        $handler->bindValue(':bid', Util::purifyHTML($bugId));
        $handler->bindParam(':cid', $caseId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return false;
        }

        DB_DAO::connectDatabase();
        $handler2 = DB_DAO::getDB()->prepare("INSERT INTO bug (bug_id, case_id) VALUES (:bid, :cid)");
        $handler2->bindParam(':bid', Util::purifyHTML($bugId));
        $handler2->bindParam(':cid', $caseId);

        if (!$handler2->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        Mediator::emit("issueAddedToCase", array("caseId" => $caseId, "bugId" => $bugId));
        return true;
    }

    /**
     * Removes issue from case
     * @param int $bugId
     * @param int $caseId
     * @return boolean true if success
     */
    public function removeIssue($bugId, $caseId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM bug WHERE (bug_id=:bid OR bug_id='no-number') AND case_id=:cid");
        $handler->bindParam(':bid', Util::purifyHTML($bugId));
        $handler->bindParam(':cid', $caseId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        Mediator::emit("issueRemovedFromCase", array("caseId" => $caseId, "bugId" => $bugId));
        return true;
    }

    public function addLabel($label, $caseId, $kid) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO case_has_keyword (keyword_id, case_id) VALUES (:kid, :cid)");
        $handler->bindParam(':kid', $kid);
        $handler->bindParam(':cid', $caseId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        Mediator::emit("labelAddedToCase", array("caseId" => $caseId, "label" => $label));
        return true;
    }

    public function getKeywordId($label) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT id  FROM keyword WHERE keyword=:k");
        $handler->bindParam(':k', $label);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return intval($row['id']);
        }

        return -1;
    }

    public function createKeyword($label) {
        DB_DAO::connectDatabase();
        $handler2 = DB_DAO::getDB()->prepare("INSERT INTO keyword (keyword) VALUES (:k)");
        $handler2->bindValue(':k', Util::purifyHTML($label));

        if (!$handler2->execute()) {
            DB_DAO::throwDbError($handler2->errorInfo());
        }
        return intval(DB_DAO::getDB()->lastInsertId());
    }

    /**
     * Removes keyword from case
     * @param type $label
     * @param type $caseId
     * @return boolean true if successful
     */
    public function removelabel($label, $caseId, $kid) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM case_has_keyword WHERE case_id=:cid AND keyword_id=:kid");
        $handler->bindParam(':kid', $kid);
        $handler->bindParam(':cid', $caseId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        Mediator::emit("labelRemovedFromCase", array("caseId" => $caseId, "label" => $label));
        return true;
    }

    /**
     * Creates new case and add it to given suite
     * @param type $parentSuiteId
     * @param type $title
     * @param type $steps
     * @param type $result
     * @param type $duration
     * @return boolean
     */
    public function createCase($title, $steps, $result, $duration, $order) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO `case` (duration, title, steps, result,`order`) VALUES (:d, :t, :s, :r,:o)");
        $handler->bindParam(':d', $duration);
        $handler->bindValue(':t', Util::purifyHTML($title));
        $handler->bindValue(':s', Util::purifyHTML($steps));
        $handler->bindValue(':r', Util::purifyHTML($result));
        $handler->bindValue(':o', $order);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $caseId = DB_DAO::getDB()->lastInsertId();
        return intval($caseId);
    }

    public function createDuplicitCase($title, $steps, $result, $duration, $order) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO `case` (duration, title, steps, result,`order`) VALUES (:d, :t, :s, :r,:o)");
        $handler->bindParam(':d', $duration);
        $handler->bindValue(':t', Util::purifyHTML($title));
        $handler->bindValue(':s', Util::purifyHTML($steps));
        $handler->bindValue(':r', Util::purifyHTML($result));
        $handler->bindValue(':o', $order);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $caseId = DB_DAO::getDB()->lastInsertId();
        return intval($caseId);
    }

    /**
     * Removes case from suite
     * @param int $suiteId
     * @param int $caseId
     * @return boolean true if success
     */
    public function removeCaseFromSuite($suiteId, $caseId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM suite_has_case WHERE case_id=:cid AND suite_id=:sid");
        $handler->bindParam(':cid', $caseId);
        $handler->bindParam(':sid', $suiteId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        Mediator::emit("caseRemovedFromSuite", array("caseId" => $caseId, "suiteId" => $suiteId));
        return true;
    }

    /**
     * Returns cases (up to 25) that match given criteria
     * @param type $_REQUEST
     * @return Case[] array of cases, each instance has defined only ID and title
     */
    public function findMatchingCases($title) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT title, id FROM `case` WHERE title LIKE :title ORDER BY title ASC LIMIT 0,25");
        $title = "%" . $title . "%";
        $handler->bindParam(':title', $title, PDO::PARAM_STR);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $results = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($results, new TestCase($row['title'], -1, $row['id'], -1));
        }
        return $results;
    }

    /**
     * Edit case but does not change duration
     * @return boolean
     */
    public function edit($id, $title, $steps, $result, $order) {
        DB_DAO::connectDatabase();
        if (is_null($order)) {
            $handler = DB_DAO::getDB()->prepare("UPDATE `case` SET title=:title, steps=:steps, result=:res WHERE id=:id ");
        } else {
            $handler = DB_DAO::getDB()->prepare("UPDATE `case` SET title=:title, steps=:steps, result=:res,`order`=:or WHERE id=:id ");
            $handler->bindValue(':or', $order);
        }

        $handler->bindParam(':id', $id);
        $handler->bindValue(':title', Util::purifyHTML($title));
        $handler->bindValue(':steps', Util::purifyHTML($steps));
        $handler->bindValue(':res', Util::purifyHTML($result));

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
            return false;
        }

        Mediator::emit("caseUpdated", $id);
        return true;
    }

    /**
     * Edits case with duration and sets duration count to 1
     * @return boolean
     */
    public function editWithDuration($id, $title, $steps, $result, $duration, $order) {
        DB_DAO::connectDatabase();
        if(is_null($order)){
            $handler = DB_DAO::getDB()->prepare("UPDATE `case` SET title=:title, steps=:steps, result=:res, duration=:dur, duration_count=1 WHERE id=:id ");    
        }else{
            $handler = DB_DAO::getDB()->prepare("UPDATE `case` SET title=:title, steps=:steps, result=:res, duration=:dur, `order`=:or, duration_count=1 WHERE id=:id ");    
            $handler->bindValue(':or', $order);
        }
        
        $handler->bindParam(':id', $id);
        $handler->bindValue(':title', Util::purifyHTML($title));
        $handler->bindValue(':steps', Util::purifyHTML($steps));
        $handler->bindValue(':res', Util::purifyHTML($result));
        $handler->bindValue(':dur', intval($duration));

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
            return false;
        }

        Mediator::emit("caseUpdated", $id);
        return true;
    }

    public function cloneLabels($id, $newId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT keyword_id FROM case_has_keyword WHERE case_id=:id");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $insertQuery = "";
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $insertQuery = $insertQuery . "INSERT INTO case_has_keyword (case_id, keyword_id) VALUES (" . intval($newId) . ", " . $row['keyword_id'] . ");";
        }

        return $insertQuery;
    }

    public function cloneIssues($id, $newId) {
        DB_DAO::connectDatabase();
        
        $handler = DB_DAO::getDB()->prepare("SELECT bug_id FROM bug WHERE case_id=:id");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        
        $insertQuery = "";
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $insertQuery = $insertQuery . "INSERT INTO bug (case_id, bug_id) VALUES (" . intval($newId) . ", '" . $row['bug_id'] . "');";
        }
        return $insertQuery;
    }

    /**
     * Returns cases ID and duration where $c_id is SQL "OR" statement for id
     * @param type $c_id
     * @return TestCase[] 
     */
    public function getCasesDuration($c_id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT id, duration, duration_count FROM `case` WHERE " . $c_id);


        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $cases = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $d = round(intval($row['duration']) / intval($row['duration_count']));
            array_push($cases, new TestCase('', $d, $row['id']));
        }

        return $cases;
    }

    /**
     * Updates duration of given test case
     * @param type $caseId
     * @param int $duration Duration in minutes
     * @return boolean
     */
    public function updateDuration($id, $duration) {
        DB_DAO::connectDatabase();
        if ($duration < 1) { // do not allow less then 1 minute
            $duration = 1;
        }
        $handler = DB_DAO::getDB()->prepare("UPDATE `case` SET duration=duration+" . $duration . ", duration_count=duration_count+1 WHERE id=:id ");
        $handler->bindParam(':id', $id);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
            return false;
        }
        return true;
    }

    /**
     * Saves record about case's image to db
     * @param type $id case id
     * @param type $name image name
     * @param type $title image title
     * @return int new image ID
     */
    public function saveImage($id, $name, $title) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO case_image (case_id, path, title) VALUES (:id, :name, :title)");
        $handler->bindParam(':id', $id);
        $path = IMAGE_PATH . $name;
        $handler->bindParam(':name', $path);
        $handler->bindValue(':title', Util::purifyHTML($title));
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $newId = DB_DAO::getDB()->lastInsertId();
        Mediator::emit("imageCreated", $newId);
        return intval($newId);
    }

    public function deleteImage($id) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM case_image WHERE id=:id ");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        Mediator::emit("imageDeleted", $id);
    }

    public function getImageAttachment($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT path FROM case_image WHERE id=:id");
        $handler->bindParam(':id', $id);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return $row['path'];
        }
        return "";
    }

    /**
     * Returns all images for given test case
     * @param type $id
     */
    public function getImages($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT path, title, id FROM case_image WHERE case_id=:id");
        $handler->bindParam(':id', $id);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $data = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($data, new TestCaseImage($row['path'], $row['id'], $id, $row['title']));
        }
        return $data;
    }

    /**
     * Returns total number of cases
     * @return int
     */
    public function getCasesCount() {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT count(id) as id FROM `case`");

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return intval($row['id']);
        }
        return 0;
    }

    public function getUnusedCases() {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT c.id FROM  `case` c LEFT JOIN  `suite_has_case` s ON s.case_id = c.id WHERE s.case_id IS NULL");

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $results = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($results, intval($row['id']));
        }
        return $results;
    }

    public function removeIssuesForCase($caseId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM bug WHERE case_id=:id ");
        $handler->bindParam(':id', $caseId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function removeLabelsForCase($caseId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM case_has_keyword WHERE case_id=:id ");
        $handler->bindParam(':id', $caseId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function removeCase($caseId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM `case` WHERE id=:id ");
        $handler->bindParam(':id', $caseId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function cloneImages($id, $newId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT path, title FROM case_image WHERE case_id=:id");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            DB_DAO::executeQuery("INSERT INTO case_image (case_id, title, path) VALUES (" . intval($newId) . ", '" . $row['title'] . "', '" . $row['path'] . "');");
        }        
    }

    public function getSpecificationsIdForCase($caseId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT DISTINCT s.specification_id as spec FROM  `suite_has_case` c, suite s WHERE c.case_id=:id AND c.suite_id=s.id");
        $handler->bindParam(':id', $caseId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $results = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($results, intval($row['spec']));
        }
        return $results;
    }

    public function getLabels($caseId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT keyword_id FROM case_has_keyword WHERE case_id=:id");
        $handler->bindParam(':id', $caseId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $data = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($data, new Label('', $row['keyword_id']));
        }
        return $data;
    }

    public function isCaseInUsedSpecification($caseId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT u.specification_id FROM assignment_progress a , suite u, suite_has_case h, test_assignement e WHERE  h.suite_id=u.id AND h.case_id=:id AND a.test_assignement_id=e.id AND u.specification_id=e.specification_id LIMIT 0,1");
        $handler->bindParam(':id', $caseId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return true;
        }
        return false;
    }

    public function caseHasLabel($labelId, $caseId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT c.case_id FROM `case_has_keyword` c WHERE c.case_id=:id AND c.keyword_id=:ld LIMIT 0,1");
        $handler->bindParam(':id', $caseId);
        $handler->bindParam(':ld', $labelId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return true;
        }
        return false;
    }

    public function usedInCasesCount($path) {
           DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT COUNT(id) as totalUsed FROM `case_image` WHERE path=:p");
        $handler->bindParam(':p', $path);
        
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return intval($row["totalUsed"]);
        }
        return 0;
    }

}

?>