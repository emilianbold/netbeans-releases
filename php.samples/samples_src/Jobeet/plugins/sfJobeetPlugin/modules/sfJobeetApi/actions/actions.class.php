<?php

/**
 * api actions.
 *
 * @package    jobeet
 * @subpackage api
 * @author     Your name here
 * @version    SVN: $Id: actions.class.php 12479 2008-10-31 10:54:40Z fabien $
 */
class sfJobeetApiActions extends sfActions
{
  public function executeList(sfWebRequest $request)
  {
    $this->jobs = array();
    foreach ($this->getRoute()->getObjects() as $job)
    {
      $this->jobs[$this->generateUrl('job_show_user', $job, true)] = $job->asArray($request->getHost());
    }
 
    switch ($request->getRequestFormat())
    {
      case 'yaml':
        $this->setLayout(false);
        $this->getResponse()->setContentType('text/yaml');
        break;
    }
  }
}
