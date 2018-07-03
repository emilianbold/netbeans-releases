<?php

namespace Synergy\Providers;

use DOMDocument;
use DOMNode;
use DOMNodeList;
use Synergy\Interfaces\ReviewImporter;
use Synergy\Misc\Util;
use Synergy\Model\Exception\CurlRequestException;
use Synergy\Model\Review\ReviewPage;

/**
 * Description of ReviewImporterCtrl
 *
 * @author vriha
 */
class ReviewImporterCtrl implements ReviewImporter {

    public function parseFromUrl($url) {
        $result = Util::requestUrlByCurl($url);
        if ($result->headers['http_code'] !== 200) {
            throw new CurlRequestException("Curl request failed", "Response from URL was " . $result->headers['http_code'], "");
        }

        return $this->parseHtml($result->data);
    }

    private function parseHtml($html) {
        $page = new DOMDocument();
        $page->loadHTML($html);
        $tables = $page->getElementsByTagName("table");
        $tutorialsA = $this->parseTable($tables->item(2));
        $tutorialsB = $this->parseTable($tables->item(1));
        return array_merge($tutorialsA, $tutorialsB);
    }

    /**
     * 
     * @param \DOMNode $table
     * @return type
     */
    private function parseTable($table) {
        $rows = $table->childNodes; //getElementsByTagName("tr");
        $results = array();
        for ($i = 1, $max = $rows->length; $i < $max; $i++) {
            $results[] = $this->parseRow($rows->item($i)->childNodes);
        }
        return $results;
    }

    /**
     * 
     * @param DOMNodeList $cells
     */
    private function parseRow($cells) {

        $owner = $cells->item(3)->textContent;
        $title = $cells->item(0)->textContent;
        $link = "";
        $_a = $cells->item(0)->childNodes;
        for ($i = 1, $max = $_a->length; $i < $max; $i++) {
            if ($_a->item($i)->nodeType === XML_ELEMENT_NODE) {
                $attributes = $_a->item($i)->attributes;
                foreach ($attributes as $attr) {
                    if (strtolower($attr->nodeName) === "href") {
                        $link = $attr->nodeValue;
                        break;
                    }
                }
            }
        }

        return new ReviewPage($title, $owner, $link);
    }

}
