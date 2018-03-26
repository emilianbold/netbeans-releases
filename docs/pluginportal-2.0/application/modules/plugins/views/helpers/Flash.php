<?php
class Zend_View_Helper_Flash {
  public function flash() {
    //$_SESSION['flash']['type']=(!$_SESSION['flash']['type'])? 'error' : $_SESSION['flash']['type'];
    $o='<ul class="'.$_SESSION['flash']['type'].'">'.$_SESSION['flash']['msg'].'</ul>';
    unset($_SESSION['flash']);
    return $o;
  }
}
?>