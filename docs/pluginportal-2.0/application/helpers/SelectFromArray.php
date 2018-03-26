<?php
/**
 * Generic helper for generating select field from Doctrine_Collection object
 *
 * @author janpirek
 */
class My_View_Helper_SelectFromArray extends Zend_View_Helper_Abstract {
    /**
     * Generic helper for generating select field from Doctrine_Collection object
     *
     * @param array $data array(val=>label, val=>label);
     * @param string $labelField
     * @param string $valueField
     * @param string $selectedValue
     * @param string $selectName
     * @param string $class
     * @param boolean $multiple
     * @param integer $size
     * @param boolean $disabled
     * @return string
     */
    public function selectFromArray($data, $selectedValue=0, $selectName='companyTypeId', $class=false, $multiple=false, $size=1, $disabled=false, $emptyValue=false) {
      $mult=($multiple)? ' multiple="multiple"' : '';
      $size=($size>1)? ' size="'.$size.'"' : '';
      $dis=($disabled)? ' disabled="disabled"' : '';
      $class=($class)? ' class="'.$class.'"' : '';
      $empty=($emptyValue)? '<option value="">...</option>' : '';
      $out='<select name="'.$selectName.'" id="'.$selectName.'"'.$mult.$size.$dis.$class.'>';
      $out.=$empty;
      foreach($data as $val=>$label) {
        $selected=($selectedValue==$val)? ' selected="selected"' : '';
        $out.='<option value="'.$val.'"'.$selected.'>'.$label.'</option>';
      }
      $out.='</select>';
      return $out;
    }
}
?>
