<?php

namespace Synergy\DB;

use PDO;
use Synergy\Controller\Mediator;
use Synergy\Misc\Util;
use Synergy\Model\User;
use Synergy\Model\UsersResult;

/**
 * Description of UserDAO
 *
 * @author vriha
 */
class UserDAO {

    /**
     * Removes specification from list of favorites
     * @param type $id specification ID
     */
    public function deleteFavoriteSpecification($id) {
        DB_DAO::connectDatabase();

        $handler = DB_DAO::getDB()->prepare("DELETE FROM user_has_favorite WHERE specification_id=:id ");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    /**
     * Returns user's id based on his username
     * @param String $username username
     * @return int User's ID or -1 if user was not found
     */
    public function getUserIDbyUsername($username) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT id FROM user WHERE username=:username");
        $handler->bindParam(':username', $username);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }


        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return intval($row['id']);
        }
        return -1;
    }

    /**
     * Returns array of users with username matching %username%
     * @param type $username
     * @return User[]
     */
    public function findMatchingUsers($username) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT username FROM user WHERE username LIKE :username ORDER BY username ASC LIMIT 0,15");
        $username = "%" . $username . "%";
        $handler->bindParam(':username', $username, PDO::PARAM_STR);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $results = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            array_push($results, new User($row['username']));
        }
        return $results;
    }

    /**
     * Returns true if user is member of tribe
     * @param int $uid User id 
     * @param int $id Tribe id
     * @return boolean
     */
    public function isMemberOfTribe($uid, $id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT user_id FROM user_is_member_of WHERE user_id=:uid AND tribe_id=:tid");
        $handler->bindParam(':uid', $uid);
        $handler->bindParam(':tid', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return true;
        }
        return false;
    }

    /**
     * Add given specification to user's favorite list
     * @param type $userId
     * @param type $specificationId
     */
    public function addFavorite($userId, $specificationId) {
        DB_DAO::connectDatabase();

        $handler = DB_DAO::getDB()->prepare("INSERT INTO user_has_favorite (specification_id, user_id) VALUES (:sid, :uid)");
        $handler->bindParam(':uid', $userId);
        $handler->bindParam(':sid', $specificationId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    /**
     * Removes favorite specification for given user
     * @param type $userId
     * @param type $specificationId
     */
    public function removeFavorite($userId, $specificationId) {
        DB_DAO::connectDatabase();

        $handler = DB_DAO::getDB()->prepare("DELETE FROM user_has_favorite WHERE specification_id=:sid AND user_id=:uid");
        $handler->bindParam(':uid', $userId);
        $handler->bindParam(':sid', $specificationId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    /**
     * Returns username for user given by id
     * @param type $id
     * @return string
     */
    public function getUsernameById($id) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT username FROM user WHERE id=:id");
        $handler->bindParam(':id', $id);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return $row['username'];
        }
        return '';
    }

    /**
     * Returns list of users
     * @param type $page
     * @return UsersResult Description
     */
    public function findUsers($page) {
        $start = intval((($page - 1) * USERS_PAGE));
        $stop = intval((USERS_PAGE));
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT username, first_name, last_name, role FROM user ORDER BY first_name ASC LIMIT " . $start . "," . $stop);


        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $result = new UsersResult($page);
        $users = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $u = new User($row['username']);
            $u->firstName = $row['first_name'];
            $u->lastName = $row['last_name'];
            $u->role = $row['role'];
            array_push($users, $u);
        }
        $result->users = $users;
        if (count($result->users) < USERS_PAGE) {
            $result->nextUrl = "";
        }
        return $result;
    }

    /**
     * Removes all favorite specifications for given user
     * @param int $userId
     */
    public function deleteFavorites($userId) {
        DB_DAO::connectDatabase();

        $handler = DB_DAO::getDB()->prepare("DELETE FROM user_has_favorite WHERE user_id=:uid");
        $handler->bindParam(':uid', $userId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function deleteUser($userId, $username) {
        DB_DAO::connectDatabase();

        $handler = DB_DAO::getDB()->prepare("DELETE FROM user WHERE username=:uid");
        $handler->bindParam(':uid', $username);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        Mediator::emit("userDeleted", array("username" => $username, "userId" => $userId));
    }

    /**
     * Updates user
     * @param type $firstName
     * @param type $lastName
     * @param type $role
     * @param type $username
     */
    public function editUser($firstName, $lastName, $role, $username, $oldUsername, $emailNotifications, $email, $password) {
        DB_DAO::connectDatabase();
        if (isset($password) && !is_null($password)) {
            $handler = DB_DAO::getDB()->prepare("UPDATE user SET first_name=:f, last_name=:l,username=:u, role=:r,email=:m, email_notifications=:e, passwd=:p WHERE username=:o");
            $md5psw = md5($password . SALT);
            $handler->bindValue(':p', $md5psw);
        } else {
            $handler = DB_DAO::getDB()->prepare("UPDATE user SET first_name=:f, last_name=:l,username=:u, role=:r,email=:m, email_notifications=:e WHERE username=:o");
        }

        $handler->bindParam(':u', Util::purifyHTML($username));
        $handler->bindParam(':o', $oldUsername);
        $handler->bindValue(':f', Util::purifyHTML($firstName));
        $handler->bindValue(':m', Util::purifyHTML($email));
        $handler->bindValue(':l', Util::purifyHTML($lastName));
        $handler->bindValue(':e', ($emailNotifications ? 1 : 0));
        $handler->bindValue(':r', Util::purifyHTML($role));

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
            return false;
        }
        Mediator::emit("userUpdated", $username);
        return true;
    }

    /**
     * Creates new user
     * @param type $firstName
     * @param type $lastName
     * @param type $role
     * @param type $username
     * @return int  new userID
     */
    public function createUser($firstName, $lastName, $role, $username, $email, $emailNotifications, $password) {
        DB_DAO::connectDatabase();
        if (isset($password) && !is_null($password)) {
            $handler = DB_DAO::getDB()->prepare("INSERT INTO user (first_name, last_name, role, username, email, email_notifications, passwd) VALUES (:f, :l, :r, :u, :m, :e, :p)");
            $md5psw = md5($password . SALT);
            $handler->bindValue(':p', $md5psw);
        } else {
            $handler = DB_DAO::getDB()->prepare("INSERT INTO user (first_name, last_name, role, username, email, email_notifications) VALUES (:f, :l, :r, :u, :m, :e)");
        }

        $handler->bindValue(':u', Util::purifyHTML($username));
        $handler->bindValue(':f', Util::purifyHTML($firstName));
        $handler->bindValue(':l', Util::purifyHTML($lastName));
        $handler->bindValue(':r', Util::purifyHTML($role));
        $handler->bindValue(':e', ($emailNotifications ? 1 : 0));
        $handler->bindValue(':m', Util::purifyHTML($email));

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $newid = DB_DAO::getDB()->lastInsertId();
        Mediator::emit("userCreated", $username);
        return $newid;
    }

    /**
     * Returns user
     * @param type $username
     * @return \User|null
     */
    public function getUser($username) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT role, first_name, last_name, user.id, email_notifications, image_path, email FROM user LEFT JOIN user_image ON user_image.user_id=user.id WHERE username=:u");
        $handler->bindParam(':u', $username);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $u = new User($username);
            $u->firstName = $row['first_name'];
            $u->lastName = $row['last_name'];
            $u->role = $row['role'];
            $u->email = $row['email'];
            $u->id = intval($row['id']); // FIXME
            $u->emailNotifications = (intval($row['email_notifications']) === 1 ? true : false);
            if (!is_null($row["image_path"])) {
                $u->profileImg = IMAGE_BASE . $row["image_path"];
            }
            return $u;
        }
        return null;
    }

    /**
     * Returns all users
     * @return \User|null
     */
    public function getAllUsers() {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT username, first_name, last_name, role FROM user ORDER BY first_name ASC ");


        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
        $result = new UsersResult(1);
        $users = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $u = new User($row['username']);
            $u->firstName = $row['first_name'];
            $u->lastName = $row['last_name'];
            $u->role = $row['role'];
            array_push($users, $u);
        }
        $result->users = $users;
        if (count($result->users) !== USERS_PAGE) {
            $result->nextUrl = "";
        }
        return $result;
    }

    /**
     * Returns total number of users
     * @return int
     */
    public function getUsersCount() {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT count(id) as id FROM user");

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return intval($row['id']); // FIXME
        }
        return 0;
    }

    /**
     * Edits username - only first and last name
     * @param type $firstName
     * @param type $lastName
     * @param type $username
     * @return boolean true if OK
     */
    public function editUserSimple($firstName, $lastName, $username, $emailNotifications, $email, $password) {
        DB_DAO::connectDatabase();

        if (isset($password) && !is_null($password)) {
            $handler = DB_DAO::getDB()->prepare("UPDATE user SET first_name=:f, last_name=:l, email_notifications=:e, email=:m, passwd=:p WHERE username=:u");
            $md5psw = md5($password . SALT);
            $handler->bindValue(':p', $md5psw);
        } else {
            $handler = DB_DAO::getDB()->prepare("UPDATE user SET first_name=:f, last_name=:l, email_notifications=:e, email=:m WHERE username=:u");
        }


        $handler->bindParam(':u', $username);
        $handler->bindValue(':f', Util::purifyHTML($firstName));
        $handler->bindValue(':l', Util::purifyHTML($lastName));
        $handler->bindValue(':e', ($emailNotifications ? 1 : 0));
        $handler->bindValue(':m', Util::purifyHTML($email));
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
            return false;
        }
        Mediator::emit("userUpdated", $username);
        return true;
    }

    public function getUserById($userId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT role, first_name, last_name, username FROM user WHERE id=:u");
        $handler->bindParam(':u', $userId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $u = new User($row['username']);
            $u->firstName = $row['first_name'];
            $u->lastName = $row['last_name'];
            $u->role = $row['role'];
            $u->id = intval($userId); // FIXME
            return $u;
        }
        return null;
    }

    public function getManagers() {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT username, first_name, last_name, role,email FROM user WHERE role='manager' LIMIT 0, 10");

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        $users = array();
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $u = new User($row['username']);
            $u->firstName = $row['first_name'];
            $u->lastName = $row['last_name'];
            $u->role = $row['role'];
            $u->email = $row['email'];
            array_push($users, $u);
        }


        return $users;
    }

    public function removeProfileImg($userId) {
        DB_DAO::connectDatabase();

        $handler = DB_DAO::getDB()->prepare("DELETE FROM user_image WHERE user_id=:uid");
        $handler->bindParam(':uid', $userId);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function getProfileImgPath($userId) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT image_path FROM user_image WHERE user_id=:id");
        $handler->bindParam(':id', $userId);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }

        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return $row["image_path"];
        }

        return "";
    }

    public function addProfileImg($userId, $path) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("INSERT INTO user_image (image_path, user_id) VALUES (:p, :i)");
        $handler->bindParam(':i', $userId);
        $handler->bindParam(':p', $path);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }
    }

    public function getUserIdByEmail($email) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("SELECT id FROM user WHERE email=:e");
        $handler->bindParam(':e', $email);

        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
        }


        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            return intval($row['id']);
        }
        return -1;
    }

    public function retireUsers($roleToRetire) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE user SET role='formerUser' WHERE role=:r");
        $handler->bindParam(':r', $roleToRetire);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
            return false;
        }
        return true;
    }

    public function setRole($username, $role) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE user SET role=:r WHERE username=:u");
        $handler->bindParam(':u', $username);
        $handler->bindParam(':r', $role);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
            return false;
        }
        return true;
    }

    public function changePassword($username, $password) {
        DB_DAO::connectDatabase();
        $handler = DB_DAO::getDB()->prepare("UPDATE user SET passwd=:p WHERE username=:u");
        $handler->bindParam(':u', $username);
        $md5psw = md5($password . SALT);
        $handler->bindValue(':p', $md5psw);
        if (!$handler->execute()) {
            DB_DAO::throwDbError($handler->errorInfo());
            return false;
        }
        return true;
    }
}

?>
