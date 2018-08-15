<?php

namespace Synergy\DB;

use PDO;
use Synergy\Controller\Mediator;
use Synergy\Misc\Util;
use Synergy\Model\Project\Project;

/**
 * Description of ProjectDAO
 *
 */
class ProjectDAO {

    public function getProjects() {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT id, name,report_link,display_link,multi_display_link FROM project ORDER BY name");

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $results = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $p = new Project(intval($row["id"], 10), $row["name"]);
            $p->reportLink = $row["report_link"];
            $p->viewLink = $row["display_link"];
            $p->multiViewLink = $row["multi_display_link"];
            $results[] = $p;
        }

        return $results;
    }

    public function getProjectsForSpecification($specificationId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT p.id, p.name,p.bug_tracking_system FROM specification_has_project sp, project p WHERE sp.specification_id=:id AND sp.project_id=p.id ORDER BY id ASC");
        $handler->bindParam(':id', $specificationId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $results = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $p = new Project(intval($row["id"], 10), $row["name"]);
            $p->setBugTrackingSystem($row["bug_tracking_system"]);
            $results[] = $p;
        }

        return $results;
    }

    public function removeProjectsForSpecification($deletedId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM specification_has_project WHERE specification_id=:id");
        $handler->bindParam(':id', $deletedId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        return true;
    }

    public function isNameUsed($name, $excludeID) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT name FROM project WHERE name=:v AND id!=:i");
        $handler->bindValue(":v", Util::purifyHTML($name));
        $handler->bindValue(":i", $excludeID);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return true;
        }
        return false;
    }

    public function updateProject($id, $newname, $reportLink, $viewLink, $multiViewLink, $bugTrackingSystem) {
        if (is_null($reportLink)) {
            $reportLink = "";
        }
        if (is_null($viewLink)) {
            $viewLink = "";
        }
        if (is_null($multiViewLink)) {
            $multiViewLink = "";
        }
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE project v SET v.name=:v, report_link=:r, display_link=:d,multi_display_link=:m,bug_tracking_system=:t  WHERE id=:id");
        $handler->bindValue(":v", Util::purifyHTML($newname));
        $handler->bindParam(":id", $id);
        $handler->bindParam(":d", $viewLink);
        $handler->bindParam(":r", $reportLink);
        $handler->bindParam(":m", $multiViewLink);
        $handler->bindParam(":t", $bugTrackingSystem);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        Mediator::emit("ProjectUpdated", $id);

        return true;
    }

    public function createProject($projectName) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO project (name) VALUES (:v)");
        $handler->bindValue(":v", Util::purifyHTML($projectName));

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $pid = DB_DAO::getDB()->lastInsertId();
        Mediator::emit("ProjectCreated", $pid);

        return $pid;
    }

    /**
     * Removes project but leaves all specifications
     * @param type $projectId
     * @return boolean
     */
    public function deleteProject($projectId) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM project WHERE id=:id ");
        $handler->bindParam(':id', $projectId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $this->deleteProjectFromSpecs($projectId);
        Mediator::emit("versionDeleted", $projectId);

        return true;
    }

    private function deleteProjectFromSpecs($projectId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM specification_has_project WHERE project_id=:id ");
        $handler->bindParam(':id', $projectId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function deleteProjectForSpecification($specificationId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM specification_has_project WHERE specification_id=:id ");
        $handler->bindParam(':id', $specificationId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        return true;
    }

    public function addProjectToSpecification($specificationId, $projectId) {
        error_log($specificationId . " " . $projectId);
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO specification_has_project (specification_id, project_id) VALUES (:s, :p)");
        $handler->bindValue(":s", $specificationId);
        $handler->bindValue(":p", $projectId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        return true;
    }

    public function getProjectsForSpecifications($latestSpecs) {
        $sqlString = Util::arrayToSQLOR($latestSpecs, "sp.specification_id");

        DB_DAO::connectDatabase();
        // https://dev.mysql.com/doc/refman/5.7/en/sql-mode.html#sqlmode_only_full_group_by
        $handler = DB_DAO::getDB()->prepare("SELECT sp.specification_id, GROUP_CONCAT(p.id SEPARATOR ';') AS pids , GROUP_CONCAT(p.name SEPARATOR ';') as pnames FROM specification_has_project sp, project p WHERE sp.project_id=p.id AND " . $sqlString . " GROUP BY sp.specification_id ORDER BY sp.specification_id ASC");

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $results = array();
        $projects = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {

            $projects = array();
            $pids = explode(";", $row["pids"]);
            $pnames = explode(";", $row["pnames"]);

            foreach ($pids as $index => $value) {
                $projects[] = new Project($value, $pnames[$index]);
            }

            $results["id" . $row["specification_id"]] = $projects;
        }

        return $results;
    }

    public function getProjectDetailed($projectId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT name,id,report_link,display_link,multi_display_link,bug_tracking_system FROM project WHERE id=:i");
        $handler->bindValue(":i", $projectId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $p = new Project($row["id"], $row["name"]);
            $p->setBugTrackingSystem($row["bug_tracking_system"]);
            $p->viewLink = $row["display_link"];
            $p->multiViewLink = $row["multi_display_link"];
            $p->reportLink = $row["report_link"];
            return $p;
        }
        return null;
    }

    public function getSpecificationsIds($projectId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT DISTINCT specification_id FROM specification_has_project WHERE project_id=:i GROUP BY specification_id");
        $handler->bindValue(":i", $projectId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $ids = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $ids["id" . $row["specification_id"]] = intval($row["specification_id"], 10);
        }
        return $ids;
    }

}
