<?php

namespace Synergy\Model\Review;

use Synergy\App\Synergy;

/**
 * Description of ReviewPage
 *
 * @author vriha
 */
class ReviewPage {

    public $title;
    public $owner;
    public $url;
    private $hash;
    
    function __construct($title, $owner, $url) {
        $this->title = trim($title);
        $this->owner = trim(strtolower($owner));
        $this->url = trim($url);
    }

    public function getHash() {
        return $this->hash;
    }

    public function setHash($hash) {
        $this->hash = $hash;
    }

        
    public static function canCreate() {
        $role = Synergy::getSessionProvider()->getUserRole();
        switch ($role) {
            case "manager" :
            case "admin" :
                return true;
            default :
                return false;
        }
    }

}
