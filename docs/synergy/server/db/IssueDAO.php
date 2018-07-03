<?php

namespace Synergy\DB;

use PDO;
use Synergy\Model\Bug;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of IssueDAO
 *
 * @author vriha
 */
class IssueDAO {

    public function getIssue($id) {
        $escapedId = intval($id);
        Bugzilla_DAO::connectDatabase();
        $handler = Bugzilla_DAO::getDB()->prepare("SELECT b.bug_status, b.short_desc, b.resolution FROM bugs b WHERE bug_id=:id");
        $handler->bindParam(':id', $escapedId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $b = new Bug(-1, $escapedId);
            $b->resolution = $row['bug_status'];
            $b->status = $row['bug_status'];
            if (!is_null($row['resolution']) && strlen($row['resolution']) > 1) {
                $b->resolution = $b->resolution . " " . $row['resolution'];
            }
            $b->title = $row['short_desc'];
            return $b;
        }
        return new Bug(-1, $id);
    }

    public function validateIssues($issues) {
        $idString = "";
        foreach ($issues as $issue) {
            if (strlen($issue) > 0) {
                $idString = $idString . " OR b.bug_id='" . $issue."'";
            }
        }
        if (strlen($idString) < 1) {
            return array();
        }

        $idString = "(" . substr($idString, 3) . ")";

        Bugzilla_DAO::connectDatabase();
        $handler = Bugzilla_DAO::getDB()->prepare("SELECT DISTINCT b.bug_id, b.priority, p.login_name, b.bug_status, b.resolution FROM bugs b, profiles p WHERE p.userid=b.reporter AND " . $idString);
        
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $bugs = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($bugs, Bug::createBug($row["bug_id"], $row["login_name"], $row["priority"], $row["creation_ts"], $row["bug_status"], $row["resolution"]));
        }
        return $bugs;
    }
    public function validateIssuesAssoc($issues) {
        $idString = "";
        foreach ($issues as $issue) {
            if (strlen($issue) > 0) {
                $idString = $idString . " OR b.bug_id='" . $issue."'";
            }
        }
        if (strlen($idString) < 1) {
            return array();
        }

        $idString = "(" . substr($idString, 3) . ")";

        Bugzilla_DAO::connectDatabase();
        $handler = Bugzilla_DAO::getDB()->prepare("SELECT DISTINCT b.bug_id, b.priority, p.login_name, b.bug_status, b.resolution FROM bugs b, profiles p WHERE p.userid=b.reporter AND " . $idString);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $bugs = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $bugs["id".$row["bug_id"]] = Bug::createBug($row["bug_id"], $row["login_name"], $row["priority"], $row["creation_ts"], $row["bug_status"], $row["resolution"]);
        }
        return $bugs;
    }

}

?>