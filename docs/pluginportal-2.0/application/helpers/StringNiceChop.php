<?php
/* 
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
*/

/**
 * Description of StringNiceChop
 *
 * @author janpirek
 */
class My_View_Helper_StringNiceChop extends Zend_View_Helper_Abstract {

  /**
   * Chop string to N characters, do not break words
   *
   * @access public
   * @param string string we are operating with
   * @param integer character count to cut to
   * @param string|NULL pad. Default: '...'
   * @param string|NULL break. Where to break after the limit, default: ' '
   * @return string processed string
   **/
  public function stringNiceChop($string, $limit, $pad='...',$break=' ') {
    // remove all \r,\n
    $string = str_replace("\n","",$string);
    $string = str_replace("\r","",$string);
    //$string=strip_tags($string);
    // return with no change if string is shorter than $limit
    if(strlen($string) <= $limit) return $string;
    // is $break present between $limit and the end of the string?
    if(false !== ($breakpoint = strpos($string, $break, $limit))) {
      //echo 'cut';
      if($breakpoint < strlen($string) - 1) {
        $string = substr($string, 0, $breakpoint) . $pad;
      }
    } return
    $string;
  }
}
?>
