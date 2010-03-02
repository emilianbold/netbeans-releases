<?php

/**
 * job actions.
 *
 * @package    jobeet
 * @subpackage job
 * @author     Your name here
 * @version    SVN: $Id: actions.class.php 12474 2008-10-31 10:41:27Z fabien $
 */
class sfJobeetJobActions extends sfActions
{
  public function executeSearch(sfWebRequest $request)
  {
    if (!$query = $request->getParameter('query'))
    {
      return $this->forward('sfJobeetJob', 'index');
    }

    $this->jobs = Doctrine::getTable('JobeetJob')->getForLuceneQuery($query);

    if ($request->isXmlHttpRequest())
    {
      if ('*' == $query || !$this->jobs)
      {
        return $this->renderText('No results.');
      }
      else
      {
        return $this->renderPartial('sfJObeetJob/list', array('jobs' => $this->jobs));
      }
    }
  }

  public function executeExtend(sfWebRequest $request)
  {
    $request->checkCSRFProtection();

    $job = $this->getRoute()->getObject();
    $this->forward404Unless($job->extend());

    $this->getUser()->setFlash('notice', sprintf('Your job validity has been extend until %s.', date('m/d/Y', strtotime($job->getExpiresAt()))));

    $this->redirect($this->generateUrl('job_show_user', $job));
  }

  public function executePublish(sfWebRequest $request)
  {
    $request->checkCSRFProtection();

    $job = $this->getRoute()->getObject();
    $job->publish();

    if ($cache = $this->getContext()->getViewCacheManager())
    {
      $cache->remove('sfJobeetJob/index?sf_culture=*');
      $cache->remove('sfJobeetCategory/show?id='.$job->getJobeetCategory()->getId());
    }

    $this->getUser()->setFlash('notice', sprintf('Your job is now online for %s days.', sfConfig::get('app_active_days')));

    $this->redirect($this->generateUrl('job_show_user', $job));
  }

  public function executeIndex(sfWebRequest $request)
  {
    if (!$request->getParameter('sf_culture'))
    {
      if ($this->getUser()->isFirstRequest())
      {
        $culture = $request->getPreferredCulture(array('en', 'fr'));
        $this->getUser()->setCulture($culture);
        $this->getUser()->isFirstRequest(false);
      }
      else
      {
        $culture = $this->getUser()->getCulture();
      }

      $this->redirect('@localized_homepage');
    }

    $this->categories = Doctrine::getTable('JobeetCategory')->getWithJobs();
  }

  public function executeShow(sfWebRequest $request)
  {
    $this->job = $this->getRoute()->getObject();

    $this->getUser()->addJobToHistory($this->job);
  }

  public function executeNew(sfWebRequest $request)
  {
    $job = new JobeetJob();
    $job->setType('full-time');

    $this->form = new JobeetJobForm($job);
  }

  public function executeCreate(sfWebRequest $request)
  {
    $this->form = new JobeetJobForm();
    $this->processForm($request, $this->form);
    $this->setTemplate('new');
  }

  public function executeEdit(sfWebRequest $request)
  {
    $job = $this->getRoute()->getObject();
    $this->forward404If($job->getIsActivated());

    $this->form = new JobeetJobForm($job);
  }

  public function executeUpdate(sfWebRequest $request)
  {
    $this->form = new JobeetJobForm($this->getRoute()->getObject());
    $this->processForm($request, $this->form);
    $this->setTemplate('edit');
  }

  public function executeDelete(sfWebRequest $request)
  {
    $request->checkCSRFProtection();

    $job = $this->getRoute()->getObject();
    $job->delete();

    $this->redirect('sfJobeetJob/index');
  }

  protected function processForm(sfWebRequest $request, sfForm $form)
  {
    $form->bind(
      $request->getParameter($form->getName()),
      $request->getFiles($form->getName())
    );

    if ($form->isValid())
    {
      $job = $form->save();

      $this->redirect($this->generateUrl('job_show', $job));
    }
  }
}