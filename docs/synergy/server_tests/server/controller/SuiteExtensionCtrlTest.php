<?php
namespace Synergy\Controller\Test;

use Synergy\Controller\ExtensionCtrl;
use Synergy\Model\Suite;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of SuiteExtensionCtrlTest
 *
 * @author lada
 */
class SuiteExtensionCtrlTest extends \PHPUnit_Framework_TestCase {

    /**
     * @var ExtensionCtrl
     */
    protected $object;

    /**
     * Sets up the fixture, for example, opens a network connection.
     * This method is called before a test is executed.
     */
    protected function setUp() {
        $this->object = new ExtensionCtrl('suite');
    }

    /**
     * Tears down the fixture, for example, closes a network connection.
     * This method is called after a test is executed.
     */
    protected function tearDown() {
        
    }

    public function testGetSpecification() {
        $suite = new Suite(1,'description', 'Title', 'product', 'component',2);
        /* @var $suite2 Suite */
        $suite2 = $this->object->get($suite);
        $this->assertEquals(1, $suite2->id);
        $this->assertEquals('Title', $suite2->title);
        $this->assertEquals('description', $suite2->desc);
        $this->assertEquals('product', $suite2->product);
        $this->assertEquals('component', $suite2->component);
        $this->assertEquals(2, $suite2->specificationId);
    }

}
