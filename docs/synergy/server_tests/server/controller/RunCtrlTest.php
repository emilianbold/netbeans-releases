<?php

namespace Synergy\Controller\Test;

use Synergy\Controller\RunCtrl;
use Synergy\DB\DB_DAO;
use Synergy\DB\Test\FixtureTestCase;
use Synergy\Model\Exception\AssignmentConflictException;
use Synergy\Model\TestAssignment;
use Synergy\Model\TestRun;

/**
 * Description of RunCtrlTest
 *
 * @author vriha
 */
class RunCtrlTest extends FixtureTestCase {

    /**
     * @var RunCtrl
     */
    protected $object;

    public function setUp() {
        $this->object = new RunCtrl();
        parent::setUp();
    }

    public function testGetRunPlain() {
        $run = $this->object->getRun(1, false);
        $this->assertEquals('Sample test run', $run->title);
        $this->assertEquals('Desc', $run->desc);
        $this->assertEquals('2013-11-01 00:00:00', $run->start);
        $this->assertEquals('2013-11-28 00:00:00', $run->end);
        $this->assertEquals(1, count($run->attachments));
        $this->assertEquals(1, $run->attachments[0]->id);
        $this->assertEquals('/empty', $run->attachments[0]->getPath());
        $this->assertEquals(3, count($run->assignments));
        $this->assertEquals(3, $run->assignments[2]->id);
        $this->assertEquals('warning', $run->assignments[2]->info);
        $this->assertEquals(14, $run->assignments[2]->passed);
        $this->assertEquals(4, $run->assignments[2]->failed);
        $this->assertEquals(2, $run->assignments[2]->skipped);
        $this->assertEquals(1, $run->assignments[0]->id);
        $this->assertEquals('pending', $run->assignments[0]->info);
        $this->assertEquals('pending', $run->assignments[1]->info);
        $this->assertEquals(2, $run->assignments[1]->id);
        $this->assertEquals(20, $run->completed);
        $this->assertEquals(60, $run->total);
    }

    public function testGetRunUserCentric() {
        $run = $this->object->getRun(1, true);
        $this->assertEquals('Sample test run', $run->title);
        $this->assertEquals('Desc', $run->desc);
        $this->assertEquals('2013-11-01 00:00:00', $run->start);
        $this->assertEquals('2013-11-28 00:00:00', $run->end);
        $this->assertEquals(1, count($run->attachments));
        $this->assertEquals(1, $run->attachments[0]->id);
        $this->assertEquals('/empty', $run->attachments[0]->getPath());
        $this->assertEquals(2, count($run->assignments));
        $this->assertEquals(2, count($run->assignments["import_5_1"]->assignments));
        $this->assertEquals(0, $run->assignments["import_5_1"]->assignments[0]->completed);
        $this->assertEquals(1, count($run->assignments["import_4_-1"]->assignments));
        $this->assertEquals(20, $run->completed);
        $this->assertEquals(60, $run->total);
    }

    public function testRestartAssignment() {
        $this->object->restartAssignment(1);
        $restarted = $this->object->getRun(1, false)->assignments[0];
        $this->assertNull($this->object->getAssigmentProgress(1));
        $this->assertEquals(0, $restarted->completed);
        $this->assertEquals(0, $restarted->failed);
        $this->assertEquals(0, $restarted->skipped);
        $this->assertEquals(0, $restarted->passed);
    }

    public function testGetAssignment() {
        $assignment = $this->object->getAssignment(1);
        $this->assertEquals("Sample test run", $assignment->testRunTitle);
        $this->assertEquals("LESS support Test Specification for test 7.4", $assignment->specificationData->title);
        $this->assertEquals("LESS support Test Specification for test 7.4", $assignment->specification);
        $this->assertEquals(5, $assignment->specificationId);
        $this->assertEquals("pending", $assignment->info);
        $this->assertEquals("Windows", $assignment->platform);
        $this->assertEquals(0, $assignment->completed);
        $this->assertEquals("sanity", $assignment->label);
    }

    public function testDeleteAssignmentsForSpecification() {
        $this->object->deleteAssignmentsForSpecification(5);
        $this->assertNull($this->object->getAssignment(2));
        $this->assertNull($this->object->getAssignment(1));
        $this->assertNull($this->object->getAssigmentProgress(2));
        $this->assertNull($this->object->getAssigmentProgress(1));
        $conflict = false;
        try {
            $this->assertNotNull($this->object->getAssignment(3));
        } catch (AssignmentConflictException $e) {
            $conflict = true;
        }
        $this->assertTrue($conflict);
    }

