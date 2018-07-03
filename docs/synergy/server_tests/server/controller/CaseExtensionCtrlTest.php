<?php

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
namespace Synergy\Controller\Test;

use Synergy\Controller\ExtensionCtrl;
use Synergy\Model\TestCase;
/**
 * Description of CaseExtensionCtrlTest
 *
 * @author lada
 */
class CaseExtensionCtrlTest extends \PHPUnit_Framework_TestCase {

    /**
     * @var ExtensionCtrl
     */
    protected $object;

    /**
     * Sets up the fixture, for example, opens a network connection.
     * This method is called before a test is executed.
     */
    protected function setUp() {
        $this->object = new ExtensionCtrl('case');
    }

    /**
     * Tears down the fixture, for example, closes a network connection.
     * This method is called after a test is executed.
     */
    protected function tearDown() {
        
    }

    /**
     * @covers \Synergy\Controller\CaseCtrl::getCase
     */
    public function testGetCase() {
        $case = new TestCase('Title', 20, 1);
        /* @var $case2 TestCase */
        $case2 = $this->object->get($case);
        $this->assertEquals(1, $case2->id);
        $this->assertEquals('Title', $case2->title);
        $this->assertEquals(20, $case2->duration);
    }

}
