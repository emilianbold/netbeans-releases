<?php
class sfJobeetLanguageComponents extends sfComponents
{
  public function executeLanguage(sfWebRequest $request)
  {
    $this->form = new sfFormLanguage($this->getUser(), array('languages' => array('en', 'fr')));
    unset($this->form[$this->form->getCSRFFieldName()]);
  }
}