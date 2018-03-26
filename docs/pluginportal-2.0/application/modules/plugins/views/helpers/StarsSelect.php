<?php
class Zend_View_Helper_StarsSelect {
  public function starsSelect($elid, $selected=0) {
    $ret='<div id="'.$elid.'"><select name="selrate-'.$elid.'">';
    for($i=1;$i<=5;$i++ ){
      $sel=($selected==$i)? 'selected="selected"' : '';
      $ret.='<option value="'.$i.'" '.$sel.'>'.$i.'</option>';
    }
    $ret.='</select></div>';
    return $ret;
  }
}
?>