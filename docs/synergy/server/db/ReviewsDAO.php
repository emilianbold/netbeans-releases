<?php

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

namespace Synergy\DB;

use PDO;
use Synergy\Model\Review\ReviewPage;

/**
 * Description of ReviewsDAO
 *
 * @author vriha
 */
class ReviewsDAO {

    public function removeAll() {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM review_pages");

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function getAll() {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT * FROM review_pages");

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $results = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $results[] = new ReviewPage($row["title"], $row["owner"], $row["url"]);
        }
        return $results;
    }

    public function getAllNotStarted($runId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT rp.*, ra.id as rad FROM review_pages rp LEFT OUTER JOIN review_assignment ra ON (ra.review_url=rp.url AND ra.test_run_id=:t) HAVING rad IS NULL");
        $handler->bindParam(":t", $runId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $results = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $results[] = new ReviewPage($row["title"], $row["owner"], $row["url"]);
        }
        return $results;
    }

    /**
     * 
     * @param ReviewPage $pages
     */
    public function insertAll($pages) {
        if (count($pages) < 1) {
            return;
        }

        DB_DAO::connectDatabase();
        $baseSql = "INSERT INTO review_pages (url, title, owner) VALUES ";

        for ($i = 0, $max = count($pages); $i < $max; $i++) {
            $key = $i . time();
            $baseSql = $baseSql . "(:" . $key . "url,:" . $key . "title,:" . $key . "owner),";
            $pages[$i]->setHash($key);
        }
        $baseSql = substr($baseSql, 0, strlen($baseSql) - 1);
        error_log($baseSql);
        $handler = DB_DAO::getDB()->prepare($baseSql);

        for ($i = 0, $max = count($pages); $i < $max; $i++) {
            $key = $pages[$i]->getHash();
            $handler->bindValue(':' . $key . "url", $pages[$i]->url);
            $handler->bindValue(':' . $key . "title", $pages[$i]->title);
            $handler->bindValue(':' . $key . "owner", $pages[$i]->owner);
        }

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

}
