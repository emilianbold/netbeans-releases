<?php

namespace Synergy\DB;

use PDO;
use Synergy\App\Synergy;
use Synergy\Controller\Mediator;
use Synergy\Misc\Util;
use Synergy\Model\Specification;
use Synergy\Model\SpecificationAttachment;
use Synergy\Model\SpecificationsSimpleNameList;
use Synergy\Model\Suite;
use Synergy\Model\Project\Project;
use \Synergy\Controller\ProjectCtrl;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of SpecificationDAO
 *
 * @author vriha
 */
class SpecificationDAO {

    /**
     * Returns all specifications for version given by ID
     * @param int $versionID version ID
     * @return array
     */
    public function getSpecifications($versionID, $userId = -1) {
        DB_DAO::connectDatabase();
        if ($userId < 0) {
            $handler = DB_DAO::getDB()->prepare("SELECT specification.id, title, description, author_id, owner_id,GROUP_CONCAT( p.id SEPARATOR ';') AS pids, GROUP_CONCAT( p.name SEPARATOR ';') AS pnames FROM specification LEFT JOIN specification_has_project shp ON shp.specification_id = specification.id LEFT JOIN project p ON shp.project_id = p.id WHERE version_id=:version AND is_active=1 GROUP BY specification.id ORDER BY title ASC ");
        } else {
            $handler = DB_DAO::getDB()->prepare("SELECT s.id, title, description, author_id, owner_id, f.user_id, GROUP_CONCAT( p.id SEPARATOR  ';' ) AS pids, GROUP_CONCAT( p.name SEPARATOR  ';' ) AS pnames FROM specification s LEFT JOIN user_has_favorite f ON ( f.specification_id = s.id AND f.user_id =:uid ) LEFT JOIN specification_has_project shp ON shp.specification_id = s.id LEFT JOIN project p ON shp.project_id = p.id WHERE version_id =:version AND is_active =1 GROUP BY s.id ORDER BY title ASC");
        }
        $handler->bindParam(':version', $versionID);
        if ($userId > 0)
            $handler->bindParam(':uid', $userId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $data = array();

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $sp = new Specification(intval($row['id']), $row['description'], $row['title'], $versionID, $row['author_id'], $row['owner_id']);

            if ($userId > -1 && strval($row['user_id']) === strval($userId)) {
                $sp->isFavorite = 1;
            }

            if (strlen($row["pids"]) > 0) {
                $sp->ext["projects"] = array();
                $pids = explode(";", $row["pids"]);
                $pnames = explode(";", $row["pnames"]);
                foreach ($pids as $index => $value) {
                    $sp->ext["projects"][] = new Project($value, $pnames[$index]);
                }
            }

            array_push($data, $sp);
        }
        return $data;
    }

