<?php

namespace Synergy\Controller;

use Synergy\App\Synergy;
use Synergy\DB\ReviewsDAO;
use Synergy\Providers\ReviewImporterCtrl;

/**
 * Description of ReviewsCtrl
 *
 * @author vriha
 */
class ReviewsCtrl {

    private $reviewsDao;

    function __construct() {
        $this->reviewsDao = new ReviewsDAO();
    }

    /**
     * Imports all tutorials from given URL
     * @param String $url
     */
    public function import($url) {
        $ct = Synergy::getProvider("reviewsParser");
        $pages = $ct->parseFromUrl($url);
        $this->removeAll();
        $this->insertNewPages($pages);
    }
    
    /**
     * Returns all pages available for reviews that are not already assigned in given test run
     * @param int $runId ID of test run
     * @return type
     */
    public function getAllNotStarted($runId) {
        return $this->reviewsDao->getAllNotStarted($runId);
    }

    /**
     * Returns all pages available for reviews
     * @return type
     */
    public function getAll() {
        return $this->reviewsDao->getAll();
    }

    /**
     * Clears list of pages available for review
     */
    public function removeAll() {
        $this->reviewsDao->removeAll();
    }

    public function insertNewPages($pages) {
        $this->reviewsDao->insertAll($pages);
    }

}
