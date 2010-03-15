<?php

/**
 * JobeetAffiliate form base class.
 *
 * @package    form
 * @subpackage jobeet_affiliate
 * @version    SVN: $Id: sfDoctrineFormGeneratedTemplate.php 8508 2008-04-17 17:39:15Z fabien $
 */
class BaseJobeetAffiliateForm extends BaseFormDoctrine
{
  public function setup()
  {
    $this->setWidgets(array(
      'id'                     => new sfWidgetFormInputHidden(),
      'url'                    => new sfWidgetFormInput(),
      'email'                  => new sfWidgetFormInput(),
      'token'                  => new sfWidgetFormInput(),
      'is_active'              => new sfWidgetFormInputCheckbox(),
      'created_at'             => new sfWidgetFormDateTime(),
      'updated_at'             => new sfWidgetFormDateTime(),
      'jobeet_categories_list' => new sfWidgetFormDoctrineChoiceMany(array('model' => 'JobeetCategory')),
    ));

    $this->setValidators(array(
      'id'                     => new sfValidatorDoctrineChoice(array('model' => 'JobeetAffiliate', 'column' => 'id', 'required' => false)),
      'url'                    => new sfValidatorString(array('max_length' => 255)),
      'email'                  => new sfValidatorString(array('max_length' => 255)),
      'token'                  => new sfValidatorString(array('max_length' => 255)),
      'is_active'              => new sfValidatorBoolean(),
      'created_at'             => new sfValidatorDateTime(array('required' => false)),
      'updated_at'             => new sfValidatorDateTime(array('required' => false)),
      'jobeet_categories_list' => new sfValidatorDoctrineChoiceMany(array('model' => 'JobeetCategory', 'required' => false)),
    ));

    $this->validatorSchema->setPostValidator(
      new sfValidatorDoctrineUnique(array('model' => 'JobeetAffiliate', 'column' => array('email')))
    );

    $this->widgetSchema->setNameFormat('jobeet_affiliate[%s]');

    $this->errorSchema = new sfValidatorErrorSchema($this->validatorSchema);

    parent::setup();
  }

  public function getModelName()
  {
    return 'JobeetAffiliate';
  }

  public function updateDefaultsFromObject()
  {
    parent::updateDefaultsFromObject();

    if (isset($this->widgetSchema['jobeet_categories_list']))
    {
      $this->setDefault('jobeet_categories_list', $this->object->JobeetCategories->getPrimaryKeys());
    }

  }

  protected function doSave($con = null)
  {
    parent::doSave($con);

    $this->saveJobeetCategoriesList($con);
  }

  public function saveJobeetCategoriesList($con = null)
  {
    if (!$this->isValid())
    {
      throw $this->getErrorSchema();
    }

    if (!isset($this->widgetSchema['jobeet_categories_list']))
    {
      // somebody has unset this widget
      return;
    }

    if (is_null($con))
    {
      $con = $this->getConnection();
    }

    $this->object->unlink('JobeetCategories', array());

    $values = $this->getValue('jobeet_categories_list');
    if (is_array($values))
    {
      $this->object->link('JobeetCategories', $values);
    }
  }

}