<?php

namespace Synergy\Controller\Test;

use Synergy\Controller\SearchCtrl;
use Synergy\DB\Test\FixtureTestCase;

/**
 * Description of SearchCtrlTest
 *
 * @author vriha
 */
class SearchCtrlTest extends FixtureTestCase {

    /**
     * @var SearchCtrl
     */
    protected $object;

    public function setUp() {
        $this->object = new SearchCtrl();
        parent::setUp();
    }

    public function testSearch() {
        $data = $this->object->search("co",100,100);
        $specsCount = 0;
        $suitesCount = 0;
        foreach ($data as $d) {
            switch ($d->type) {
                case "specification":
                    $specsCount++;
                    break;
                case "suite":
                    $suitesCount++;
                    break;
                default :
                    break;
            }
        }
        $this->assertEquals(4, $specsCount + $suitesCount);
        $this->assertEquals(3, $specsCount);
        $this->assertEquals(1, $suitesCount);
    }

}
