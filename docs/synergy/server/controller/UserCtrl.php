<?php

namespace Synergy\Controller;

use Synergy\DB\TribeDAO;
use Synergy\DB\UserDAO;
use Synergy\Model\CurlRequestResult;
use Synergy\Model\Exception\CurlRequestException;
use Synergy\Model\Exception\UserException;
use Synergy\Model\User;
use Synergy\Model\UsersResult;
use Synergy\App\Synergy;

/**
 * Description of UserCtrl
 *
 * @author lada
 */
class UserCtrl {

    private $tribeDao;
    private $speCtrl;
    private $userDao;
    private $runCtrl;

    function __construct() {
        $this->tribeDao = new TribeDAO();
        $this->userDao = new UserDAO();
    }

    private function getSpeCtrl() {
        if (is_null($this->speCtrl)) {
            $this->speCtrl = new SpecificationCtrl();
        }
        return $this->speCtrl;
    }

    private function getRunCtrl() {
        if (is_null($this->runCtrl)) {
            $this->runCtrl = new RunCtrl();
        }
        return $this->runCtrl;
    }

    /**
     * Removes specification from list of favorites of all users
     * @param int $id specification ID
     */
    public function deleteFavoriteSpecification($id) {
        $this->userDao->deleteFavoriteSpecification($id);
    }

    /**
     * Returns user's id based on his username
     * @param String $username username
     * @return int User's ID or -1 if user was not found
     */
    public function getUserIDbyUsername($username) {
        return $this->userDao->getUserIDbyUsername($username);
    }

    /**
     * Returns array of users with username matching %username%
     * @param string $username username
     * @return User[]
     */
    public function findMatchingUsers($username) {
        return $this->userDao->findMatchingUsers($username);
    }

    /**
     * Returns true if user is member of tribe
     * @param int $uid User id 
     * @param int $id Tribe id
     * @return boolean true if user is member of tribe
     */
    public function isMemberOfTribe($uid, $id) {
        return $this->userDao->isMemberOfTribe($uid, $id);
    }

    /**
     * Add given specification to user's favorite list
     * @param int $userId user ID
     * @param int $specificationId specification ID
     */
    public function addFavorite($userId, $specificationId) {
        $this->userDao->addFavorite($userId, $specificationId);
    }

    /**
     * Removes favorite specification for given user
     * @param int $userId user ID
     * @param int $specificationId specification ID
     */
    public function removeFavorite($userId, $specificationId) {
        $this->userDao->removeFavorite($userId, $specificationId);
    }

    /**
     * Returns username for user given by id
     * @param int $id user ID
     * @return string
     */
    public function getUsernameById($id) {
        return $this->userDao->getUsernameById($id);
    }

    /**
     * Returns list of users, results are paginated (first page is 1)
     * @param int $page page number
     * @return UsersResult 
     */
    public function findUsers($page) {
        return $this->userDao->findUsers($page);
    }

    /**
     * Removes all favorite specifications for given user
     * @param int $userId
     */
    public function deleteFavorites($userId) {
        $this->userDao->deleteFavorites($userId);
    }

    /**
     * Removes user from system - membership, favorites, authorship, user, assignments
     * @param string $username username
     * @return boolean true if successful
     */
    public function deleteUser($username) {
        $userId = $this->getUserIDbyUsername($username);
        $this->deleteFavorites($userId);
        $this->tribeDao->removeAllMemberships($userId);
        $this->tribeDao->removeLeader($userId);
        $this->getSpeCtrl()->deleteAuthorship($userId);
        $this->getSpeCtrl()->deleteOwnership($userId);
        $this->getRunCtrl()->deleteUsersAssignments($username);
        $this->userDao->deleteUser($userId, $username);
        return true;
    }

