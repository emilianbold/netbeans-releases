<?php

namespace Synergy\Controller;

use Synergy\DB\CommentsDAO;

/**
 * Description of CommentsCtrl
 *
 * @author vriha
 */
class CommentsCtrl {

    private $commentsDao;
    
    function __construct() {
        $this->commentsDao = new CommentsDAO();
    }

    /**
     * Returns all comment types
     * @return CommentType[]
     */
    public function getCommentTypes() {
        return $this->commentsDao->getCommentTypes();
    }
}
