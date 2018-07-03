<?php

namespace Synergy\DB;

use PDO;
use Synergy\Model\Job;

/**
 * DAO for Continuous integration extension
 *
 * @author vriha
 */
class CiDAO {

    /**
     * Returns array of jobs for given specification
     * @param int $specificationId
     * @return Job|Array
     */
    public function getJobsUrl($specificationId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT job_url, id FROM jobs WHERE specification_id=:id");
        $handler->bindParam(':id', $specificationId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $results = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($results, new Job($row['id'], $specificationId, $row['job_url']));
        }
        return $results;
    }

    /**
     * Returns true if given specification already has given job URL
     * @param type $specificationId
     * @param type $jobUrl
     * @return boolean
     */
    public function jobUrlExist($specificationId, $jobUrl) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT id FROM jobs WHERE specification_id=:id AND job_url=:j");
        $handler->bindParam(':id', $specificationId);
        $handler->bindParam(':j', $jobUrl);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return true;
        }
        return false;
    }

    public function createJobUrl($specificationId, $jobUrl) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO jobs (specification_id, job_url) VALUES (:s, :u)");
        $handler->bindParam(':s', $specificationId);
        $handler->bindParam(':u', $jobUrl);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function editJobUrl($jobId, $specificationId, $jobUrl) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE jobs SET job_url=:j WHERE specification_id=:s AND id=:i)");
        $handler->bindParam(':s', $specificationId);
        $handler->bindParam(':j', $jobUrl);
        $handler->bindParam(':i', $jobId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    /**
     * Deletes all jobs for given specification
     * @param int $specificationId
     */
    public function deleteJobsForSpecification($specificationId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM jobs WHERE specification_id=:id");
        $handler->bindParam(':id', $specificationId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function deleteJob($jobId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM jobs WHERE id=:id");
        $handler->bindParam(':id', $jobId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

}

?>