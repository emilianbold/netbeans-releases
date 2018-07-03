<?php

namespace Synergy\Controller\Test;

use Synergy\Controller\CaseCtrl;
use Synergy\Controller\SpecificationCtrl;
use Synergy\Controller\SuiteCtrl;
use Synergy\DB\DB_DAO;
use Synergy\DB\Test\FixtureTestCase;
use Synergy\Model\Suite;

/**
 * Description of SuiteCtrlTest
 *
 * @author vriha
 */
class SuiteCtrlTest extends FixtureTestCase {

    /**
     * @var SuiteCtrl
     */
    protected $object;

    public function setUp() {
        $this->object = new SuiteCtrl();
        parent::setUp();
    }

    public function testDeleteSuitesForSpecification() {
        $specCtrl = new SpecificationCtrl();
        $specCtrl->deleteSpecification(5);
        $caseCtrl = new CaseCtrl();
        $this->assertNull($this->object->getSuite(11));
        $this->assertNull($caseCtrl->getCase(20, 11));
    }

    public function testGetSuite() {
        $suite = $this->object->getSuite(14);
        $this->assertEquals('Refactoring', $suite->title);
        $this->assertEquals(3, $suite->estimation);
        $this->assertEquals(3, count($suite->testCases));

        $this->assertEquals('Mixins', $suite->testCases[0]->title);
        $this->assertEquals('Variables', $suite->testCases[1]->title);
        $this->assertEquals('Parameters', $suite->testCases[2]->title);
    }

    public function testCloneSpecificationSuite() {
        $oldSuite = $this->object->getSuite(14);
        $specCtrl = new SpecificationCtrl();
        $oldArray = $specCtrl->getSuitesIDs(4);
        $this->object->cloneSpecificationSuite(4, $oldSuite);
        
        DB_DAO::executeQuery("UPDATE suite SET id=999 WHERE id IS NULL;");
        $suite = $this->object->getSuite(999);

        $newArray = $specCtrl->getSuitesIDs(4);
        
        $this->assertEquals($oldSuite->title, $suite->title);
        $this->assertEquals($oldSuite->desc, $suite->desc);
        $this->assertEquals($oldSuite->product, $suite->product);
        $this->assertEquals($oldSuite->component, $suite->component);
        $this->assertEquals($oldSuite->order, $suite->order);
        $this->assertEquals(count($oldArray)+1, count($newArray));
    }

    public function testGetTestCasesIds() {
        $this->assertEquals(3, count($this->object->getTestCasesIds(14)));
    }

    public function testUpdateSuite() {
        $suite = new Suite(14, 'Desc_', 'Title_', 'web', 'test', 5, 10);
        $this->object->updateSuite($suite);
        $suite = $this->object->getSuite(14);
        $this->assertEquals('Title_', $suite->title);
        $this->assertEquals('Desc_', $suite->desc);
        $this->assertEquals('web', $suite->product);
        $this->assertEquals('test', $suite->component);
        $this->assertEquals(10, $suite->order);
        $this->assertEquals(3, $suite->estimation);
    }

    public function testGetSpecificationId() {
        $this->assertEquals(5, $this->object->getSpecificationId(14));
    }

    public function testDeleteSuite() {
        $this->object->deleteSuite(14);
        $this->assertNull($this->object->getSuite(14));
        $this->assertEquals(0, count($this->object->getTestCasesIds(14)));
    }

    public function testCreateSuite() {
        $suite = new Suite(0, 'Desc_', 'Title_', 'web', 'test', 5, 10);
        $this->object->createSuite($suite);
        DB_DAO::executeQuery("UPDATE suite SET id=999 WHERE id IS NULL;");
        $suite = $this->object->getSuite(999);
        $this->assertEquals('Title_', $suite->title);
        $this->assertEquals('Desc_', $suite->desc);
        $this->assertEquals('web', $suite->product);
        $this->assertEquals('test', $suite->component);
        $this->assertEquals(10, $suite->order);
        $this->assertEquals(0, $suite->estimation);
    }

    public function testAddCaseToSuite() {
        $this->object->addCaseToSuite(17, 14);
        $suite = $this->object->getSuite(14);
        $this->assertEquals(4, count($suite->testCases));
        $newCase = false;
        foreach ($suite->testCases as $c) {
            if ($c->title === "Cordova plugins") {
                $newCase = true;
                break;
            }
        }
        $this->assertTrue($newCase);
    }

    public function testSuiteAlreadyHasCase() {
        $this->assertTrue($this->object->suiteAlreadyHasCase(37, 14));
        $this->assertFalse($this->object->suiteAlreadyHasCase(17, 14));
    }

    public function testOnlySuiteHasCase() {
        $this->assertTrue($this->object->onlySuiteHasCase(37, 14));
        $this->object->addCaseToSuite(37, 8);
        $this->assertFalse($this->object->onlySuiteHasCase(37, 14));
    }

    public function testGetAllSpecificationsForCase() {
        $this->assertEquals(1, count($this->object->getAllSpecificationsForCase(37)));
        $this->object->addCaseToSuite(37, 8);
        $this->assertEquals(2, count($this->object->getAllSpecificationsForCase(37)));
    }

}
