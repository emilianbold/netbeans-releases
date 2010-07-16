<?php
class JobeetTestFunctional extends sfTestFunctional
{
  public function createJob($values = array(), $publish = false)
  {
    $this->
      get('/en/job/new')->
      click('Preview your job', array('job' => array_merge(array(
        'company'      => 'Sensio Labs',
        'url'          => 'http://www.sensio.com/',
        'position'     => 'Developer',
        'location'     => 'Atlanta, USA',
        'description'  => 'You will work with symfony to develop websites for our customers.',
        'how_to_apply' => 'Send me an email',
        'email'        => 'for.a.job@example.com',
        'is_public'    => false,
      ), $values)))->
      followRedirect()
    ;
 
    if ($publish)
    {
      $this->
        click('Publish', array(), array('method' => 'put', '_with_csrf' => true))->
        followRedirect()
      ;
    }
 
    return $this;
  }
 
  public function getJobByPosition($position)
  {
    $q = Doctrine_Query::create()
      ->from('JobeetJob j')
      ->where('j.position = ?', $position);
 
    return $q->fetchOne();
  }

  public function loadData()
  {
    Doctrine::loadData(sfConfig::get('sf_test_dir').'/fixtures');
 
    return $this;
  }
 
  public function getMostRecentProgrammingJob()
  {
    $q = Doctrine_Query::create()
      ->select('j.*')
      ->from('JobeetJob j')
      ->leftJoin('j.JobeetCategory c')
      ->leftJoin('c.Translation t')
      ->where('t.slug = ?', 'programming');
    $q = Doctrine::getTable('JobeetJob')->addActiveJobsQuery($q);
 
    return $q->fetchOne();
  }
 
  public function getExpiredJob()
  {
    $q = Doctrine_Query::create()
      ->from('JobeetJob j')
      ->where('j.expires_at < ?', date('Y-m-d', time()));
 
    return $q->fetchOne();
  }
}