<?php

namespace Synergy\Controller\Test;

use PDO;
use Synergy\Controller\CaseCtrl;
use Synergy\DB\DB_DAO;
use Synergy\DB\Test\FixtureTestCase;
use Synergy\Model\TestCase;

/**
 * Description of CaseCtrlTest
 *
 * @author vriha
 */
class CaseCtrlTest extends FixtureTestCase {

    protected $object;

    public function setUp() {
        $this->object = new CaseCtrl();
        parent::setUp();
    }

    public function testGetCase() {
        $case = $this->object->getCase(21, 11);
        $this->assertEquals('Elements completion', $case->title);
        $this->assertEquals('Items like datalist, dd, div etc. are offered', $case->result);
        $this->assertEquals(1, $case->order);
        $this->assertEquals(1, $case->duration);
        $this->assertNull($this->object->getCase(1212, 111));
    }

    public function testGetCasesByFilter() {
        $cases = $this->object->getCasesByFilter('sanity', 1);
        $this->assertEquals(1, count($cases->cases));
        $result = $this->object->getCasesByFilter('sanity2', 1);
        $this->assertEquals(0, count($result->cases));
        $result = $this->object->getCasesByFilter('sanity', 2);
        $this->assertEquals(0, count($result->cases));
        $this->assertEquals('New Project', $cases->cases[0]->title);
    }

    public function testUpdateCase() {

        DB_DAO::executeQuery("INSERT INTO suite_has_case (suite_id, case_id) VALUES (15,36)");
        $case = $this->object->getCase(36, 13);
        $case->title = "Updated case";
        $case->steps = "Updated case2";
        $case->result = "Updated case3";

        $this->object->updateCase($case, 1);

        $case = $this->object->getCase(36, 13);
        $this->assertEquals("Updated case", $case->title);
        $this->assertEquals("Updated case2", $case->steps);
        $this->assertEquals("Updated case3", $case->result);
        $this->assertEquals(1, $case->duration);

        $case->duration = 5;
        $this->object->updateCase($case, 1);
        $case = $this->object->getCase(36, 13);
        $this->assertEquals(5, $case->duration);

        $this->object->addIssue(111111, 36);
        $this->object->addLabel('sanity', 36);
        $this->object->saveImage(36, "12_image.png", "simple schema");
        $case->title = "Updated Clonned case";
        $case->suiteId = 13;

        $this->object->updateCase($case, 0);

        DB_DAO::executeQuery("UPDATE `case` SET id=333 WHERE title='Updated Clonned case';");
        DB_DAO::executeQuery("UPDATE `suite_has_case` SET case_id=333 WHERE case_id=0;");

        $this->assertNull($this->object->getCase(36, 13));

        $originalCase = $this->object->getCase(36, -1);
        $clonnedCase = $this->object->getCase(333, 13);
        
        $this->assertEquals('Updated case', $originalCase->title);
        $this->assertEquals('Updated Clonned case', $clonnedCase->title);
        $this->assertEquals(2, count($this->object->findMatchingCases('Updated')));
    }

    public function testAddRemoveIssue() {
        $this->object->addIssue(111111, 16);
        $this->object->addIssue(87, 17);
        $case1 = $this->object->getCase(16, -1);
        $this->assertEquals(2, count($case1->issues));
        $case2 = $this->object->getCase(17, -1);
        $this->assertEquals(1, count($case2->issues));
        $this->assertEquals(87, $case2->issues[0]->bugId);

        $this->object->removeIssue(87, 17);
        $case1 = $this->object->getCase(17, -1);
        $this->assertEquals(0, count($case1->issues));
    }

    public function testAddRemoveLabel() {
        $this->object->addLabel('sanity', 18);
        $case1 = $this->object->getCase(18, -1);
        $this->assertEquals(1, count($case1->keywords));
        $this->assertEquals('sanity', $case1->keywords[0]);

        $this->object->removelabel('sanity', 18);
        $case1 = $this->object->getCase(18, -1);
        $this->assertEquals(0, count($case1->keywords));
    }

    public function testCreateGetKeyword() {
        $idSanity = $this->object->createGetKeyword('sanity');
        $this->object->createGetKeyword('foo');
        DB_DAO::executeQuery("UPDATE `keyword` SET id=21 WHERE `keyword`='foo';");
        $id = $this->object->createGetKeyword('foo');
        $this->assertEquals(1, $idSanity);
        $this->assertEquals(21, $id);
    }

    public function testCreateCase() {
        $testCase = new TestCase("Unit test case 11", 5, -1, 2);
        $testCase->steps = "steps";
        $testCase->result = "result";
        $testCase->suiteId = 15;
        $this->object->createCase($testCase);
        DB_DAO::connectDatabase();
        DB_DAO::executeQuery("UPDATE `case` SET id=999 WHERE title='Unit test case 11';");
        DB_DAO::executeQuery("UPDATE `suite_has_case` SET case_id=999 WHERE case_id=0;");

        $handler = DB_DAO::getDB()->prepare("SELECT id FROM `case` WHERE title='Unit test case 11'");
        $handler->execute();
        $newId = -1;
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $newId = $row['id'];
        }