    public function testDeleteRun() {
        $this->object->deleteRun(1);
        $this->assertNull($this->object->getRun(1, false));
        $this->assertNull($this->object->getAssignment(1));
        $this->assertNull($this->object->getAssigmentProgress(1));
        $this->assertEquals(0, count($this->object->getAttachments(1)));
    }

    public function testDeleteAssignmentsForTestRun() {
        $this->object->createAssignment(5, 1, 1, 2, "jack", -1, TestAssignment::CREATED_BY_TESTER);
        DB_DAO::executeQuery("UPDATE test_assignement SET id=999 WHERE id IS NULL");
        $this->object->deleteAssignmentsForTestRun(1);
        $run = $this->object->getAssignments(1);
        $run2 = $this->object->getAssignments(2);
        $this->assertEquals(0, count($run));
        $this->assertEquals(1, count($run2));
    }

    public function testDeleteAssignment() {
        $this->object->deleteAssignment(1);
        $this->assertNull($this->object->getAssignment(1));
        $this->assertNotNull($this->object->getAssignment(2));
    }

    public function testGetRuns() {
        $runs = $this->object->getRuns(1);
        $this->assertEquals(1, count($runs));
        $this->assertEquals('Sample test run', $runs->testRuns[1]->title);
        $this->assertEquals(60, $runs->testRuns[1]->total);
        $this->assertEquals(20, $runs->testRuns[1]->completed);
        $this->assertEquals(3, $runs->testRuns[1]->membersCount);
        $runs2 = $this->object->getRuns(2);
        $this->assertEquals(0, count($runs2->testRuns));
    }

    public function testCreateRun() {
        date_default_timezone_set('UTC');
        $today = date("Y-m-d H:i:s");
        $end = date("Y-m-d H:i:s", strtotime(date("Y-m-d H:i:s") . " + 1 day"));
        $testRun = new TestRun("MyRun", -1, $today, $end);
        $testRun->desc = "desc";
        $this->object->createRun($testRun->title, $testRun->desc, $testRun->start, $testRun->end, -1);
        DB_DAO::executeQuery("UPDATE test_run SET id=999 WHERE id IS NULL");
        $newRun = $this->object->getRun(999, false);
        $this->assertEquals($testRun->title, $newRun->title);
        $this->assertEquals($testRun->desc, $newRun->desc);
        $this->assertEquals($testRun->start, $newRun->start);
        $this->assertEquals($testRun->end, $newRun->end);
    }

    public function testGetRunOverview() {
        $overview = $this->object->getRunOverview(1);
        $this->assertEquals("Sample test run", $overview->title);
        $this->assertEquals("Desc", $overview->desc);
        $this->assertEquals("2013-11-01 00:00:00", $overview->start);
        $this->assertEquals("2013-11-28 00:00:00", $overview->end);
        $this->assertEquals(1, count($overview->attachments));
    }

    public function testEditRun() {
        $this->object->editRun(1, "New Title", "Desc2", "2013-10-01 00:00:00", "2013-12-01 00:00:00", -1);
        $updatedRun = $this->object->getRun(1, false);
        $this->assertEquals("New Title", $updatedRun->title);
        $this->assertEquals("Desc2", $updatedRun->desc);
        $this->assertEquals("2013-10-01 00:00:00", $updatedRun->start);
        $this->assertEquals("2013-12-01 00:00:00", $updatedRun->end);
    }

