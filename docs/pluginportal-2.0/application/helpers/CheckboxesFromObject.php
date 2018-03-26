<?php
/**
 * Generic helper for generating select field from Doctrine_Collection object
 *
 * @author janpirek
 */
class My_View_Helper_CheckboxesFromObject extends Zend_View_Helper_Abstract {
  /**
   * Generic helper for generating select field from Doctrine_Collection object
   *
   * @param Doctrine_Collection $data
   * @param string $labelField
   * @param string $valueField
   * @param string $selectedValue
   * @param string $selectName
   * @param string $class
   * @param string $separator
   * @return string
   */
  public function checkboxesFromObject($data, $labelField, $valueField, $selectedValue=array(), $selectName='companyTypeId', $class=false, $separator='&nbsp;', $type='checkbox', $disabled='') {
    
    $class=($class)? ' class="'.$class.'"' : '';
    //$out= //'<select name="'.$selectName.'" id="'.$selectName.'"'.$mult.$size.$dis.$class.'>';
    $out='';
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
        $selected=(in_array($t->$valueField, $selectedValue))? ' checked="checked"' : '';
      } else {
        $selected=($selectedValue==$t->$valueField)? ' checked="checked"' : '';
      }
      $out.='<input type="'.$type.'" name="'.$selectName.'" value="'.$t->$valueField.'" '.$selected.' '.$class.' '.$disabled.'/>'.$label.' '.$separator;
    }
    return $out;
  }
}
?>