    /**
     * Updates user
     * @param string $firstName first name
     * @param string $lastName last name
     * @param string $role role
     * @param string $username username
     * @return boolean true if successful
     */
    public function editUser($firstName, $lastName, $role, $username, $oldUsername, $emaiNotications, $email, $password) {
        $existingUser = $this->getUser($username);
        if ($username !== $oldUsername && !is_null($existingUser)) {
            throw new UserException('username already exists', '', '');
        }
        $uid = $this->getUserIdByEmail($email);
        $uid2 = $this->getUserIDbyUsername($oldUsername);
        if ($uid > -1 && $uid !== $uid2) {
            throw new UserException("Wrong email", "email is already used");
        }
        if (!isset($role) || strlen($role) < 1) {
            $role = $existingUser->role;
        }

        return $this->userDao->editUser($firstName, $lastName, $role, $username, $oldUsername, $emaiNotications, $email, $password);
    }

    /**
     * Creates new user
     * @param string $firstName first name
     * @param string $lastName last name
     * @param string $role role
     * @param string $username username
     * @return boolean true if successful
     */
    public function createUser($firstName, $lastName, $role, $username, $email, $emailNotifications, $password) {
        $existingUser = $this->getUser($username);
        if (!is_null($existingUser)) {
            throw new UserException('username already exists', '', '');
        }

        $uid = $this->getUserIdByEmail($email);
        if ($uid > -1) {
            throw new UserException("Wrong email", "email is already used");
        }

        return $this->userDao->createUser($firstName, $lastName, $role, $username, $email, $emailNotifications, $password);
    }

    /**
     * Returns user
     * @param string $username username
     * @return User|null
     */
    public function getUser($username) {
        return $this->userDao->getUser($username);
    }

    /**
     * Returns user for given user ID
     * @param int $userId
     * @return User|null
     */
    public function getUserById($userId) {
        return $this->userDao->getUserById($userId);
    }

    /**
     * Returns list of all users inside single page result   
     * @return UsersResult 
     */
    public function getAllUsers() {
        return $this->userDao->getAllUsers();
    }

    /**
     * Edits username - only first and last name
     * @param type $firstName
     * @param type $lastName
     * @param type $username
     * @return boolean true if OK
     */
    public function editUserSimple($firstName, $lastName, $username, $emailNotifications, $email, $password) {
        $uid = $this->getUserIdByEmail($email);
        $uid2 = $this->getUserIDbyUsername($username);
        if ($uid > -1 && $uid !== $uid2) {
            throw new UserException("Wrong email", "email is already used");
        }
        return $this->userDao->editUserSimple($firstName, $lastName, $username, $emailNotifications, $email, $password);
    }

    /**
     * Imports users from given web service (endpoint)
     * @param String $url webservice URL to fetch data from
     * @return int number of imported users
     * @throws CurlRequestException
     */
    public function importUsers($url) {
        $data = $this->requestUrlForUsers($url);
        if ($data->headers['http_code'] !== 200) {
            throw new CurlRequestException("Curl request failed", "Response from URL was " . $data->headers['http_code'], "");
        }

        $users = json_decode($data->data);
        $createdUsers = 0;
        foreach ($users as $user) {
            if ($this->createImportedUser($user)) {
                $createdUsers++;
            }
        }
        return $createdUsers;
    }

    private function setRole($username, $role) {
        return $this->userDao->setRole($username, $role);
    }

    /**
     * Creates user from fetched data from web service. This actually only tries to 
     * set firstName and lastName as good as possible and then call createUser() method
     * @param type $user
     * @return boolean true on user creation
     */
    private function createImportedUser($user) {
        $u = new User($user->username);

        if (strlen($user->name) < 1 || strlen($user->username) < 1) {
            return false;
        }
        $existing = $this->getUser($u->username);
        if (!is_null($existing)) {
            $this->setRole($u->username, "tester");
            return false;
        }

        $_t = explode(" ", $user->name);
        $u->firstName = $_t[0];
        $namesCount = count($_t);
        switch ($namesCount) {
            case 1:
                $u->firstName = $_t[0];
                $u->lastName = $_t[0];
                break;
            case 2:
                $u->firstName = $_t[0];
                $u->lastName = $_t[1];
                break;
            default:
                $u->firstName = $_t[0];
                $u->lastName = "";
                for ($i = 0; $i < $namesCount - 1; $i++) {
                    $u->lastName = $u->lastName . " " . $_t[$i + 1];
                }
                break;
        }
        try {
            return $this->createUser($u->firstName, $u->lastName, "tester", $u->username);
        } catch (UserException $e) {
            // ignore 
            return false;
        }
    }