    public function testCreateAssignment() {
        $this->object->createAssignment(4, 1, 1, 2, "jack", -1, TestAssignment::CREATED_BY_TESTER);
        DB_DAO::executeQuery("UPDATE test_assignement SET id=999 WHERE id IS NULL");

        $newAssignment = $this->object->getAssignment(999);
        $this->assertEquals("Sample test run2", $newAssignment->testRunTitle);
        $this->assertEquals("Cordova support Test Specification for test 7.4", $newAssignment->specificationData->title);
        $this->assertEquals("Cordova support Test Specification for test 7.4", $newAssignment->specification);
        $this->assertEquals(4, $newAssignment->specificationId);
        $this->assertEquals("pending", $newAssignment->info);
        $this->assertEquals("Windows", $newAssignment->platform);
        $this->assertEquals(0, $newAssignment->completed);
        $this->assertEquals(1, $newAssignment->total);
        $this->assertEquals("sanity", $newAssignment->label);

        $this->object->createAssignment(4, 1, -1, 2, "jack", -1, TestAssignment::CREATED_BY_TESTER);
        DB_DAO::executeQuery("UPDATE test_assignement SET id=333 WHERE id IS NULL");
        $newAssignment = $this->object->getAssignment(333);
        $this->assertEquals("Sample test run2", $newAssignment->testRunTitle);
        $this->assertEquals("Cordova support Test Specification for test 7.4", $newAssignment->specificationData->title);
        $this->assertEquals("Cordova support Test Specification for test 7.4", $newAssignment->specification);
        $this->assertEquals(4, $newAssignment->specificationId);
        $this->assertEquals("pending", $newAssignment->info);
        $this->assertEquals(0, $newAssignment->completed);
        $this->assertEquals(5, $newAssignment->total);
        $this->assertEquals("", $newAssignment->label);
    }

    public function testCreateAssignmentTribe() {
        $before = $this->object->getAssignments(2);
        $this->object->createAssignment(8, 1, 1, 2, "", 1, TestAssignment::CREATED_BY_TESTER);
        $after = $this->object->getAssignments(2);
        $this->assertEquals(1 + count($before), count($after));

        $before = $this->object->getAssignments(2);
        $this->object->createAssignment(8, 1, 1, 2, "", 2, TestAssignment::CREATED_BY_TESTER);
        $after = $this->object->getAssignments(2);
        $this->assertEquals(count($before), count($after));
    }

    public function testCheckUserIsAssigned() {
        $this->assertFalse($this->object->checkUserIsAssigned(1, "jack"));
        $this->assertTrue($this->object->checkUserIsAssigned(1, "import"));
    }

    public function updateAssignment($id, $numberOfCases, $numberOfCasesCompleted, $failedCases, $skippedCases, $passedCases) {
        
    }

    public function testDeleteAssignmentProgress() {
        $this->object->deleteAssignmentProgress(1);
        $this->assertNull($this->object->getAssigmentProgress(1));
    }

    public function testGetRunTitleForAssignment() {
        $this->assertEquals("Sample test run", $this->object->getRunTitleForAssignment(1));
        $this->assertEquals("", $this->object->getRunTitleForAssignment(4));
    }

    public function getUsersAssignments($username) {
        
    }

    public function getAllUsersAssignments($username) {
        
    }

    public function testDeleteUsersAssignments() {
        $this->object->createAssignment(5, 1, 0, 1, "jack", -1, TestAssignment::CREATED_BY_TESTER);
        DB_DAO::executeQuery("UPDATE test_assignement SET id=999 WHERE ID IS NULL");
        $this->object->deleteUsersAssignments("import");
        $this->assertNull($this->object->getAssigmentProgress(1));
        $this->assertNull($this->object->getAssignment(1));
        $this->assertNotNull($this->object->getAssignment(999));
    }

    public function getRunsByDate($startDate, $stopDate) {
        
    }

    public function testGetBasicAssignment() {
        $basic = $this->object->getBasicAssignment(1);
        $this->assertEquals("import", $basic->username);
        $this->assertEquals("Sample test run", $basic->testRunTitle);
        $this->assertEquals("2013-11-28 00:00:00", $basic->deadline);
        $this->assertEquals(5, $basic->specificationId);
        $this->assertEquals("LESS support Test Specification for test 7.4", $basic->specification);
        $this->assertEquals("Windows", $basic->platform);
        $this->assertEquals(1, $basic->testRunId);
    }

    public function createMatrixAssignment($data) {
        
    }

    public function startAssignment($assignmentId, $startTime) {
        
    }

    public function startAssignmentConditional($assignmentId, $startTime) {
        
    }

    public function testGetAssignees() {
        $users = $this->object->getAssignees(1);
        $this->assertEquals(1, count($users));
        $this->assertEquals("import", $users[0]->username);
        $this->object->createAssignment(9, 1, 0, 1, "jack", -1, TestAssignment::CREATED_BY_TESTER);
        $users2 = $this->object->getAssignees(1);
        $this->assertEquals(2, count($users2));
        $this->assertEquals("import", $users2[0]->username);
        $this->assertEquals("jack", $users2[1]->username);
    }

}
