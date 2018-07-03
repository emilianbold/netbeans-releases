<?php

namespace Synergy\Model\Label\Rest;

use Synergy\Model\Testcase\Rest\CaseListItemResource;


/**
 * Description of LabelSearchResource
 *
 * @author vriha
 */
class LabelSearchResource {
   public $label;
   public $nextUrl;
   public $prevUrl;
   public $url;
   public $cases;
   
   
   public static function create($result) {
       $i = new LabelSearchResource();
       $i->label = $result->label;
       $i->nextUrl = $result->nextUrl;
       $i->prevUrl = $result->prevUrl;
       $i->url = $result->url;
       $i->cases = CaseListItemResource::createFromCases($result->cases);
       return $i;
   }
   
}
