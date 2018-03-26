<?php
class My_View_Helper_WordShitCleaner  extends Zend_View_Helper_Abstract {

  public function wordShitCleaner($data) {
    $data=strip_tags($data, '<p><div><span><br><i><b><ul><li><table><tr><td><a><sub><sup><u>');
    return $data;
  }
}

?>
