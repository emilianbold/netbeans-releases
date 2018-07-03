<?php

namespace Synergy\Controller\Test;

use Synergy\Controller\AttachmentCtrl;
use Synergy\DB\Test\FixtureTestCase;

/**
 * Description of AttachmentCtrlTest
 *
 * @author vriha
 */
class AttachmentCtrlTest extends FixtureTestCase {

    protected $object;

    public function setUp() {
        $this->object = new AttachmentCtrl();
        parent::setUp();
    }

    public function testGetSpecificationAttachment() {
        $this->assertEquals('/var/www/att/1384162432_TS_74_LessSupport_sample.less', $this->object->getSpecificationAttachment(1));
        $this->assertEquals('', $this->object->getSpecificationAttachment(11));
    }

    public function testDeleteSpecificationAttachment() {
        $this->assertTrue($this->object->deleteSpecificationAttachment(1));
        $this->assertEquals('', $this->object->getSpecificationAttachment(1));
    }

    public function testDeleteRunAttachment() {
        $this->assertTrue($this->object->deleteRunAttachment(1));
        $this->assertEquals('', $this->object->getRunAttachment(1));
    }

    public function testGetSpecificationId() {
        $this->assertEquals(5, $this->object->getSpecificationId(1));
        $this->assertEquals(-1, $this->object->getSpecificationId(11));
    }

    public function testGetRunAttachment() {
        $this->assertEquals('/empty', $this->object->getRunAttachment(1));
        $this->assertEquals('', $this->object->getRunAttachment(11));
    }

}
