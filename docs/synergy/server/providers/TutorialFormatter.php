<?php

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

namespace Synergy\Providers;

use DOMDocument;
use DOMXPath;
use Synergy\Interfaces\TutorialProvider;

/**
 * Description of TutorialFormatter
 *
 * @author vriha
 */
class TutorialFormatter implements TutorialProvider {

    public function prepare($content, $tutorialUrl) {
        $thirdLevelDomain = $this->getThirdLevelDomain($tutorialUrl);
        $content = preg_replace("/(\.\.\/){1,4}/i", "https://" . $thirdLevelDomain . "netbeans.org/", $content); // images
        $dom = new DOMDocument;                    //scripts
        $dom->loadHTML($content);
        $xpath = new DOMXPath($dom);
        $nodes = $xpath->query('//script');
        foreach ($nodes as $node) {
            $node->parentNode->removeChild($node);
        }
        return $dom->saveHTML();
    }

    private function getThirdLevelDomain($url) {
        $result = preg_match("/https?:\/\/(\w*)\.(\w*)\.org/i", $url, $matches);
        if ($result === 0 || $result === FALSE) {
            return "";
        } else {
            return $matches[1].".";
        }
    }

}
