<?php

namespace Synergy\DB;

use PDO;
use Synergy\Model\CommentType;

/**
 * Description of CommentsDAO
 *
 * @author vriha
 */
class CommentsDAO {

    public function getCommentTypes() {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT name, id FROM comment");
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $results = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($results, new CommentType($row["name"], $row["id"]));
        }
        return $results;
    }

}
