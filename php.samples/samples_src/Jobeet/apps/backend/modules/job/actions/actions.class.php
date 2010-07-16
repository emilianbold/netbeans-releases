<?php

require_once dirname(__FILE__).'/../lib/jobGeneratorConfiguration.class.php';
require_once dirname(__FILE__).'/../lib/jobGeneratorHelper.class.php';

/**
 * job actions.
 *
 * @package    jobeet
 * @subpackage job
 * @author     Your name here
 * @version    SVN: $Id: actions.class.php 12474 2008-10-31 10:41:27Z fabien $
 */
class jobActions extends autoJobActions
{
  public function executeListDeleteNeverActivated(sfWebRequest $request)
  {
    $nb = Doctrine::getTable('JobeetJob')->cleanup(60);
 
    if ($nb)
    {
      $this->getUser()->setFlash('notice', sprintf('%d never activated jobs have been deleted successfully.', $nb));
    }
    else
    {
      $this->getUser()->setFlash('notice', 'No job to delete.');
    }
 
    $this->redirect('@jobeet_job');
  }

  public function executeListExtend(sfWebRequest $request)
  {
    $job = $this->getRoute()->getObject();
    $job->extend(true);
 
    $this->getUser()->setFlash('notice', 'The selected jobs have been extended successfully.');
 
    $this->redirect('@jobeet_job');
  }

  public function executeBatchExtend(sfWebRequest $request)
  {
    $ids = $request->getParameter('ids');
 
    $q = Doctrine_Query::create()
      ->from('JobeetJob j')
      ->whereIn('j.id', $ids);
 
    foreach ($q->execute() as $job)
    {
      $job->extend(true);
    }
 
    $this->getUser()->setFlash('notice', 'The selected jobs have been extended successfully.');
 
    $this->redirect('@jobeet_job');
  }
}