    public function getAllSpecifications($userId = -1) {
        DB_DAO::connectDatabase();
        if ($userId < 0) {
            $handler = DB_DAO::getDB()->prepare("SELECT specification.id, title, description, author_id, owner_id, simpleName, version_id, v.version FROM specification, version v WHERE v.id=specification.version_id AND v.isObsolete=0 AND specification.is_active=1 ORDER BY title ASC");
        } else {
            $handler = DB_DAO::getDB()->prepare("SELECT s.id, title, description, author_id, owner_id,simpleName, f.user_id, version_id, v.version FROM specification s JOIN version v ON s.version_id=v.id LEFT JOIN user_has_favorite f ON (f.specification_id=s.id AND f.user_id=:uid) WHERE v.isObsolete=0 AND s.is_active=1 ORDER BY title ASC");
            $handler->bindParam(':uid', $userId);
        }

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $data = array();

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $sp = new Specification(intval($row['id']), $row['description'], $row['title'], $row['version_id'], $row['author_id'], $row['owner_id']);
            $sp->version = $row['version'];
            $sp->simpleName = $row['simpleName'];
            if ($userId > -1 && strval($row['user_id']) === strval($userId))
                $sp->isFavorite = 1;
            array_push($data, $sp);
        }
        return $data;
    }

    /**
     * Returns favorite specification of given user
     * @param String $username
     * @return Specification[]
     */
    public function getFavoriteSpecifications($username) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT s.id, s.title, s.description, s.author_id,v.version, s.version_id, s.owner_id FROM specification s, user_has_favorite f, user u, version v WHERE f.user_id=u.id AND u.username=:username AND f.specification_id=s.id AND s.is_active=1 AND s.version_id=v.id ORDER BY s.title ASC");
        $handler->bindParam(':username', $username);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $data = array();

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $s = new Specification(intval($row['id']), $row['description'], $row['title'], $row['version_id'], $row['author_id'], $row['owner_id']);
            $s->version = $row['version'];
            $s->isFavorite = 1;
            array_push($data, $s);
        }
        return $data;
    }

    /**
     * Returns complete specification with test suites and attachments
     * @param type $id
     * @param String $label not required, if given, only cases with this label will be counted
     * @return Specification|null
     */
    public function getSpecification($id, $label = '', $userId) {// TODO owner
        DB_DAO::connectDatabase();
//        $handler = DB_DAO::getDB()->prepare("SELECT s.id, s.last_updated,s.simpleName, s.title, s.description, s.author_id,s.version_id, v.version, u.username, u.first_name, u.last_name, s.owner_id FROM specification s, version v, user u WHERE s.id=:id AND s.version_id=v.id AND u.id=s.author_id");
        $handler = DB_DAO::getDB()->prepare("SELECT s.id, s.last_updated,s.simpleName, s.title, s.description, s.author_id,s.version_id, v.version, u.username, u.first_name, u.last_name, s.owner_id, f.user_id FROM specification s, version v, user u LEFT JOIN user_has_favorite f ON (f.specification_id=:id AND f.user_id=:ud) WHERE s.id=:sd AND s.is_active=1 AND s.version_id=v.id AND u.id=s.author_id");
        $handler->bindParam(':id', $id);
        $handler->bindParam(':sd', $id);
        $handler->bindParam(':ud', $userId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }


        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $s = new Specification(intval($row['id']), $row['description'], $row['title'], $row['version_id'], $row['author_id'], $row['owner_id']);
            $s->author = $row['username'];
            if (isset($row['user_id']) && !is_null($row['user_id'])) {
                $s->isFavorite = 1;
            }
            $s->version = $row['version'];
            $s->simpleName = $row['simpleName'];
            $s->authorName = $row['first_name'] . " " . $row['last_name'];
            $s->setLastUpdated($row['last_updated']);
            return $s;
        }
        return null;
    }

    /**
     * Returns all attachments for given specification
     * @param type $id
     * @return SpecificationAttachment[]
     */
    public function getAttachments($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT s.id, s.path FROM specification_attachement s WHERE s.specification_id=:id");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $data = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($data, new SpecificationAttachment($row['path'], $row['id'], $id));
        }
        return $data;
    }

    /**
     * Returns all test suites for given specification
     * @param type $id
     * @return Suite[]
     */
    public function getTestSuites($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT s.id, s.title, s.description, s.product, s.component, s.order FROM suite s WHERE s.specification_id=:id ORDER BY s.order, s.id ASC");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $data = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($data, new Suite($row['id'], $row['description'], $row['title'], $row['product'], $row['component'], $id, $row['order']));
        }
        return $data;
    }

    /**
     * Returns sum of duration of all cases for given specification
     * @param type $id
     * @param String $label not required, if given, only cases with this label will be counted
     * @return int
     */
    public function getEstimatedTime($id, $label = '') {
        DB_DAO::connectDatabase();
        if (strlen($label) > 0) {
            $handler = DB_DAO::getDB()->prepare("SELECT cs.duration, cs.duration_count FROM suite_has_case sc, suite s, `case` cs, case_has_keyword ck, keyword kw WHERE s.specification_id =:id AND sc.suite_id=s.id AND cs.id=sc.case_id AND kw.keyword=:label AND kw.id=ck.keyword_id AND ck.case_id=cs.id");
            $handler->bindParam(':id', $id);
            $handler->bindParam(':label', $label);

            if (!$handler->execute()) {
                DB_DAO::throwDbError($handler->errorInfo());
            }

            $estimatedTime = 0;
            while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
                $estimatedTime += round(intval($row['duration']) / intval($row['duration_count']));
            }
            return $estimatedTime;
        } else {
            $handler = DB_DAO::getDB()->prepare("SELECT cs.duration, cs.duration_count FROM suite_has_case sc, suite s, `case` cs WHERE s.specification_id =:id AND sc.suite_id=s.id AND cs.id=sc.case_id");
            $handler->bindParam(':id', $id);

            if (!$handler->execute()) {
                DB_DAO::throwDbError($handler->errorInfo());
            }
            $estimatedTime = 0;
            while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
                $estimatedTime += round(intval($row['duration']) / intval($row['duration_count']));
            }
            return $estimatedTime;
        }
    }

    /**
     * Updates specification
     * @param type $id
     * @param type $title
     * @param type $desc
     * @return boolean
     */
    public function updateSpecification($id, $title, $desc, $ownerId, $simpleName) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE specification SET last_updated=:lu, title=:title, description=:desc, owner_id=:o, simpleName=:simple WHERE id=:id ");
        $handler->bindParam(':id', $id);
        $handler->bindParam(':o', $ownerId);
        date_default_timezone_set('UTC');
        $localTime = date('Y-m-d H:i:s');
        $handler->bindParam(':lu', $localTime);
        $handler->bindValue(':title', Util::purifyHTML($title));
        $handler->bindValue(':desc', Util::purifyHTML($desc));
        $handler->bindValue(':simple', $simpleName);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
            return false;
        }
        Mediator::emit("specificationUpdated", $id);
        return true;
    }

    public function deleteSpecification($id) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE specification SET is_active=0, simpleName='__deleted_specification__' WHERE id=:id ");
