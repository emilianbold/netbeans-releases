<?php

/**
 * JobeetAffiliate form.
 *
 * @package    form
 * @subpackage JobeetAffiliate
 * @version    SVN: $Id: sfDoctrineFormTemplate.php 6174 2007-11-27 06:22:40Z fabien $
 */
abstract class PluginJobeetAffiliateForm extends BaseJobeetAffiliateForm
{
  public function setup()
  {
    parent::setup();

    unset($this['is_active'], $this['token'], $this['created_at'], $this['updated_at']);
 
    $this->widgetSchema['jobeet_categories_list']->setOption('expanded', true);
    $this->widgetSchema['jobeet_categories_list']->setLabel('Categories');
 
    $this->validatorSchema['jobeet_categories_list']->setOption('required', true);
 
    $this->widgetSchema['url']->setLabel('Your website URL');
    $this->widgetSchema['url']->setAttribute('size', 50);
 
    $this->widgetSchema['email']->setAttribute('size', 50);
 
    $this->validatorSchema['email'] = new sfValidatorEmail(array('required' => true));
  }
}