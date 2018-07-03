<?php

namespace Synergy\Controller\Test;

use Synergy\Controller\SpecificationCtrl;
use Synergy\Controller\VersionCtrl;
use Synergy\DB\DB_DAO;
use Synergy\DB\Test\FixtureTestCase;

/**
 * Description of VersionCtrlTest
 *
 * @author vriha
 */
class VersionCtrlTest extends FixtureTestCase {

    /**
     * @var VersionCtrl
     */
    protected $object;

    public function setUp() {
        $this->object = new VersionCtrl();
        parent::setUp();
    }

    public function testGetVersionByName() {
        $latest = $this->object->getVersionByName();
        $v60 = $this->object->getVersionByName("6.0");

        $this->assertEquals(4, $latest->id);
        $this->assertEquals('8.0', $latest->name);
        $this->assertEquals(5, $v60->id);
        $this->assertEquals('6.0', $v60->name);
    }

    public function testGetAllVersions() {
        $versions = $this->object->getAllVersions();
        $this->assertEquals(4, count($versions));
        $this->assertEquals('8.0', $versions[0]->name);
        $this->assertEquals('6.0', $versions[2]->name);
    }

  
    public function tetsUpdateVersion() {
        $fail = $this->object->updateVersion(2, "7.4", 0);
        $this->assertFalse($fail);
        
        $ok = $this->object->updateVersion(2, "9", 1);
        $this->assertTrue($ok);
        
        $v = $this->object->getVersionByName("9");
        $this->assertEquals("9", $v->name);
        $this->assertEquals(1, $v->isObsolete);
        
    }

    public function testDeleteVersion() {
        $this->object->deleteVersion(3);
        $specCtrl = new SpecificationCtrl();
        $specs = $specCtrl->getSpecifications(3);
        $this->assertEquals(0, count($specs));
        $specsKept = $specCtrl->getSpecifications(2);
        $this->assertEquals(1, count($specsKept));
    }

    public function testCreateVersion() {
        $this->assertFalse($this->object->createVersion('8.0'));
        $this->object->createVersion('5.0');
        DB_DAO::executeQuery("UPDATE `version` SET id=999 WHERE id IS NULL");
        $v = $this->object->getVersionByName('5.0');
        $this->assertEquals("5.0", $v->name);
        $this->assertEquals(0, $v->isObsolete);
    }


    public function testGetVersions() {
        $versions = $this->object->getAllVersions();
        $this->assertEquals(4, count($versions));
        $this->assertEquals('8.0', $versions[0]->name);
    }

}
