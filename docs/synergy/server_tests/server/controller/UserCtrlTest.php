<?php

namespace Synergy\Controller\Test;

use Synergy\Controller\RunCtrl;
use Synergy\Controller\SpecificationCtrl;
use Synergy\Controller\TribeCtrl;
use Synergy\Controller\UserCtrl;
use Synergy\DB\DB_DAO;
use Synergy\DB\Test\FixtureTestCase;
use Synergy\Model\User;
use Synergy\Model\UserException;
use Synergy\Providers\SessionCtrl_Production;

/**
 * Description of UserCtrlTest
 *
 * @author vriha
 */
class UserCtrlTest extends FixtureTestCase {

    /**
     * @var UserCtrl
     */
    protected $object;

    public function setUp() {
        $this->object = new UserCtrl();
        parent::setUp();
    }

    public function testDeleteFavoriteSpecification() {
        $this->object->addFavorite(7, 4);
        $this->object->deleteFavoriteSpecification(4);
        $specCtrl = new SpecificationCtrl();

        $newFavs = $specCtrl->getFavoriteSpecifications("import");
        $this->assertEquals(0, count($newFavs[0]));

        $newFavs = $specCtrl->getFavoriteSpecifications("jack");
        $this->assertEquals(1, count($newFavs));
    }

    public function testGetUserIDbyUsername() {
        $this->assertEquals(7, $this->object->getUserIDbyUsername("jack"));
        $this->assertEquals(-1, $this->object->getUserIDbyUsername("foo"));
    }

    public function testFindMatchingUsers() {
        $matchA = $this->object->findMatchingUsers("ja");
        $this->assertEquals(1, count($matchA));
        $this->assertEquals('jack', $matchA[0]->username);
        $matchB = $this->object->findMatchingUsers("de");
        $this->assertEquals(0, count($matchB));
    }

    public function testIsMemberOfTribe() {
        $this->assertFalse($this->object->isMemberOfTribe(7, 1));
        $this->assertTrue($this->object->isMemberOfTribe(6, 1));
        $this->assertFalse($this->object->isMemberOfTribe(6, 2));
    }

    public function testAddFavorite() {
        $this->object->addFavorite(7, 4);
        $specCtrl = new SpecificationCtrl();
        $newFavs = $specCtrl->getFavoriteSpecifications("jack");
        $this->assertEquals(4, $newFavs[0]->id);
        $this->assertEquals(5, $newFavs[1]->id);
    }

    public function testRemoveFavorite() {
        $this->object->removeFavorite(7, 5);
        $specCtrl = new SpecificationCtrl();
        $favs = $specCtrl->getFavoriteSpecifications("jack");
        $this->assertEquals(0, count($favs));
    }

    public function testGetUsernameById() {
        $this->assertEquals('jack', $this->object->getUsernameById(7));
        $this->assertEquals('', $this->object->getUsernameById(20));
    }

    public function testDeleteFavorites() {
        $this->object->addFavorite(7, 4);
        $this->object->deleteFavorites(7);
        $specCtrl = new SpecificationCtrl();
        $favs = $specCtrl->getFavoriteSpecifications("jack");
        $this->assertEquals(0, count($favs));
    }

    public function testDeleteUser() {
        SessionCtrl_Production::$userId = 7;
        $this->object->deleteUser("import");
        $specCtrl = new SpecificationCtrl();
        $tribeCtrl = new TribeCtrl();
        $runCtrl = new RunCtrl();
        $newFavs = $specCtrl->getFavoriteSpecifications("import");
        $this->assertEquals(0, count($newFavs));
        $this->assertEquals(0, count($tribeCtrl->getUserMembership("import")));
        $this->assertEquals(0, count($runCtrl->getUsersAssignments("import")));
        $this->assertNull($runCtrl->getAssigmentProgress(1));
        $this->assertEquals(0, count($specCtrl->getSpecificationsByAuthor("import")));
        $this->assertEquals(0, count($specCtrl->getSpecificationsByOwner("import")));
        $this->assertNull($this->object->getUser("import"));
    }

    public function testEditUser() {
        $user = new User("test");
        $user->firstName = "John";
        $user->lastName = "Doe";
        $user->role = "admin";
        $this->object->editUser($user->firstName, $user->lastName, $user->role, $user->username, "jack");
        $u = $this->object->getUser($user->username);
        $this->assertEquals($user->firstName, $u->firstName);
        $this->assertEquals($user->lastName, $u->lastName);
        $this->assertEquals($user->role, $u->role);
        $this->assertEquals($user->username, $u->username);

        $exc = false;
        try {
            $this->object->editUser($user->firstName, $user->lastName, $user->role, "import", $user->username);
        } catch (UserException $ex) {
            $exc = true;
        }

        $this->assertTrue($exc);
    }

    public function testCreateUser() {
        $user = new User("test");
        $user->firstName = "John";
        $user->lastName = "Doe";
        $user->role = "admin";
        $exc = false;
        try {
            $this->object->createUser($user->firstName, $user->lastName, $user->role, "import");
        } catch (UserException $ex) {
            $exc = true;
        }
        $this->assertTrue($exc);
        $this->object->createUser($user->firstName, $user->lastName, $user->role, $user->username);
        DB_DAO::executeQuery("UPDATE `user` SET id=999 WHERE id IS NULL");

        $u = $this->object->getUserById(999);
        $this->assertEquals($user->firstName, $u->firstName);
        $this->assertEquals($user->lastName, $u->lastName);
        $this->assertEquals($user->role, $u->role);
        $this->assertEquals($user->username, $u->username);
    }

    public function testGetUser() {
        $user = $this->object->getUser("jack");
        $this->assertEquals('jack', $user->username);
        $this->assertEquals('jack', $user->firstName);
        $this->assertEquals('jack', $user->lastName);
        $this->assertEquals('tester', $user->role);
        $this->assertEquals(7, $user->id);
        $this->assertNull($this->object->getUser("test"));
    }

    public function testGetUserById() {
        $user = $this->object->getUserById(7);
        $this->assertEquals('jack', $user->username);
        $this->assertEquals('jack', $user->firstName);
        $this->assertEquals('jack', $user->lastName);
        $this->assertEquals('tester', $user->role);
        $this->assertNull($this->object->getUserById(25));
    }

    public function testGetAllUsers() {
        $result = $this->object->getAllUsers();
        $this->assertEquals("import", $result->users[0]->username);
        $this->assertEquals("import", $result->users[0]->firstName);
        $this->assertEquals("import", $result->users[0]->lastName);
        $this->assertEquals("tester", $result->users[0]->role);
        $this->assertEquals("jack", $result->users[1]->username);
        $this->assertEquals(2, count($result->users));
    }

    public function testEditUserSimple() {
        $this->object->editUserSimple("jimmy", "doe", "import", true);
        $user = $this->object->getUser("import");
        $this->assertEquals("jimmy", $user->firstName);
        $this->assertEquals("doe", $user->lastName);
    }

}
