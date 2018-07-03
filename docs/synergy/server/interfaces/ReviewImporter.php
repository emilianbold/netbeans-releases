<?php

namespace Synergy\Interfaces;

/**
 *
 * @author vriha
 */
interface ReviewImporter {

    /**
     * Parses data from given URL and returns array of ReviewPage instances from the URL
     * @param String $url URL to fetch data from
     * @return \Synergy\Model\Review\ReviewPage Description
     */
    public function parseFromUrl($url);
}
