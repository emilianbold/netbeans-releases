<?php

/**
 * JobeetCategoryTranslation form base class.
 *
 * @package    form
 * @subpackage jobeet_category_translation
 * @version    SVN: $Id: sfDoctrineFormGeneratedTemplate.php 8508 2008-04-17 17:39:15Z fabien $
 */
class BaseJobeetCategoryTranslationForm extends BaseFormDoctrine
{
  public function setup()
  {
    $this->setWidgets(array(
      'id'   => new sfWidgetFormInputHidden(),
      'name' => new sfWidgetFormInput(),
      'lang' => new sfWidgetFormInputHidden(),
      'slug' => new sfWidgetFormInput(),
    ));

    $this->setValidators(array(
      'id'   => new sfValidatorDoctrineChoice(array('model' => 'JobeetCategoryTranslation', 'column' => 'id', 'required' => false)),
      'name' => new sfValidatorString(array('max_length' => 255)),
      'lang' => new sfValidatorDoctrineChoice(array('model' => 'JobeetCategoryTranslation', 'column' => 'lang', 'required' => false)),
      'slug' => new sfValidatorString(array('max_length' => 255, 'required' => false)),
    ));

    $this->validatorSchema->setPostValidator(
      new sfValidatorDoctrineUnique(array('model' => 'JobeetCategoryTranslation', 'column' => array('slug', 'lang', 'name')))
    );

    $this->widgetSchema->setNameFormat('jobeet_category_translation[%s]');

    $this->errorSchema = new sfValidatorErrorSchema($this->validatorSchema);

    parent::setup();
  }

  public function getModelName()
  {
    return 'JobeetCategoryTranslation';
  }

}