//        $handler = DB_DAO::getDB()->prepare("DELETE FROM specification WHERE id=:id ");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        Mediator::emit("specificationDeleted", $id);
        return true;
    }

    /**
     * Returns array of IDs of suites that belongs to given specification
     * @param type $spec_id
     * @return array
     */
    public function getSuitesIDs($spec_id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT id FROM suite WHERE specification_id =:id");
        $handler->bindParam(':id', $spec_id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $ids = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($ids, intval($row['id']));
        }
        return $ids;
    }

    public function getSpecificationTitleIdBySuiteId($suiteId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT s.title, s.id, v.version FROM specification s, suite su, version v WHERE su.id=:id AND s.is_active=1 AND s.version_id=v.id AND su.specification_id = s.id");
        $handler->bindParam(':id', $suiteId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $data = array();
        $data[0] = "";
        $data[1] = "-1";
        $data[2] = "";
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $data[0] = $row['title'];
            $data[1] = intval($row['id']);
            $data[2] = $row['version'];
        }
        return $data;
    }

    public function getSpecificationsByAuthor($username) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT s.id, s.title, s.description, s.author_id,v.version, s.version_id, s.owner_id FROM specification s, user u, version v WHERE s.author_id=u.id AND s.is_active=1 AND u.username=:username AND s.version_id=v.id");
        $handler->bindParam(':username', $username);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $data = array();

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $s = new Specification(intval($row['id']), $row['description'], $row['title'], $row['version_id'], $row['author_id'], $row['owner_id']);
            $s->version = $row['version'];
            array_push($data, $s);
        }
        return $data;
    }

    public function getSpecificationsByOwner($username) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT s.id, s.title, s.description, s.author_id,v.version, s.version_id, s.owner_id FROM specification s, user u, version v WHERE s.owner_id=u.id AND s.is_active=1 AND u.username=:username AND s.version_id=v.id");
        $handler->bindParam(':username', $username);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $data = array();

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $s = new Specification(intval($row['id']), $row['description'], $row['title'], intval($row['version_id']), $row['author_id'], $row['owner_id']);
            $s->version = $row['version'];
            array_push($data, $s);
        }
        return $data;
    }

    /**
     * @return Specification[]
     */
    public function getSpecificationsByOwnerAndVersion($username, $versionId, $projectId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT s.id, s.title, s.description, s.author_id,v.version, s.version_id, s.owner_id FROM (specification s, user u, version v) JOIN specification_has_project ON specification_has_project.specification_id=s.id  WHERE specification_has_project.project_id=:projectId AND s.owner_id=u.id AND s.is_active=1 AND u.username=:username AND s.version_id=v.id AND v.id=:versionId GROUP BY s.id");
        $handler->bindParam(':username', $username);
        $handler->bindParam(':versionId', $versionId);
        $handler->bindParam(':projectId', $projectId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $data = array();

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $s = new Specification(intval($row['id']), $row['description'], $row['title'], intval($row['version_id']), $row['author_id'], $row['owner_id']);
            $s->version = $row['version'];
            array_push($data, $s);
        }
        return $data;
    }

    public function createSpecification($title, $desc, $versionId, $userId, $ownerId, $simpleName) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO specification (title, description, author_id, version_id, owner_id, last_updated, simpleName, is_active) VALUES (:t, :d, :a, :v, :o, :lu, :s, 1)");
        $handler->bindParam(':v', $versionId);
        date_default_timezone_set('UTC');
        $localTime = date('Y-m-d H:i:s');
        $handler->bindParam(':lu', $localTime);
        $handler->bindParam(':a', $userId);
        $handler->bindParam(':o', $ownerId);
        $handler->bindValue(':t', Util::purifyHTML($title));
        $handler->bindValue(':d', Util::purifyHTML($desc));
        $handler->bindValue(':s', $simpleName);

        if (!$handler->execute()) {

            DB_DAO::throwDbError($handler->errorInfo());
            return -1;
        }
        $newid = DB_DAO::getDB()->lastInsertId();
        Mediator::emit("specificationCreated", $newid);
        return $newid;
    }

    public function cloneSpecificationAttachment($specification, $newSpecId) {
        $att = "";
        for ($i = 0, $max = count($specification->attachments); $i < $max; $i++) {
            $att = $att . "INSERT INTO specification_attachement (path, specification_id) VALUES ('" . $specification->attachments[$i]->getPath() . "', " . $newSpecId . ");";
        }
        if (strlen($att) > 1) {
            DB_DAO::executeQuery($att);
        }
    }

    public function cloneSpecificationCases($cases, $newSuiteId) {
        $sc = "";
        for ($j = 0, $max = count($cases); $j < $max; $j++) {
            $sc = $sc . "INSERT INTO suite_has_case (suite_id, case_id) VALUES (" . $newSuiteId . "," . $cases[$j] . ");";
        }

        if (strlen($sc) > 1) {
            DB_DAO::executeQuery($sc);
        }
    }

    public function findMatchingSpecifications($title, $limit = 15) {
        DB_DAO::connectDatabase();
        $title = strtolower($title);
        $handler = DB_DAO::getDB()->prepare("SELECT specification.id,title, v.version, GROUP_CONCAT(project.name SEPARATOR ';') AS pnames, GROUP_CONCAT(project.id SEPARATOR ';') AS pids FROM (specification, version v) LEFT JOIN specification_has_project shp ON shp.specification_id=specification.id LEFT JOIN project ON project.id=shp.project_id WHERE LOWER(title) LIKE :t AND v.id=specification.version_id AND specification.is_active=1 GROUP BY specification.id ORDER BY title ASC LIMIT 0," . $limit);
        $title = "%" . $title . "%";
        $handler->bindParam(':t', $title, PDO::PARAM_STR);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $results = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $s = new Specification($row['id'], '', $row['title'], -1, -1, -1);
            $s->version = $row['version'];
           $s->setProjects($row["pids"], $row["pnames"]);
            array_push($results, $s);
        }
        return $results;
    }

    /**
     * Returns number of all cases for given specification
     * @param type $id
     * @param type $labelId if greater than 0, then only cases with this label will be counted
     * @return int
     */
    public static function getCasesCount($id, $labelId = 0) {

        if ($labelId < 1) {

            DB_DAO::connectDatabase();
            $handler = DB_DAO::getDB()->prepare("SELECT COUNT(c.case_id) as cid FROM suite s,suite_has_case c WHERE s.specification_id=:id AND s.id=c.suite_id");
            $handler->bindValue(':id', intval($id));

            if (!$handler->execute()) {
                DB_DAO::throwDbError($handler->errorInfo());
            }

            while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
                return $row['cid'];
            }
        } else {
            DB_DAO::connectDatabase();
//            $handler = DB_DAO::getDB()->prepare("SELECT COUNT(c.case_id) as cid FROM suite s,suite_has_case c WHERE s.specification_id=:id AND s.id=c.suite_id");
            $handler = DB_DAO::getDB()->prepare("SELECT COUNT(c.id) as cid FROM (`case` c, case_has_keyword ck, keyword k) JOIN (suite s, suite_has_case sc, specification sp) ON (sc.suite_id=s.id AND sc.case_id=c.id AND sp.id=s.specification_id) WHERE ck.case_id=c.id AND ck.keyword_id=k.id AND k.id=:lid AND sp.id=:id");
            $handler->bindValue(':id', intval($id));
            $handler->bindValue(':lid', intval($labelId));
            if (!$handler->execute()) {
                DB_DAO::throwDbError($handler->errorInfo());
            }

            while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
                return $row['cid'];
            }
        }
        return 0;
    }

    /**
     * Returns $limit latest specfications. Age of specification is derived from ID (auto increment in DB)
     * @param type $limit
     * @return array
     */
    public function getLatestSpecifications($limit) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT specification.id, title, description, author_id, owner_id, simpleName, v.version FROM specification, version v WHERE version_id=v.id AND specification.is_active=1 ORDER BY specification.id DESC LIMIT " . intval($limit));
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $data = array();

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $sp = new Specification(intval($row['id']), $row['description'], $row['title'], -1, $row['author_id'], $row['owner_id']);
            $sp->simpleName = $row['simpleName'];
            $sp->version = $row['version'];
            array_push($data, $sp);
        }
        return $data;
    }

    /**
     * Quazi removes authorship. Because DB constraints have to correct, current user is set as auhtor
     * @param type $userId
     */
    public function deleteAuthorship($userId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE specification SET author_id=:nid WHERE author_id=:id ");
        $handler->bindParam(':id', $userId);
        $handler->bindValue(':nid', Synergy::getSessionProvider()->getUserId());

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
            return false;
        }

        return true;
    }

    /**
     * Quazi removes ownership. Because DB constraints have to correct, current user is set as owner
     * @param type $userId
     */
    public function deleteOwnership($userId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE specification SET owner_id=:nid WHERE owner_id=:id ");
        $handler->bindParam(':id', $userId);
        $handler->bindValue(':nid', Synergy::getSessionProvider()->getUserId());

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
            return false;
        }

        return true;
    }

    /**
     * Returns total number of 
     * @return int
     */
    public function getSpecificationsCount() {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT count(id) as id FROM specification WHERE is_active=1");

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return intval($row['id']);
        }
        return 0;
    }

    public function setLastUpdatedDate($date, $specificationId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE specification SET last_updated=:d WHERE id=:i ");
        $handler->bindParam(':i', $specificationId);
        $handler->bindValue(':d', $date);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
            return false;
        }
        return true;
    }

    public function setAuthorship($userId, $specificationId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE specification SET author_id=:d WHERE id=:i ");
        $handler->bindParam(':i', $specificationId);
        $handler->bindValue(':d', $userId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
            return false;
        }
        return true;
    }

    public function getSpecificationAlias($simpleName, $versionID) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT s.id, s.last_updated,s.title, s.description, s.author_id,s.version_id, v.version, u.username, u.first_name, u.last_name, s.owner_id FROM specification s, version v, user u WHERE s.simpleName=:simple AND s.version_id=:vid AND s.version_id=v.id AND u.id=s.author_id AND s.is_active=1 ORDER BY v.version DESC");
        $handler->bindParam(':simple', $simpleName);
        $handler->bindParam(':vid', $versionID);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $s = new Specification(intval($row['id']), $row['description'], $row['title'], intval($row['version_id']), $row['author_id'], $row['owner_id']);
            $s->author = $row['username'];
            $s->version = $row['version'];
            $s->simpleName = $simpleName;
            $s->authorName = $row['first_name'] . " " . $row['last_name'];
            $s->setLastUpdated($row['last_updated']);
            return $s;
        }
        return null;
    }

    public function getSpecificationVersionID($specificationID) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT s.version_id FROM specification s WHERE s.id=:s");
        $handler->bindParam(':s', $specificationID);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return intval($row['version_id']);
        }
        return -1;
    }

    public function findMatchingSpecification($specificationID, $simpleName, $versionID) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT s.id FROM specification s WHERE s.id!=:s AND s.version_id=:v AND s.is_active=1 AND s.simpleName=:n");
        $handler->bindParam(':s', $specificationID);
        $handler->bindParam(':v', $versionID);
        $handler->bindParam(':n', $simpleName);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return intval($row['id']);
        }
        return -1;
    }

    public function findSimilar($simpleName, $excludedId) {
        DB_DAO::connectDatabase();
        $simpleName = strtolower($simpleName);
        $handler = DB_DAO::getDB()->prepare("SELECT s.id, s.title, v.version FROM specification s, version v WHERE LOWER(s.simpleName)=:s AND s.version_id=v.id AND s.id!=:e AND s.is_active=1 ORDER BY v.version DESC");
        $handler->bindParam(':s', $simpleName);
        $handler->bindParam(':e', $excludedId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $data = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $s = new Specification($row['id'], '', $row['title'], -1, -1, -1);
            $s->version = $row['version'];
            array_push($data, $s);
        }
        return $data;
    }

    public function setSimpleName($id, $title) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE specification SET simpleName=:d WHERE id=:i ");
        $handler->bindParam(':i', $id);
        $handler->bindValue(':d', $title);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
            return false;
        }
        return true;
    }

    public function getAllSpecificationsGroupedBySimpleName() {
        DB_DAO::connectDatabase();
        $simpleName = strtolower($simpleName);
        $handler = DB_DAO::getDB()->prepare("SELECT s.simpleName as simple, GROUP_CONCAT(u.username SEPARATOR ';') as owners, GROUP_CONCAT(u.role SEPARATOR ';') as roles,GROUP_CONCAT(s.id SEPARATOR ';') as ids, GROUP_CONCAT(s.title SEPARATOR ';') as titles, GROUP_CONCAT(v.version SEPARATOR ';') as versions FROM specification s, version v, user u WHERE v.isObsolete=0 AND s.version_id=v.id AND s.is_active=1 AND LENGTH(s.simpleName) >0 AND u.id=s.owner_id GROUP BY s.simpleName ORDER BY s.simpleName ASC");
        $handler->bindParam(':s', $simpleName);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $latestSpecs = array();
        $dataAssoc = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $c = new SpecificationsSimpleNameList($row['simple']);
            $c->populate($row['ids'], $row['titles'], $row['versions'], $row['owners'], $row['roles']);
            $dataAssoc["id" . $c->getLatestId()] = $c;
            $latestSpecs[] = $c->getLatestId();
        }

        $projectCtrl = new ProjectCtrl();
        $projects = $projectCtrl->getProjectsForSpecifications($latestSpecs);

        foreach ($dataAssoc as $group) {
            if (array_key_exists("id" . $group->getLatestId(), $projects)) {
                $group->projects = $projects["id" . $group->getLatestId()];
            }
        }
        return array_values($dataAssoc);
    }

    public function updateSimpleNames($oldSipleName, $newSimpleName) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE specification SET simpleName=:d WHERE simpleName=:i ");
        $handler->bindParam(':i', $oldSipleName);
        $handler->bindValue(':d', $newSimpleName);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function isUniqueSimpleName($newSimpleName) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT id FROM specification s WHERE simpleName=:s LIMIT 0,1");
        $handler->bindParam(':s', $newSimpleName);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return false;
        }
        return true;
    }

    public function getSpecificationAliasLatest($simpleName) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT s.id, s.last_updated,s.title, s.description, s.author_id,s.version_id, v.version, u.username, u.first_name, u.last_name, s.owner_id FROM specification s, version v, user u WHERE s.simpleName=:simple AND s.version_id=v.id AND s.is_active=1 AND u.id=s.author_id ORDER BY v.version DESC LIMIT 0,1");
        $handler->bindParam(':simple', $simpleName);


        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $s = new Specification(intval($row['id']), $row['description'], $row['title'], intval($row['version_id']), $row['author_id'], $row['owner_id']);
            $s->author = $row['username'];
            $s->version = $row['version'];
            $s->simpleName = $simpleName;
            $s->authorName = $row['first_name'] . " " . $row['last_name'];
            $s->setLastUpdated($row['last_updated']);
            return $s;
        }
        return null;
    }

    public function isSpecificationFavorite($specificationId, $userId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT s.id FROM specification s, user_has_favorite u WHERE u.user_id=:user AND u.specification_id=:spec LIMIT 0,1");
        $handler->bindParam(':spec', $specificationId);
        $handler->bindParam(':user', $userId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return 1;
        }
        return 0;
    }

    public function isSpecificationUsed($specificationId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT s.id FROM specification s, assignment_progress p, test_assignement t WHERE s.id=:id AND s.id=t.specification_id AND t.id=p.test_assignement_id LIMIT 0,1");
        $handler->bindParam(':id', $specificationId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return 1;
        }
        return 0;
    }

    public function getOwnerId($specificationId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT owner_id FROM specification WHERE id=:id");
        $handler->bindParam(':id', $specificationId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return intval($row["owner_id"]);
        }
        return -1;
    }

    public function getSpecificationOverview($specificationId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT u.username, s.title, s.id, u.id AS uid FROM specification s LEFT JOIN user u ON u.id=s.owner_id WHERE s.id=:id ");
        $handler->bindParam(':id', $specificationId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $s = new Specification($row["id"], "", $row["title"], "", "", "");
            $s->owner = $row["username"];
            $s->ownerId = intval($row["uid"]);
            return $s;
        }
        return null;
    }

    public function getSpecificationsForProject($projectId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT s.id, s.title, s.description, s.author_id, s.owner_id, s.simpleName, s.version_id, v.version FROM specification s, version v, specification_has_project shp WHERE shp.specification_id=s.id AND shp.project_id=:p AND v.id=s.version_id AND v.isObsolete=0 AND s.is_active=1 GROUP BY s.id ORDER BY s.title ASC");
        $handler->bindParam(':p', $projectId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $data = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $sp = new Specification(intval($row['id']), $row['description'], $row['title'], $row['version_id'], $row['author_id'], $row['owner_id']);
            $sp->version = $row['version'];
            $sp->simpleName = $row['simpleName'];
            array_push($data, $sp);
        }
        return $data;
    }

}

?>