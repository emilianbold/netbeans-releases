<?php

namespace Synergy\Model;

/**
 * Description of Revision
 *
 * @author vriha
 */
class Revision {

    public $id;
    public $specificationId;
    public $content;
    public $author;
    public $date;

    function __construct($id, $specificationId, $content, $author, $date) {
        $this->id = intval($id);
        $this->specificationId = intval($specificationId);
        $this->content = $content;
        $this->author = $author;
        date_default_timezone_set('UTC');
        $str = strtotime($date);
        $this->date = gmdate("d M Y H:i:s", $str) . " UTC";
    }

}
