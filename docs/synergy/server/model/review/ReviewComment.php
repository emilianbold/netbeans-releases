<?php

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

namespace Synergy\Model\Review;

/**
 * Description of ReviewComment
 *
 * @author vriha
 */
class ReviewComment {

    public $authorUsername;
    public $authorDisplayName;
    public $text;
    public $id;
    public $elements;
    private $assignmentId;
    private $hash;

    function __construct($authorUsername, $authorDisplayName, $text, $id, $elements) {
        $this->authorUsername = $authorUsername;
        $this->authorDisplayName = $authorDisplayName;
        $this->text = $text;
        $this->id = $id;
        $this->elements = $elements;
    }

    public function getAssignmentId() {
        return $this->assignmentId;
    }

    public function setAssignmentId($assignmentId) {
        $this->assignmentId = $assignmentId;
    }

    public function getHash() {
        return $this->hash;
    }

    public function setHash($hash) {
        $this->hash = $hash;
    }

}
