<?php
namespace Synergy\Model;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 * Description of UsersResult
 *
 * @author vriha
 */
class UsersResult {

    public $url;
    public $nextUrl;
    public $prevUrl;
    /**
     *
     * @var User[]
     */
    public $users = array();

    function __construct($page) {
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
        
    }

}

?>
