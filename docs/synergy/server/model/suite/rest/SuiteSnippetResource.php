<?php

namespace Synergy\Model\Suite\Rest;

/**
 * Description of SuiteSnippetResource
 *
 * @author vriha
 */
class SuiteSnippetResource {

    public $component;
    public $controls;
    public $desc;
    public $estimation;
    public $ext;
    public $id;
    public $isUsed;
    public $order;
    public $product;
    public $specificationId;
    public $specificationTitle;
    public $title;
    public $version;
    public $url;

    function __construct($suite) {
        $this->component = $suite->component;
        $this->controls = $suite->controls;
        $this->desc = $suite->desc;
        $this->estimation = $suite->estimation;
        $this->ext = $suite->ext;
        $this->id = $suite->id;
        $this->isUsed = $suite->isUsed;
        $this->order = $suite->order;
        $this->product = $suite->product;
        $this->specificationId = $suite->specificationId;
        $this->specificationTitle = $suite->specificationTitle;
        $this->title = $suite->title;
        $this->version = $suite->version;
        $this->url = $suite->url;
    }

    public static function createFromSuite($suite) {
        $i = new SuiteSnippetResource($suite);
        return $i;
    }

    public static function createFromSuites($suites) {
        $list = array();
        foreach ($suites as $suite) {
            array_push($list, SuiteSnippetResource::createFromSuite($suite));
        }
        return $list;
    }

}
