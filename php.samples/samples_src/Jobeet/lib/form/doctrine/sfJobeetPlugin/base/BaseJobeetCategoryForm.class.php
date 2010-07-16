<?php

/**
 * JobeetCategory form base class.
 *
 * @package    form
 * @subpackage jobeet_category
 * @version    SVN: $Id: sfDoctrineFormGeneratedTemplate.php 8508 2008-04-17 17:39:15Z fabien $
 */
class BaseJobeetCategoryForm extends BaseFormDoctrine
{
  public function setup()
  {
    $this->setWidgets(array(
      'id'                     => new sfWidgetFormInputHidden(),
      'created_at'             => new sfWidgetFormDateTime(),
      'updated_at'             => new sfWidgetFormDateTime(),
      'jobeet_affiliates_list' => new sfWidgetFormDoctrineChoiceMany(array('model' => 'JobeetAffiliate')),
    ));

    $this->setValidators(array(
      'id'                     => new sfValidatorDoctrineChoice(array('model' => 'JobeetCategory', 'column' => 'id', 'required' => false)),
      'created_at'             => new sfValidatorDateTime(array('required' => false)),
      'updated_at'             => new sfValidatorDateTime(array('required' => false)),
      'jobeet_affiliates_list' => new sfValidatorDoctrineChoiceMany(array('model' => 'JobeetAffiliate', 'required' => false)),
    ));

    $this->widgetSchema->setNameFormat('jobeet_category[%s]');

    $this->errorSchema = new sfValidatorErrorSchema($this->validatorSchema);

    parent::setup();
  }

  public function getModelName()
  {
    return 'JobeetCategory';
  }

  public function updateDefaultsFromObject()
  {
    parent::updateDefaultsFromObject();

    if (isset($this->widgetSchema['jobeet_affiliates_list']))
    {
      $this->setDefault('jobeet_affiliates_list', $this->object->JobeetAffiliates->getPrimaryKeys());
    }

  }

  protected function doSave($con = null)
  {
    parent::doSave($con);

    $this->saveJobeetAffiliatesList($con);
  }

  public function saveJobeetAffiliatesList($con = null)
  {
    if (!$this->isValid())
    {
      throw $this->getErrorSchema();
    }

    if (!isset($this->widgetSchema['jobeet_affiliates_list']))
    {
      // somebody has unset this widget
      return;
    }

    if (is_null($con))
    {
      $con = $this->getConnection();
    }

    $this->object->unlink('JobeetAffiliates', array());

    $values = $this->getValue('jobeet_affiliates_list');
    if (is_array($values))
    {
      $this->object->link('JobeetAffiliates', $values);
    }
  }

}