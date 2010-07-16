<?php

/**
 * language actions.
 *
 * @package    jobeet
 * @subpackage language
 * @author     Your name here
 * @version    SVN: $Id: actions.class.php 12479 2008-10-31 10:54:40Z fabien $
 */
class sfJobeetLanguageActions extends sfActions
{
  public function executeChangeLanguage(sfWebRequest $request)
  {
    $form = new sfFormLanguage($this->getUser(), array('languages' => array('en', 'fr')));
    unset($form[$form->getCSRFFieldName()]);
    $form->process($request);
 
    return $this->redirect('@localized_homepage');
  }
}
