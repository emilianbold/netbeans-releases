<?php
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */

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