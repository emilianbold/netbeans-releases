<?php

/**
 * JobeetCategoryAffiliate form base class.
 *
 * @package    form
 * @subpackage jobeet_category_affiliate
 * @version    SVN: $Id: sfDoctrineFormGeneratedTemplate.php 8508 2008-04-17 17:39:15Z fabien $
 */
class BaseJobeetCategoryAffiliateForm extends BaseFormDoctrine
{
  public function setup()
  {
    $this->setWidgets(array(
      'category_id'  => new sfWidgetFormInputHidden(),
      'affiliate_id' => new sfWidgetFormInputHidden(),
    ));

    $this->setValidators(array(
      'category_id'  => new sfValidatorDoctrineChoice(array('model' => 'JobeetCategoryAffiliate', 'column' => 'category_id', 'required' => false)),
      'affiliate_id' => new sfValidatorDoctrineChoice(array('model' => 'JobeetCategoryAffiliate', 'column' => 'affiliate_id', 'required' => false)),
    ));

    $this->widgetSchema->setNameFormat('jobeet_category_affiliate[%s]');

    $this->errorSchema = new sfValidatorErrorSchema($this->validatorSchema);

    parent::setup();
  }

  public function getModelName()
  {
    return 'JobeetCategoryAffiliate';
  }

}