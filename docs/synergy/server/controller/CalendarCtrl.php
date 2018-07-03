<?php
namespace Synergy\Controller;

use Exception;
use Synergy\Interfaces\Observer;
use Synergy\Model\Exception\GeneralException;
use Synergy\Model\TestRun;

/**
 * Description of CalendarCtrl
 *
 * @author vriha
 */
class CalendarCtrl implements Observer {

    public $isFeedOK;
    public static $feedFile = "ical.ics";
    private static $listening = array('testRunDeleted', 'testRunEdited', 'testRunCreated');

    function __construct() {
        $this->isFeedOK = false;
    }

    /**
     * Returns test run in form of a vevent
     * @param TestRun $run
     */
    private function addEvent($run) {
        $feed = "BEGIN:VEVENT\nUID:TestRun_" . $run->id . "@" . $_SERVER['SERVER_NAME'] . "\n";
        $feed .="DTSTAMP:" . date('Ymd\THis') . "\n";
        $feed .="CREATED:" . date('Ymd\THis') . "\n";
        $feed .="DTSTART:" . date('Ymd', strtotime($run->start)) . "\n";
        $feed .="DTEND:" . date('Ymd', strtotime($run->end)) . "\n";
        $feed .="DESCRIPTION:" . $run->desc . "\n";
        $feed .="SUMMARY:" . $run->title . "\n";
        $feed .="END:VEVENT\n";
        return $feed;
    }

    /**
     * Writes feed to cache file
     * @param String $feed
     * @throws GeneralException if the feed cannot be written to file
     */
    private function writeFeed($feed) {
        try {
            $fp = fopen(CACHE . CalendarCtrl::$feedFile, 'w');
            fwrite($fp, $feed);
            fclose($fp);
        } catch (Exception $exc) {
            throw new GeneralException("Problem saving calendar feed", $exc->getTraceAsString(), "");
        }
    }

    /**
     * Deletes cache file from cache dir
     */
    private function deleteCachedFeed() {
        unlink(CACHE . CalendarCtrl::$feedFile);
    }

    /**
     * Creates a new feed of all test runs in interval (now-2months; now + 2months)
     * @return String ical feed
     */
    private function createFeed() {
        $runCtrl = new RunCtrl();
        $feed = $this->initFeed();
        date_default_timezone_set('UTC');
        $runs = $runCtrl->getRunsByDate($startDate = date('Y-m-d H:i:s', strtotime("-2 months")), $startDate = date('Y-m-d H:i:s', strtotime("+2 months")));
        foreach ($runs as $run) {
            $feed.=$this->addEvent($run);
        }

        return $this->endFeed($feed);
    }

    /**
     * Listens on Test Run modifications and update static cached feed
     * @param String $name message name
     * @param mixed $data data 
     */
    public static function on($name, $data) {
        if (in_array($name, CalendarCtrl::$listening)) {
            $instance = new self();
            $instance->deleteCachedFeed();
            $feed = $instance->createFeed();
            $instance->writeFeed($feed);
        }
    }

    /**
     * Returns first lines of ics
     * @return string
     */
    private function initFeed() {
        return "BEGIN:VCALENDAR\nVERSION:2.0\nPRODID:-//hacksw/handcal//NONSGML v1.0//EN\nCALSCALE:GREGORIAN\nMETHOD:PUBLISH\n";
    }

    /**
     * Finishes ics feed
     * @param String $feed current feed to be finished
     * @return String feed terminated with END:VCALENDAR
     */
    private function endFeed($feed) {
        return $feed . "END:VCALENDAR";
    }

    /**
     * Returns events
     * @return TestRun[]
     */
    public function getEvents() {
        date_default_timezone_set('UTC');
        $runCtrl = new RunCtrl();
        $runs = $runCtrl->getRunsByDate($startDate = date('Y-m-d H:i:s', strtotime("-2 months")), $startDate = date('Y-m-d H:i:s', strtotime("+2 months")));
        foreach ($runs as $run) {
            $run->start = date('Ymd', strtotime($run->start));
            $run->end = date('Ymd', strtotime($run->end));
        }
        return $runs;
    }

}

?>
