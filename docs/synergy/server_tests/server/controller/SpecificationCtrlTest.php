<?php

namespace Synergy\Controller\Test;

use Synergy\Controller\CaseCtrl;
use Synergy\Controller\RunCtrl;
use Synergy\Controller\SpecificationCtrl;
use Synergy\DB\CaseDAO;
use Synergy\DB\DB_DAO;
use Synergy\DB\Test\FixtureTestCase;
use Synergy\Model\Specification;

/**
 * Description of SpecificationCtrlTest
 *
 * @author vriha
 */
class SpecificationCtrlTest extends FixtureTestCase {

    /**
     * @var SpecificationCtrl
     */
    protected $object;

    public function setUp() {
        $this->object = new SpecificationCtrl();
        parent::setUp();
    }

    public function testGetSpecifications() {
        $d = $this->object->getSpecifications(2);
        $this->assertEquals(1, count($d));

        $d2 = $this->object->getSpecifications(3, 6);
        $this->assertEquals(2, count($d2));

        $d3 = $this->object->getSpecifications(-1, -1);
        $this->assertEquals(4, count($d3));
    }

    public function testGetSpecificationFullByAlias() {
        $d = $this->object->getSpecificationFullByAlias('cordova_support_test_specification_for_test_7.4', 3);
        $this->assertEquals('Cordova support Test Specification for test 7.4', $d->title);

        $d2 = $this->object->getSpecificationFullByAlias('cordova_support_test_specification_for_test_7.4', -1);
        $this->assertEquals('Cordova support Test Specification for test 8.0', $d2->title);
    }

    public function testGetAllSpecificationsGroupedBySimpleName() {
        $d = $this->object->getSpecificationsByVersion();
        $this->assertEquals(3, count($d));
    }

    public function testGetSimilarSpecs() {
        $d = $this->object->getSimilarSpecs('cordova_support_test_specification_for_test_7.4', 7);
        $this->assertEquals(2, count($d));
    }

    public function testGetFavoriteSpecifications() {
        $d = $this->object->getFavoriteSpecifications('import');
        $this->assertEquals(1, count($d));
    }

    public function testGetEstimatedTime() {
        $this->assertEquals(5, $this->object->getEstimatedTime(4));
        $this->assertEquals(1, $this->object->getEstimatedTime(4, 'sanity'));
    }

    public function testGetVersionID() {
        $this->assertEquals(3, $this->object->getVersionID(4));
    }

    public function testGetCasesCount() {
        $this->assertEquals(5, $this->object->getCasesCount(4));
        $this->assertEquals(1, $this->object->getCasesCount(4, 1));
    }

    public function testGetSpecificationFull() {
        $spec = $this->object->getSpecificationFull(5);
        $this->assertEquals('LESS support Test Specification for test 7.4', $spec->title);
        $this->assertEquals(7, $spec->authorId);
        $this->assertEquals('jack jack', $spec->authorName);
        $this->assertEquals(5, count($spec->testSuites));
        $totalCases = 0;
        $caseFound = false;
        for ($i = 0; $i < count($spec->testSuites); $i++) {
            $totalCases += count($spec->testSuites[$i]->testCases);
            for ($j = 0; $j < count($spec->testSuites[$i]->testCases); $j++) {
                if ($spec->testSuites[$i]->testCases[$j]->title === "Syntax coloring") {
                    $this->assertEquals(1, count($spec->testSuites[$i]->testCases[$j]->images));
                    $this->assertEquals('TS_74_LessSupport_coloring.png', $spec->testSuites[$i]->testCases[$j]->images[0]->title);
                    $caseFound = true;
                }
            }
        }

        $this->assertTrue($caseFound);
        $this->assertEquals(23, $totalCases);
        $this->assertEquals(2, count($spec->attachments));
        $this->assertEquals('TS_74_LessSupport_sample.less', $spec->attachments[0]->name);
    }

