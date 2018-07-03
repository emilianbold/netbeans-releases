<?php

namespace Synergy\Model;

use Synergy\Misc\Util;

/**
 * Description of Job
 *
 * @author vriha
 */
class Job {

    public $id;
    public $specificationId;
    public $jobUrl;
    const SUFIX_COMPLETED = "lastCompletedBuild/api/json?jsonp=";

    function __construct($id, $specificationId, $jobUrl) {
        $this->id = intval($id);
        $this->specificationId = intval($specificationId);
        $this->jobUrl = $jobUrl;
        if (!Util::endsWith($jobUrl, Job::SUFIX_COMPLETED)) {
            if (!Util::endsWith($jobUrl, "/")) {
                $this->jobUrl = $jobUrl . "/" . Job::SUFIX_COMPLETED;
            } else {
                $this->jobUrl = $jobUrl . Job::SUFIX_COMPLETED;
            }
        }
    }

}

?>