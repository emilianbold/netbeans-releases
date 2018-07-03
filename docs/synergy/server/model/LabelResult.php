<?php
namespace Synergy\Model;

/**
 * Description of LabelResult
 *
 * @author lada
 */
class LabelResult {

    public $url;
    public $nextUrl;
    public $prevUrl;
    public $label;
    public $cases;

    function __construct($page, $label) {
        $this->nextUrl = "";
        $this->prevUrl = "";
        $this->cases = array();
        $segments = "http://$_SERVER[HTTP_HOST]$_SERVER[REQUEST_URI]";
        $segments = explode('?', $segments, 2);
        $url = $segments[0];

        if ($page > 1) {
            $this->prevUrl = $url."?label=".rawurlencode($label)."&page=".($page-1);
        }
        $this->nextUrl = $url."?label=".rawurlencode($label)."&page=".($page+1);
        $this->url = $url."?label=".rawurlencode($label)."&page=".($page);
        $this->label = $label;

    }

}

?>
