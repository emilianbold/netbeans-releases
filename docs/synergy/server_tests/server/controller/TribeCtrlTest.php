<?php

namespace Synergy\Controller\Test;

use Synergy\Controller\TribeCtrl;
use Synergy\DB\DB_DAO;
use Synergy\DB\Test\FixtureTestCase;
use Synergy\Model\Tribe;

/**
 * Description of TribeCtrlTest
 *
 * @author vriha
 */
class TribeCtrlTest extends FixtureTestCase {

    /**
     * @var TribeCtrl
     */
    protected $object;

    public function setUp() {
        $this->object = new TribeCtrl();
        parent::setUp();
    }

    public function testGetUserMembership() {
        $this->assertEquals(2, count($this->object->getUserMembership("import")));
        $this->assertEquals(1, count($this->object->getUserMembership("jack")));
    }

    public function testGetTribe() {
        $tribe = $this->object->getTribe(1);
        $this->assertEquals('Web', $tribe->name);
        $this->assertEquals('Desc.', $tribe->description);
        $this->assertEquals(6, $tribe->leader_id);
    }

    public function testGetMembers() {
        $members = $this->object->getMembers(1);
        $this->assertEquals(1, count($members));
        $this->assertEquals('import', $members[0]->username);
        $this->assertEquals(0, count($this->object->getMembers(2)));
    }

    public function testRemoveMember() {
        $this->object->removeMember('import', 1);
        $this->assertEquals(0, count($this->object->getMembers(1)));
    }

    public function testGetLeader() {
        $leader1 = $this->object->getLeader(1);
        $this->assertEquals('import', $leader1->username);
        $leader2 = $this->object->getLeader(2);
        $this->assertEquals('jack', $leader2->username);
    }

    public function testEditTribe() {
        $tribe = new Tribe(1, 'NewWeb', 'NewDesc.', 6);
        $tribe->leaderUsername = "foo";
        $this->assertFalse($this->object->editTribe($tribe));
        $tribe->leaderUsername = "import";
        $this->object->editTribe($tribe);
        $tribeNew = $this->object->getTribe(1);
        $this->assertEquals($tribe->name, $tribeNew->name);
        $this->assertEquals($tribe->description, $tribeNew->description);
        $this->assertEquals($tribe->leader_id, $tribeNew->leader_id);
    }

    public function testAddMember() {
        $result = $this->object->addMember("foo", 1);
        $this->assertFalse($result);
        $this->assertEquals(1, count($this->object->getMembers(1)));
        $result = $this->object->addMember("jack", 1);
        $this->assertTrue($result);
        $this->assertEquals(2, count($this->object->getMembers(1)));
    }

    public function testGetTribes() {
        $tribes = $this->object->getTribes();
        $this->assertEquals(2, count($tribes));
        foreach ($tribes as $tribe) {
            $this->assertTrue(strlen($tribe->leaderUsername) > 0);
        }
    }

    public function testRemoveTribe() {
        $this->object->removeTribe(1);
        $this->assertNull($this->object->getTribe(1));
        $this->assertEquals(0, count($this->object->getMembers(1)));
    }

    public function testCreateTribe() {
        $tribe = new Tribe(-1, "TestTribe", "d", -1);
        $tribe->leaderUsername = "foo";
        $this->assertEquals(0, $this->object->createTribe($tribe));
        $tribe->leaderUsername = "import";
        $this->object->createTribe($tribe);
        DB_DAO::executeQuery("UPDATE tribe SET id=999 WHERE id IS NULL");
        $tribeNew = $this->object->getTribe(999);
        $this->assertEquals("TestTribe", $tribeNew->name);
        $this->assertEquals("d", $tribeNew->description);
        $this->assertEquals(6, $tribeNew->leader_id);
    }

}
