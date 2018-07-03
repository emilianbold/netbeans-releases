<?php

namespace Synergy\Model\Specification\Rest;

use Synergy\Model\Suite\Rest\SuiteResource;
use Synergy\Model\Suite\Rest\SuiteSnippetResource;

/**
 * Description of SpecificationResource
 *
 * @author vriha
 */
class SpecificationResource {

    public $id;
    public $desc;
    public $title;
    public $simpleName;
    public $versionId;
    public $controls;
    public $authorId;
    public $author;
    public $authorName;
    public $ownerId;
    public $owner;
    public $ownerName;
    public $version;
    public $similar;
    public $isUsed;
    public $testSuites;
    public $attachments;
    public $estimation;
    public $url;
    public $isFavorite;
    public $lastUpdated;
    public $ext;
    public $userIsRelated;

    /**
     * 
     * @param type $specification
     * @param boolean $detailed if true, suites also contain full information (including product/component, description etc.) and test cases contain steps/resolution/time, otherwise only case's title/labels/actions
     * @return \Synergy\Model\Specification\Rest\SpecificationResource
     */
    public static function createFromSpecification($specification, $detailed) {
        $i = new SpecificationResource();
        $i->id = $specification->id;
        $i->desc = $specification->desc;
        $i->title = $specification->title;
        $i->simpleName = $specification->simpleName;
        $i->versionId = $specification->versionId;
        $i->controls = $specification->controls;
        $i->authorId = $specification->authorId;
        $i->author = $specification->author;
        $i->authorName = $specification->authorName;
        $i->ownerId = $specification->ownerId;
        $i->owner = $specification->owner;
        $i->ownerName = $specification->ownerName;
        $i->version = $specification->version;
        $i->similar = $specification->similar;
        $i->isUsed = $specification->isUsed;
        $i->testSuites = ($detailed) ? SuiteResource::createFromSuites($specification->testSuites, $detailed) : SuiteSnippetResource::createFromSuites($specification->testSuites);
        $i->attachments = SpecificationAttachmentResource::createFromAttachments($specification->attachments);
        $i->estimation = $specification->estimation;
        $i->url = $specification->url;
        $i->isFavorite = $specification->isFavorite;
        $i->lastUpdated = $specification->lastUpdated;
        $i->ext = $specification->ext;
        $i->userIsRelated = $specification->userIsRelated;

        return $i;
    }

}
