<?php

class My_View_Helper_CzechMonthName extends Zend_View_Helper_Abstract {

  public function czechMonthName($mon) {
    $months = array(
        '01' => 'Leden',
        '02' => 'Únor',
        '03' => 'Březen',
        '04' => 'Duben',
        '05' => 'Květen',
        '06' => 'Červen',
        '07' => 'Červenec',
        '08' => 'Srpen',
        '09' => 'Září',
        '10' => 'Říjen',
        '11' => 'Listopad',
        '12' => 'Prosinec');
    return $months[$mon];
  }

}

?>
