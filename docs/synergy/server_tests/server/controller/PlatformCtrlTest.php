<?php

namespace Synergy\Controller\Test;

use Synergy\Controller\PlatformCtrl;
use Synergy\Controller\RunCtrl;
use Synergy\DB\Test\FixtureTestCase;

/**
 * Description of PlatformCtrlTest
 *
 * @author vriha
 */
class PlatformCtrlTest extends FixtureTestCase {

    /**
     * @var PlatformCtrl
     */
    protected $object;
    public static $extraLabel = false;

    /**
     * Sets up the fixture, for example, opens a network connection.
     * This method is called before a test is executed.
     */
    public function setUp() {
        $this->object = new PlatformCtrl();
        parent::setUp();
    }

    public function testUpdatePlatform() {
        $this->object->updatePlatform(1, "Windows8");
        $platforms = $this->object->getPlatforms();
        foreach ($platforms as $platform) {
            $this->assertTrue($platform->name !== "Windows");
        }
    }

    public function testCreatePlatform() {
        $this->object->createPlatform("MacOSX");
        $platforms = $this->object->getPlatforms();
        $this->assertEquals(2, count($platforms));
        $created = false;
        foreach ($platforms as $platform) {
            if ($platform->name === "MacOSX") {
                $created = true;
            }
        }
        $this->assertTrue($created);
    }

    public function testDeletePlatform() {
        $this->object->deletePlatform(1);
        $this->assertEquals(1, count($this->object->getPlatforms()));
        $runCtrl = new RunCtrl();
        $this->assertEquals(1, count($runCtrl->getRuns(1)));
        $this->assertNull($runCtrl->getAssignment(1));
        $this->assertNull($runCtrl->getAssigmentProgress(1));
    }

    public function testFindMatchingPlatform() {
        $data = $this->object->findMatchingPlatform("Win");
        $this->assertEquals(1, count($data));
    }

}
