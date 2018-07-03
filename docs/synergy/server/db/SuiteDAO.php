<?php
namespace Synergy\DB;

use PDO;
use Synergy\Controller\Mediator;
use Synergy\Misc\Util;
use Synergy\Model\Suite;
use Synergy\Model\TestCase;

/**
 * Description of SuiteDAO
 *
 * @author vriha
 */
class SuiteDAO {
    //put your code here

    /**
     * Deletes all suites for given specification
     * @param int $spec_id
     */
    public function deleteSuitesForSpecification($spec_id) {
        DB_DAO::connectDatabase();

        $handler2 = DB_DAO::getDB()->prepare("DELETE FROM suite WHERE specification_id=:id ");
        $handler2->bindParam(':id', $spec_id);

        if (!$handler2->execute()) {
            DB_DAO::throwDbError($handler2->errorInfo());
        }
    }

    public function deleteSuiteRefFromSpec($obsolete) {
        DB_DAO::connectDatabase();
        $handler3 = DB_DAO::getDB()->prepare("DELETE FROM suite_has_case WHERE " . $obsolete);

        if (!$handler3->execute()) {
            DB_DAO::throwDbError($handler3->errorInfo());
        }
    }

    public function getSuite($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT s.id, s.title, s.description, s.product,s.component, s.specification_id, s.order, sp.title as stitle, v.version FROM specification sp, suite s, version v WHERE s.id=:id AND s.specification_id=sp.id AND sp.is_active=1 AND v.id=sp.version_id");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }


        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $s = new Suite(intval($row['id']), $row['description'], $row['title'], $row['product'], $row['component'], $row['specification_id'], $row['order']);
            $s->version = $row['version'];
            $s->specificationTitle = $row['stitle'];
            return $s;
        }
        return null;
    }

    /**
     * Returns only basic information about cases (no steps or result)
     * @param type $id
     * @return array
     */
    public function getTestCases($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT c.id, c.duration, c.duration_count, c.title, c.order, GROUP_CONCAT(k.keyword SEPARATOR '|') as keywords FROM (`case` c, suite_has_case sc) LEFT JOIN (case_has_keyword ck, keyword k) ON (ck.case_id = c.id AND k.id=ck.keyword_id)  WHERE c.id=sc.case_id AND sc.suite_id=:id GROUP BY c.id ORDER BY c.order, c.id ASC");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $cases = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $duration = round(intval($row['duration']) / intval($row['duration_count']));
            $t = new TestCase($row['title'], $duration, $row['id'], $row['order']);
            $t->setKeywords($row['keywords'], '|');
            $t->suiteId = intval($id);
            array_push($cases, $t);
        }

        return $cases;
    }

    /**
     * Returns full test cases (including steps and result)
     * @param type $id
     * @return array
     */
    public function getTestCasesDetailed($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT c.id, c.duration, c.duration_count, c.title, c.steps, c.result, c.order, GROUP_CONCAT(k.keyword SEPARATOR '|') as keywords FROM (`case` c, suite_has_case sc) LEFT JOIN (case_has_keyword ck, keyword k) ON (ck.case_id = c.id AND k.id=ck.keyword_id)  WHERE c.id=sc.case_id AND sc.suite_id=:id GROUP BY c.id ORDER BY c.order, c.id ASC");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $cases = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $duration = round(intval($row['duration']) / intval($row['duration_count']));
            $t = new TestCase($row['title'], $duration, $row['id'], $row['order']);
            $t->setKeywords($row['keywords'], '|');
            $t->steps = $row['steps'];
            $t->result = $row['result'];
            array_push($cases, $t);
        }

        return $cases;
    }

       public function getTestCasesDetailedByLabel($id, $label) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT c.id, c.duration, c.duration_count, c.title, c.steps, c.result, c.order, GROUP_CONCAT(k.keyword SEPARATOR '|') as keywords FROM (`case` c, suite_has_case sc) JOIN (case_has_keyword ck, keyword k) ON (ck.case_id = c.id AND k.id=ck.keyword_id)  WHERE c.id=sc.case_id AND sc.suite_id=:id AND k.keyword=:l GROUP BY c.id ORDER BY c.order, c.id ASC");
        $handler->bindParam(':id', $id);
        $handler->bindParam(':l', $label);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $cases = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $duration = round(intval($row['duration']) / intval($row['duration_count']));
            $t = new TestCase($row['title'], $duration, $row['id'], $row['order']);
            $t->setKeywords($row['keywords'], '|');
            $t->steps = $row['steps'];
            $t->result = $row['result'];
            array_push($cases, $t);
        }

        return $cases;
    }
    
    public function getTestCasesIds($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT case_id FROM suite_has_case sc WHERE sc.suite_id=:id");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $cases = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($cases, intval($row['case_id']));
        }

        return $cases;
    }

    public function updateSuite($id, $title, $desc, $product, $component, $order) {
        DB_DAO::connectDatabase();
        if (is_null($order)) {
            $handler = DB_DAO::getDB()->prepare("UPDATE suite SET title=:title, description=:desc, product=:pid, component=:cid WHERE id=:id ");
        } else {
            $handler = DB_DAO::getDB()->prepare("UPDATE suite SET title=:title, description=:desc, product=:pid, component=:cid, `order`=:ord WHERE id=:id ");
            $handler->bindValue(':ord', $order);
        }

        $handler->bindParam(':id', $id);
        $handler->bindValue(':title', Util::purifyHTML($title));
        $handler->bindValue(':desc', Util::purifyHTML($desc));
        $handler->bindValue(':pid', Util::purifyHTML($product));
        $handler->bindValue(':cid', Util::purifyHTML($component));
        
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
            return false;
        }
        return true;
    }

    public function deleteSuite($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM suite WHERE id=:id ");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        Mediator::emit("suiteDeleted", $id);
        return true;
    }

    public function deleteReferencesToCases($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM suite_has_case WHERE suite_id=:id ");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function getSuiteTitle($suiteId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT title FROM suite WHERE id=:id");
        $handler->bindParam(':id', $suiteId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }


        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return $row['title'];
        }
        return "";
    }

    /**
     * Creates a new test suite for given specification
     * @param type $specificationId
     * @param type $title
     * @param type $description
     * @param type $product
     * @param type $component
     */
    public function createSuite($specificationId, $title, $description, $product, $component, $order) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO suite (title, description, product, component, specification_id, `order`) VALUES (:title, :desc, :pid, :cid, :specid, :or)");
        $handler->bindValue(':title', Util::purifyHTML($title));
        $handler->bindValue(':desc', Util::purifyHTML($description));
        $handler->bindValue(':pid', Util::purifyHTML($product));
        $handler->bindValue(':cid', Util::purifyHTML($component));
        $handler->bindValue(':specid', intval($specificationId));
        $handler->bindValue(':or', intval($order));

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        
        $newid = DB_DAO::getDB()->lastInsertId();
        Mediator::emit("suiteAddedToSpecification", array("specificationId" => $specificationId, "suiteId" => $newid));
        return $newid;
        ;
    }

    /**
     * Adds case to suite
     * @param type $caseId
     * @param type $suiteId
     * @return boolean true if successful
     */
    public function addCaseToSuite($caseId, $suiteId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO suite_has_case (suite_id, case_id) VALUES (:sid, :cid)");
        $handler->bindParam(':sid', $suiteId);
        $handler->bindParam(':cid', $caseId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        Mediator::emit("caseAddedToSuite", array("caseId" => $caseId, "suiteId" => $suiteId));

        return true;
    }

    /**
     * Returns true if given suite already has given case
     * @param type $caseId
     * @param type $suiteId
     * @return boolean
     */
    public function suiteAlreadyHasCase($caseId, $suiteId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT suite_id FROM suite_has_case WHERE suite_id=:sid AND case_id=:id");
        $handler->bindParam(':id', $caseId);
        $handler->bindParam(':sid', $suiteId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return true;
        }
        return false;
    }

    /**
     * Returns true if no other suite has this case
     * @param type $id
     * @param type $suiteId
     * @return boolean
     */
    public function onlySuiteHasCase($id, $suiteId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT count(suite_id) as suites FROM suite_has_case WHERE case_id=:cid AND suite_id!=:sid");
        $handler->bindParam(':cid', $id);
        $handler->bindParam(':sid', $suiteId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            if (intval($row['suites']) === 0) {
                return true;
            } else {
                return false;
            }
        }
        return false;
    }

    public function cloneSpecificationSuite($newSpecId, $suite) {
        if(is_null($suite->order) || !isset($suite->order)){
            $suite->order = 0;
        }
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO suite (title, description, product, component, specification_id, `order`) VALUES  (:t, :d, :p, :c, :s, :o)");
        $handler->bindParam(':c', $suite->component);
        $handler->bindParam(':p', $suite->product);
        $handler->bindValue(':t', $suite->title);
        $handler->bindValue(':d', $suite->desc);
        $handler->bindValue(':o', $suite->order);
        $handler->bindParam(':s', $newSpecId);
        if (!$handler->execute()) {

            DB_DAO::throwDbError($handler->errorInfo());
            return -1;
        }

        return intval(DB_DAO::getDB()->lastInsertId());
    }

    /**
     * Returns total number of suites
     * @return int
     */
    public function getSuitesCount() {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT count(id) as id FROM suite");

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return intval($row['id']);
        }
        return 0;
    }

    public function findMatchingSuites($title, $limit = 15) {
        DB_DAO::connectDatabase();
        $title = strtolower($title);
        $handler = DB_DAO::getDB()->prepare("SELECT id,title,specification_id FROM suite WHERE LOWER(title) LIKE :t ORDER BY title ASC LIMIT 0,".$limit);
        $title = "%" . $title . "%";
        $handler->bindParam(':t', $title, PDO::PARAM_STR);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $results = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($results, new Suite($row['id'], '', $row['title'], '', '', $row['specification_id']));
        }
        return $results;
    }

    public function getSpeficiationId($suiteId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT specification_id FROM suite WHERE id=:id");
        $handler->bindParam(':id', $suiteId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return intval($row['specification_id']);
        }
        return -1;
    }

}

?>
