<?php

namespace Synergy\DB\Test;

use Synergy\DB\SpecificationDAO;

/**
 * Description of SpecificationDAOTest
 *
 * @author vriha
 */
class SpecificationDAOTest extends FixtureTestCase {

    public $fixtures = array(
        'dump'
    );

    /**
     * @group DB
     */
    function testGetAllSpecifications() {
        $specDao = new SpecificationDAO();
        $list = $specDao->getAllSpecifications();
        $this->assertEquals(4, count($list)); 
    }
    /**
     * @group DB
     */
    function testGetAllSpecificationsUser() {
        $specDao = new SpecificationDAO();
        $list = $specDao->getAllSpecifications(6);
        $this->assertEquals(4, count($list)); 
    }

}
