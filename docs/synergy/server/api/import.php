<?php

use Synergy\App\Synergy;
use Synergy\DB\AttachmentDAO;
use Synergy\Misc\HTTP;

require_once '../setup/conf.php';
if (Synergy::getSessionProvider()->sessionExists()) {
    switch ($_REQUEST['mode']) {
        case "attachment":
            $specificationId = intval($_REQUEST['owner']);
            $attachmentId = intval($_REQUEST['subject']);
            AttachmentDAO::updateAttachmentSpecification($specificationId, $attachmentId);
            HTTP::OK('');
            break;
        case "image":
            $caseId = intval($_REQUEST['owner']);
            $imageId = intval($_REQUEST['subject']);
            AttachmentDAO::updateImageCase($caseId, $imageId);
            HTTP::OK('');
            break;
        
        default:
            break;
    }
} else {
    HTTP::Unauthorized('unauthorized');
}
?>