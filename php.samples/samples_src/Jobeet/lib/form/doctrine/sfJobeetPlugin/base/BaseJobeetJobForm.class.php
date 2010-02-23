<?php

/**
 * JobeetJob form base class.
 *
 * @package    form
 * @subpackage jobeet_job
 * @version    SVN: $Id: sfDoctrineFormGeneratedTemplate.php 8508 2008-04-17 17:39:15Z fabien $
 */
class BaseJobeetJobForm extends BaseFormDoctrine
{
  public function setup()
  {
    $this->setWidgets(array(
      'id'           => new sfWidgetFormInputHidden(),
      'category_id'  => new sfWidgetFormDoctrineSelect(array('model' => 'JobeetCategory', 'add_empty' => false)),
      'type'         => new sfWidgetFormInput(),
      'company'      => new sfWidgetFormInput(),
      'logo'         => new sfWidgetFormInput(),
      'url'          => new sfWidgetFormInput(),
      'position'     => new sfWidgetFormInput(),
      'location'     => new sfWidgetFormInput(),
      'description'  => new sfWidgetFormTextarea(),
      'how_to_apply' => new sfWidgetFormTextarea(),
      'token'        => new sfWidgetFormInput(),
      'is_public'    => new sfWidgetFormInputCheckbox(),
      'is_activated' => new sfWidgetFormInputCheckbox(),
      'email'        => new sfWidgetFormInput(),
      'expires_at'   => new sfWidgetFormDateTime(),
      'created_at'   => new sfWidgetFormDateTime(),
      'updated_at'   => new sfWidgetFormDateTime(),
    ));

    $this->setValidators(array(
      'id'           => new sfValidatorDoctrineChoice(array('model' => 'JobeetJob', 'column' => 'id', 'required' => false)),
      'category_id'  => new sfValidatorDoctrineChoice(array('model' => 'JobeetCategory')),
      'type'         => new sfValidatorString(array('max_length' => 255, 'required' => false)),
      'company'      => new sfValidatorString(array('max_length' => 255)),
      'logo'         => new sfValidatorString(array('max_length' => 255, 'required' => false)),
      'url'          => new sfValidatorString(array('max_length' => 255, 'required' => false)),
      'position'     => new sfValidatorString(array('max_length' => 255)),
      'location'     => new sfValidatorString(array('max_length' => 255)),
      'description'  => new sfValidatorString(array('max_length' => 4000)),
      'how_to_apply' => new sfValidatorString(array('max_length' => 4000)),
      'token'        => new sfValidatorString(array('max_length' => 255)),
      'is_public'    => new sfValidatorBoolean(),
      'is_activated' => new sfValidatorBoolean(),
      'email'        => new sfValidatorString(array('max_length' => 255)),
      'expires_at'   => new sfValidatorDateTime(),
      'created_at'   => new sfValidatorDateTime(array('required' => false)),
      'updated_at'   => new sfValidatorDateTime(array('required' => false)),
    ));

    $this->validatorSchema->setPostValidator(
      new sfValidatorDoctrineUnique(array('model' => 'JobeetJob', 'column' => array('token')))
    );

    $this->widgetSchema->setNameFormat('jobeet_job[%s]');

    $this->errorSchema = new sfValidatorErrorSchema($this->validatorSchema);

    parent::setup();
  }

  public function getModelName()
  {
    return 'JobeetJob';
  }

}