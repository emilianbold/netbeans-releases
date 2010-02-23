<?php
class JobeetCleanupTask extends sfBaseTask
{
  protected function configure()
  {
    $this->addOptions(array(
      new sfCommandOption('application', null, sfCommandOption::PARAMETER_REQUIRED, 'The application', 'frontend'),
      new sfCommandOption('env', null, sfCommandOption::PARAMETER_REQUIRED, 'The environement', 'prod'),
      new sfCommandOption('days', null, sfCommandOption::PARAMETER_REQUIRED, '', 90),
    ));
 
    $this->namespace = 'jobeet';
    $this->name = 'cleanup';
    $this->briefDescription = 'Cleanup Jobeet database';
 
    $this->detailedDescription = <<<EOF
The [jobeet:cleanup|INFO] task cleans up the Jobeet database:
 
  [./symfony jobeet:cleanup --env=prod --days=90|INFO]
EOF;
  }
 
  protected function execute($arguments = array(), $options = array())
  {
    $databaseManager = new sfDatabaseManager($this->configuration);

    // cleanup Lucene index
    $index = Doctrine::getTable('JobeetJob')->getLuceneIndex();

    $q = Doctrine_Query::create()
      ->from('JobeetJob j')
      ->where('j.expires_at < ?', date('Y-m-d'));

    $jobs = $q->execute();
    foreach ($jobs as $job)
    {
      if ($hit = $index->find('pk:'.$job->getId()))
      {
        $hit->delete();
      }
    }

    $index->optimize();

    $this->logSection('lucene', 'Cleaned up and optimized the job index');

    // Remove stale jobs
    $nb = Doctrine::getTable('JobeetJob')->cleanup($options['days']);

    $this->logSection('doctrine', sprintf('Removed %d stale jobs', $nb));
  }
}