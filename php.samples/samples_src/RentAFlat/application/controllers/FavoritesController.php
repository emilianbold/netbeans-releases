<?php

class FavoritesController extends Zend_Controller_Action {

    public function addAction() {
        General::addToCookie($this->getRequest()->getParam("id"));
        echo '<a id="p'. $this->getRequest()->getParam("id") .'" class="fav-del" href="#" onclick="removeFromFavorites(' . $this->getRequest()->getParam("id") . ')"
            title="Remove from favorites">Remove from favorites</a>';
        exit;
    }

    public function removeAction() {
        General::removeFromCookie($this->getRequest()->getParam("id"));
        echo '<a id="p'. $this->getRequest()->getParam("id") .'" class="fav" href="#" onclick="addToFavorites(' . $this->getRequest()->getParam("id") . ')"
            title="Add to favorites">Add to favorites</a>';
        exit;
    }

    public function addDetailAction() {
        General::addToCookie($this->getRequest()->getParam("id"));
        echo '<a href="#" onclick="removeFromFavoritesDetail(' . $this->getRequest()->getParam("id") . ')"
            title="Remove from favorites">Remove from favorites</a>';
        exit;
    }

    public function removeDetailAction() {
        General::removeFromCookie($this->getRequest()->getParam("id"));
        echo '<a fav" href="#" onclick="addToFavoritesDetail(' . $this->getRequest()->getParam("id") . ')"
            title="Add to favorites">Add to favorites</a>';
        exit;
    }

}