    /**
     * Makes curl request to specified url
     * @param String $url
     * @return CurlRequestResult
     */
    private function requestUrlForUsers($url) {
        $ch = curl_init();
        curl_setopt($ch, CURLOPT_HEADER, 0);
        curl_setopt($ch, CURLOPT_URL, $url);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, TRUE);
        curl_setopt($ch, CURLOPT_SSL_VERIFYPEER, false);
        curl_setopt($ch, CURLOPT_SSL_VERIFYHOST, false);
        curl_setopt($ch, CURLOPT_FOLLOWLOCATION, 1);
        if (Synergy::hasProxy()) {
            curl_setopt($ch, CURLOPT_PROXY, Synergy::getProxy());
        }
//        curl_setopt($ch, CURLOPT_PROXYPORT, 80);

        $data = curl_exec($ch);
        $result = new CurlRequestResult($data, curl_getinfo($ch));
        curl_close($ch);
        return $result;
    }

    /**
     * Returns max 5 users with role "manager"
     * @return User
     */
    public function getManagers() {
        return $this->userDao->getManagers();
    }

    public function saveProfileImg($data, $fileName, $userId) {

        $currentImg = $this->getProfileImg($userId);
        try {
            date_default_timezone_set('UTC');
            $timestamp = strtotime(date("Y-m-d H:i:s"));
            $fp = fopen(IMAGE_PATH . $timestamp . "_" . $fileName, 'w');
            fwrite($fp, $data);
            fclose($fp);
            if (strlen($currentImg) > 1) {
                $this->userDao->removeProfileImg($userId);
                unlink(IMAGE_PATH . $currentImg);
            }
            $this->userDao->addProfileImg($userId, $timestamp . "_" . $fileName);
            return IMAGE_BASE . $timestamp . "_" . $fileName;
        } catch (Exception $e) {
            $er = print_r($e, true);
            $logger = Synergy::getProvider("logger");
            $logger::log($er);
            return "";
        }
    }

    public function deleteProfileImg($userId) {
        $currentImg = $this->getProfileImg($userId);
        if (strlen($currentImg) > 1) {
            $this->userDao->removeProfileImg($userId);
            unlink(IMAGE_PATH . $currentImg);
        }
    }

    public function retireUsers($roleToRetire) {
        return $this->userDao->retireUsers($roleToRetire);
    }

    public function getProfileImg($userId) {
        return $this->userDao->getProfileImgPath($userId);
    }

    public function getUserIdByEmail($email) {
        return $this->userDao->getUserIdByEmail($email);
    }

    public function resetPassword($username) {
        $s = $this->userDao->getUser($username);
        if (is_null($s)) {
            throw new UserException('User not found', 'User not found', '');
        }
        if (is_null($s->email) || strlen($s->email) < 1) {
            throw new UserException('User does not have email address registered, please contact Synergy administrator', 'User does not have email address registered, please contact Synergy administrator', '');
        }

        $newPassword = $this->generateRandomString(10);
        if ($this->userDao->changePassword($username, $newPassword)) {
            $emailProvider = Synergy::getProvider("email");
            $b = "<div>Your password for Synergy account has been reset. Your new password is <strong>" . $newPassword . "</strong>. You can login <a href='http://netbeans-vm.apache.org/synergy/client/app/#/login'>here</a></div>";
            $emailProvider->send($emailProvider->compose($b, "New Synergy password", $s->email));
            return true;
        }
        return false;
    }

    private function generateRandomString($length) {
        $characters = '0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ';
        $charactersLength = strlen($characters);
        $randomString = '';
        for ($i = 0; $i < $length; $i++) {
            $randomString .= $characters[rand(0, $charactersLength - 1)];
        }
        return $randomString;
    }

}

?>