    public function testUpdateSpecification() {
        $spec = $this->object->getSpecificationFull(4);
        $spec->title = "New specification for Cordova";
        $result = $this->object->updateSpecification($spec, true);
        $this->assertTrue($result);

        $spec = $this->object->getSpecificationFull(4);
        $this->assertEquals('new_specification_for_cordova', $spec->simpleName);
        $this->assertEquals('New specification for Cordova', $spec->title);

        $d = $this->object->getSimilarSpecs('new_specification_for_cordova', 4);
        $this->assertEquals(2, count($d));
        $d = $this->object->getSimilarSpecs('cordova_support_test_specification_for_test_7.4', 4);
        $this->assertEquals(0, count($d));
    }

    public function testGetSpecificationsByAuthor() {
        $this->assertEquals(1, count($this->object->getSpecificationsByOwner('jack')));
    }

    public function testGetSpecificationsByOwner() {
        $this->assertEquals(1, count($this->object->getSpecificationsByOwner('jack')));
    }

    public function testCreateSpecification() {
        $sampleSpec = new Specification(13, "test specification", "random title", "8.0", 6, 6);
        $sampleSpec->author = "import";
        $sampleSpec->authorName = "import import";
        $sampleSpec->ownerName = "import import";
        $sampleSpec->version = "8.0";
        $this->object->createSpecification($sampleSpec, false);
        $createdSpec = $this->object->getSpecificationFullByAlias('random_title', -1);

        $this->assertEquals($sampleSpec->title, $createdSpec->title);
        $this->assertEquals($sampleSpec->desc, $createdSpec->desc);
        $this->assertEquals($sampleSpec->version, $createdSpec->version);
        $this->assertEquals($sampleSpec->authorName, $createdSpec->authorName);
        $this->assertEquals($sampleSpec->ownerName, $createdSpec->ownerName);
    }

    public function testDeleteSpecification() {
        $this->object->deleteSpecification(5);
        $s = $this->object->getSpecification(5);
        $this->assertNull($s);
        $this->assertEquals(0, count($this->object->getAttachments(5)));
        $this->assertEquals(0, count($this->object->getSuitesIDs(5)));

        $caseCtrl = new CaseCtrl();
        $this->assertNull($caseCtrl->getCase(35, 13));

        $runCtrl = new RunCtrl();
        $this->assertNull($runCtrl->getAssigmentProgress(1));
        $this->assertNull($runCtrl->getAssignment(1));

        $this->assertEquals(0, count($this->object->getFavoriteSpecifications('jack')));

        $caseDao = new CaseDAO();
        $this->assertEquals(0, count($caseDao->getImages(6)));
    }

    public function testCloneSpecification() {
        $sampleSpec = $this->object->getSpecification(1);
        $this->object->cloneSpecification(1, "8.0", "test two");

        DB_DAO::executeQuery("UPDATE specification SET id=13 WHERE title='test two';"); // phpunit in this case does not auto insert ID!!!
        $createdSpec = $this->object->getSpecificationFullByAlias('test_two', -1);
        $differentTitles = false;
        if ($sampleSpec->title !== $createdSpec->title) {
            $differentTitles = true;
        }
        $this->assertTrue($differentTitles);
        $this->assertEquals($sampleSpec->desc, $createdSpec->desc);
        $this->assertEquals("8.0", $createdSpec->version);
        $this->assertEquals($sampleSpec->authorName, $createdSpec->authorName);
        $this->assertEquals($sampleSpec->ownerName, $createdSpec->ownerName);

        $d = $this->object->getSimilarSpecs('test_two', -1);
        $this->assertEquals(2, count($d));
        $d = $this->object->getSimilarSpecs('test', -1);
        $this->assertEquals(0, count($d));
        $sampleSpec = $this->object->getSpecification(1);
        $this->assertEquals($sampleSpec->simpleName, 'test_two');
    }

}
