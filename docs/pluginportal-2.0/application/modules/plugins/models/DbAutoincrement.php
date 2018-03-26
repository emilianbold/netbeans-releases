<?php

/**
 * DbAutoincrement emulates autoincrement feature of the DB as by David's original
 * application this was not done by DB itself but instead by app
 *
 * current value is stored in table sequence, col seq_count
 *
 * @author janpirek
 */
class DbAutoincrement {

  /**
   * get next value
   */
  public static function getNewValue() {
    $v = Doctrine_Query::create()->from('PpSequence')->fetchOne();
    if ($v) {
      return $v->seq_count + 1;
    } else {
      return 1;
    }
  }

  /**
   * increase the counter by 1
   */
  public static function increaseCounter() {
    Doctrine_Query::create()->update('PpSequence')->set('seq_count', self::getNewValue())->where('seq_name="SEQ_GEN"')->execute();
  }

}

?>
