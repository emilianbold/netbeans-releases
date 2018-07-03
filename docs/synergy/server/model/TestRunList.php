<?php
namespace Synergy\Model;

/**
 * Description of TestRunList
 *
 * @author lada
 */
class TestRunList {

    public $url;
    public $nextUrl;
    public $prevUrl;
    public $testRuns = array();

    function __construct($page, $data) {
        $this->nextUrl = "";
        $this->prevUrl = "";
        $segments = "http://$_SERVER[HTTP_HOST]$_SERVER[REQUEST_URI]";
        $segments = explode('?', $segments, 2);
        $url = $segments[0];

        if ($page > 1) {
            $this->prevUrl = $url . "?page=" . ($page - 1);
        }
        $this->nextUrl = $url . "?page=" . ($page + 1);
        $this->url = $url . "?page=" . ($page);

        $this->testRuns = $data;
    }

}

?>
