<?php

/**
 * affiliate module configuration.
 *
 * @package    jobeet
 * @subpackage affiliate
 * @author     Your name here
 * @version    SVN: $Id: configuration.php 12474 2008-10-31 10:41:27Z fabien $
 */
class affiliateGeneratorConfiguration extends BaseAffiliateGeneratorConfiguration
{
  public function getFilterDefaults()
  {
    return array('is_active' => '0');
  }
}
