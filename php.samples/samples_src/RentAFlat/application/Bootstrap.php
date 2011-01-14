<?php

class Bootstrap extends Zend_Application_Bootstrap_Bootstrap {

    protected function _initDoctype() {

        $this->bootstrap('view');
        $view = $this->getResource('view');
        $view->doctype('XHTML1_STRICT');
    }

    protected function _initMenu() {
        $this->bootstrap('view');
        $view = $this->getResource('view');

        $view->placeholder('menu')
                ->setPrefix("<div id=\"menu\">")
                ->setPostfix("</div>");
    }

    protected function _initfFooter() {
        $this->bootstrap('view');
        $view = $this->getResource('view');

        $view->placeholder('footer')
                ->setPrefix("<div id=\"footbox\"><div id=\"foot\">")
                ->setPostfix("<div class=\"cleaner\"></div></div></div>");
    }

    

    public function toText($array) {
        foreach ($array as $object) {
            if ($object->getText_cz() == 1) {
                return "Ano";
            } else
                return "Ne";
        }
    }

}

class General {
    
    public static function getPerex($text) {
        if (strlen($text) > 200) {
            $firstPor = substr($text, 0, 200);
            $space = strrpos($firstPor, " ");

            return substr($firstPor, 0, $space) . " ...";
        } return $text;
    }

    public static function toText($value) {

        if ($value == 1) {
            return "Ano";
        } else
            return "Ne";
    }

    public static function addToCookie($id) {
        $session = new Zend_Session_Namespace('favorites');
        $session->setExpirationSeconds(2592000);
        if (is_numeric($id)) {
            //check if id is not in already 
            for ($i = 0; $i < count($session->favorites); $i++) {
                if ($session->favorites[$i] == $id) {
                    return;
                }
            }
            $session->favorites[] = $id;
        } return;
    }

    public static function isInFavorites($id) {

        if (is_numeric($id) && $id != null) {
            $session = new Zend_Session_Namespace('favorites');
            for ($i = 0; $i < count($session->favorites); $i++) {
                if ($session->favorites[$i] == $id) {
                    return true;
                }
            }
        }
        return false;
    }

    public static function removeFromCookie($id) {
        if (General::isInFavorites($id)) {
            $temp = array();
            $session = new Zend_Session_Namespace('favorites');
            for ($i = 0; $i < count($session->favorites); $i++) {
                if ($session->favorites[$i] != $id) {
                    $temp[] = $session->favorites[$i];
                }
            }
            $session->favorites = $temp;
        }
    }

}


