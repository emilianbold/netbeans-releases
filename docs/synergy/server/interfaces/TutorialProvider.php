<?php

namespace Synergy\Interfaces;

/**
 *
 * @author vriha
 */
interface TutorialProvider {

    /**
     * Prepares given content so it is possible to safely use it in tutorial review page.
     * Typically it removes all scripts and fixes relative URLs for e.g. images
     * @param String $content content to be prepared
     * @param String $tutorialUrl url of content to be prepared
     * @return String prepared content
     */
    public function prepare($content, $tutorialUrl);
}