        $testCaseCreated = $this->object->getCase($newId, 15);

        $this->assertEquals($testCase->title, $testCaseCreated->title);
        $this->assertEquals($testCase->result, $testCaseCreated->result);
        $this->assertEquals($testCase->steps, $testCaseCreated->steps);
        $this->assertEquals($testCase->order, $testCaseCreated->order);

        $this->object->removeCaseFromSuite(15, $newId);
        $this->assertNull($this->object->getCase($newId, 15));

        $this->object->saveImage($newId, "12_image.png", "simple schema");
        DB_DAO::executeQuery("UPDATE `case_image` SET id=999 WHERE case_id=" . $newId . ";");
        $this->object->addIssue(111111, $newId);
        $this->object->addLabel('sanity', $newId);
        $case1 = $this->object->getCase($newId, -1);

        // TODO removal
        $this->assertEquals(1, count($case1->images));
        $this->assertEquals(1, count($case1->keywords));
        $this->assertEquals(1, count($case1->issues));

        $this->object->removeCase($newId);

        $this->assertEquals(0, count($this->object->getIssuesForCase($newId)));
        $this->assertEquals(0, count($this->object->getLabelsForCase($newId)));
        $this->assertEquals(0, count($this->object->getImagesForCase($newId)));

        $this->assertNull($this->object->getCase($newId, 15));
        $this->assertNull($this->object->getCase($newId, -1));
    }

    public function testFindMatchingCases() {
        $matchingCases = $this->object->findMatchingCases('Cor');
        $this->assertEquals(2, count($matchingCases));
        $this->assertEquals('Cordova plugins', $matchingCases[0]->title);
        $this->assertEquals('Cordova plugins II', $matchingCases[1]->title);
    }

    public function testGetCasesDuration() {
        $data = $this->object->getCasesDuration("(id=15 OR id=16)");
        $this->assertEquals(2, count($data));
        $this->assertEquals(1, $data[0]->duration);
        $this->assertEquals(1, $data[1]->duration);
    }

    public function testUpdateDuration() {
        $this->object->updateDuration(30, 3);
        $caseUpdated = $this->object->getCase(30, -1);
        $this->assertEquals(2, $caseUpdated->duration);
        $data = $this->object->getCasesDuration("(id=15 OR id=30)");
        $this->assertEquals(3, $data[0]->duration + $data[1]->duration);
    }

    public function testSaveDeleteImage() {
        $this->object->saveImage(21, "12_image.png", "simple schema");
        $caseUpdated = $this->object->getCase(21, -1);
        $this->assertEquals(1, count($caseUpdated->images));
        $this->assertEquals(IMAGE_PATH . "12_image.png", $caseUpdated->images[0]->getPath());
        $this->assertEquals("simple schema", $caseUpdated->images[0]->title);
        DB_DAO::executeQuery("UPDATE case_image SET id=14 WHERE title='simple schema'");
        $this->object->deleteImage(14);
        $caseUpdated = $this->object->getCase(21, -1);
        $this->assertEquals(0, count($caseUpdated->images));
    }

    public function testGetTestCasesDetailed() {
        $this->object->addIssue(12, 21);
        $this->object->addLabel('sanity', 21);
        $this->object->saveImage(21, "12_image.png", "simple schema");
        $case = $this->object->getCase(21, 11);
        $this->assertEquals('Elements completion', $case->title);
        $this->assertEquals('Items like datalist, dd, div etc. are offered', $case->result);
        $this->assertEquals(1, $case->order);
        $this->assertEquals(1, $case->duration);
        $this->assertEquals(1, count($case->keywords));
        $this->assertEquals(1, count($case->images));
    }

    public function testRemoveUnusedCases() {
        $testCase = new TestCase("Unit test case 11", 5, -1, 2);
        $testCase->steps = "steps";
        $testCase->result = "result";
        $testCase->suiteId = 15;
        $this->object->createCase($testCase);
        DB_DAO::connectDatabase();
        DB_DAO::executeQuery("UPDATE `case` SET id=999 WHERE title='Unit test case 11';");
        DB_DAO::executeQuery("UPDATE `suite_has_case` SET case_id=999 WHERE case_id=0;");

        $handler = DB_DAO::getDB()->prepare("SELECT id FROM `case` WHERE title='Unit test case 11'");
        $handler->execute();
        $newId = -1;
        while ($row = $handler->fetch(PDO::FETCH_ASSOC)) {
            $newId = $row['id'];
        }

        $this->object->removeCaseFromSuite(15, $newId);
        $this->object->removeUnusedCases();
        $this->assertNull($this->object->getCase($newId, -1));
    }

}
