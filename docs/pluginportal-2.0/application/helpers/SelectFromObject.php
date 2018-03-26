<?php
/**
 * Generic helper for generating select field from Doctrine_Collection object
 *
 * @author janpirek
 */
class My_View_Helper_SelectFromObject extends Zend_View_Helper_Abstract {
  /**
   * Generic helper for generating select field from Doctrine_Collection object
   *
   * @param Doctrine_Collection $data
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
  public function selectFromObject($data, $labelField, $valueField, $selectedValue=0, $selectName='companyTypeId', $class=false, $multiple=false, $size=1, $disabled=false, $emptyValue=false) {
    $mult=($multiple)? ' multiple="multiple"' : '';
    $size=($size>1)? ' size="'.$size.'"' : '';
    $dis=($disabled)? ' disabled="disabled"' : '';
    $class=($class)? ' class="'.$class.'"' : '';
    $empty=($emptyValue)? '<option value="">'.$emptyValue.'</option>' : '';
    $out='<select name="'.$selectName.'" id="'.$selectName.'"'.$mult.$size.$dis.$class.'>';
    $out.=$empty;
    foreach($data as $t) {
      $t->$labelField;
      $label='';
      if(is_array($labelField)) {
        foreach ($labelField as $f) {
          $label.=$t->$f.' ';
        }
      } else {
        $label=$t->$labelField;
      }
      if(is_array($selectedValue)) {
        $selected=(in_array($t->$valueField, $selectedValue))? ' selected="selected"' : '';
      } else {
        $selected=($selectedValue==$t->$valueField)? ' selected="selected"' : '';
      }
      $out.='<option value="'.$t->$valueField.'"'.$selected.'>'.$label.'</option>';
    }
    $out.='</select>';
    return $out;
  }
}
?>
