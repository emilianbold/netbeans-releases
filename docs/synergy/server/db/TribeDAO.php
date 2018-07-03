<?php

namespace Synergy\DB;

use PDO;
use Synergy\App\Synergy;
use Synergy\Controller\Mediator;
use Synergy\Misc\Util;
use Synergy\Model\Membership;
use Synergy\Model\Tribe;
use Synergy\Model\User;

/**
 * Description of TribeDAO
 *
 * @author lada
 */
class TribeDAO {

    public function getUserMembership($username) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT t.id, t.name, t.leader_id, u.id AS uid FROM tribe t, user_is_member_of m, user u WHERE u.username=:username AND m.user_id=u.id AND t.id=m.tribe_id");
        $handler->bindParam(':username', $username);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $data = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $m = new Membership($row['id'], $row['name']);
            $m->role = "Member";
            array_push($data, $m);
        }
        return $data;
    }

    /**
     * Returns instance of Tribe (information + list of members)
     * @param int $id
     * @return Tribe
     */
    public function getTribe($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT name, t.id as tid, leader_id, description, username, first_name, last_name FROM tribe t, user WHERE t.id=:id AND user.id=t.leader_id");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $m = new Tribe($row['tid'], $row['name'], $row['description'], $row['leader_id']);
            $m->leaderUsername = $row['username'];
            $m->leaderDisplayName = $row['first_name'] . " " . $row['last_name'];
            return $m;
        }
        return null;
    }

    /**
     * Returns array of users that are members of tribe given by ID
     * @param int $id
     * @return User[]
     */
    public function getTribeMembers($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT user.username, user.first_name, user.last_name, user.id, user_image.image_path FROM (user, user_is_member_of u) LEFT JOIN user_image ON user_image.user_id=user.id  WHERE user.id=u.user_id AND u.tribe_id=:id ORDER BY username ASC");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $data = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $u = new User($row['username']);
            $u->id = intval($row['id']);
            $u->firstName = $row['first_name'];
            $u->lastName = $row['last_name'];
            if (!is_null($row["image_path"])) {
                $u->profileImg = IMAGE_BASE . $row["image_path"];
            }
            array_push($data, $u);
        }
        return $data;
    }

    /**
     * Removes user from tribe
     * @param String $username
     * @param int $id Tribe ID
     */
    public function removeMember($username, $id, $user_id) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM user_is_member_of WHERE user_id=:id AND tribe_id=:tid");
        $handler->bindParam(':id', $user_id);
        $handler->bindParam(':tid', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        Mediator::emit("tribeMemberDeleted", array("tribeId" => $id, "userId" => $user_id));
    }

    /**
     * Returns leader
     * @param int $id tribe id
     * @return User
     */
    public function getLeader($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT username, user.id FROM user, tribe WHERE user.id=tribe.leader_id AND tribe.id=:id");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }


        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $u = new User($row['username']);
            $u->id = intval($row['id']);
            return $u;
        }
        return null;
    }

    public function editTribe($name, $desc, $id, $uid) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE tribe SET name=:name, description=:desc, leader_id=:lid WHERE id=:id ");
        $handler->bindParam(':id', $id);
        $handler->bindParam(':lid', $uid);
        $handler->bindValue(':name', Util::purifyHTML($name));
        $handler->bindValue(':desc', Util::purifyHTML($desc));

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
            return false;
        }
        Mediator::emit("tribeUpdated", $id);
        return true;
    }

    /**
     * Add user to tribe
     * @param String $username
     * @param int $id tribe id
     * @return boolean true if successful
     */
    public function addMember($uid, $id) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO user_is_member_of (user_id, tribe_id) VALUES (:uid, :tid)");
        $handler->bindParam(':tid', $id);
        $handler->bindParam(':uid', $uid);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
            return false;
        }

        Mediator::emit("tribeMemberAdded", array("tribeId" => $id, "userId" => $uid));
        return true;
    }

    /**
     * Returns basic information about all tribes (no information about members)
     * @return Tribe[]
     */
    public function getTribes() {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT name, t.id, t.description, t.leader_id FROM tribe t ORDER BY name DESC LIMIT 0,100");

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $data = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $t = new Tribe($row['id'], $row['name'], $row['description'], $row['leader_id']);
            array_push($data, $t);
        }
        return $data;
    }

    /**
     * Removes given tribe and all membership
     * @param type $id
     */
    public function removeTribe($id) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM tribe WHERE id=:id");
        $handler->bindParam(':id', $id);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        Mediator::emit("tribeDeleted", $id);
        return true;
    }

    public function removeMembersOfTribe($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM user_is_member_of WHERE tribe_id=:tid");
        $handler->bindParam(':tid', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function removeMembersOfAllTribes() {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM user_is_member_of");
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    /**
     * Creates a new tribe
     * @param type $leaderUsername
     * @param type $description
     * @param type $name
     * @return int ID of a new tribe, 0 otherwise
     */
    public function createTribe($uid, $description, $name) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO tribe (name, description, leader_id) VALUES (:name, :desc, :lid)");
        $handler->bindValue(':name', Util::purifyHTML($name));
        $handler->bindValue(':desc', Util::purifyHTML($description));
        $handler->bindValue(':lid', intval($uid));

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $newid = DB_DAO::getDB()->lastInsertId();
        Mediator::emit("tribeCreated", $newid);

        return $newid;
    }

    /**
     * Removes user from all tribes
     * @param int $userId
     */
    public function removeAllMemberships($userId) {

        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("DELETE FROM user_is_member_of WHERE user_id=:id");
        $handler->bindParam(':id', $userId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        Mediator::emit("tribeMemberDeleted", array("tribeId" => -1, "userId" => $userId));
    }

    /**
     * Removes tribe leader and replace it with current user
     * @param type $userId
     * @return boolean
     */
    public function removeLeader($userId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE tribe SET leader_id=:nid WHERE leader_id=:id ");
        $handler->bindParam(':id', $userId);
        $handler->bindValue(':nid', Synergy::getSessionProvider()->getUserId());

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
            return false;
        }

        return true;
    }

    /**
     * Returns total number of tribes
     * @return int
     */
    public function getTribesCount() {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT count(id) as id FROM tribe");

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return $row['id'];
        }
        return 0;
    }

    public function getUserLeadership($userId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT t.id, t.name FROM tribe t WHERE t.leader_id=:id");
        $handler->bindParam(':id', $userId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $data = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $m = new Membership($row['id'], $row['name']);
            $m->role = "Leader";
            array_push($data, $m);
        }
        return $data;
    }

    public function tribeExists($tribeName) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT t.id FROM tribe t WHERE t.name=:n");
        $handler->bindParam(':n', $tribeName);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return true;
        }
        return false;
    }

    public function getTribeIdByName($tribeName) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT t.id FROM tribe t WHERE t.name=:n");
        $handler->bindParam(':n', $tribeName);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return intval($row['id']);
        }
        return -1;
    }

    /**
     * 
     * @return Tribe[]
     */
    public function getTribesNameAndLeaders() {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT t.name, u.username, t.id FROM tribe t, user u WHERE t.leader_id=u.id");

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $results = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $t = new Tribe(intval($row["id"]), $row["name"], "", -1);
            $t->leaderUsername = $row["username"];
            array_push($results, $t);
        }
        return $results;
    }

    public function getTribeAndSpecifications($tribeSqlString) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT t.id, GROUP_CONCAT(s.specification_id SEPARATOR ';') AS specs FROM tribe t, tribe_has_specification s WHERE s.tribe_id=t.id AND " . $tribeSqlString . " GROUP BY t.id");
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $results = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($results, new TribeOverview(intval($row["id"]), $row["specs"]));
        }
        return $results;
    }

}

class TribeOverview {

    public $id;
    public $specificationIds;

    function __construct($id, $specsString) {
        $this->id = intval($id);
        $this->specificationIds = array();

        if (!is_null($specsString) && strlen($specsString) > 0) {
            $this->specificationIds = explode(";", $specsString);
        }
    }

}

?>
