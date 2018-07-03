<?php
namespace Synergy\Controller;

use Exception;
use Synergy\App\Synergy;
use Synergy\DB\AttachmentDAO;
use Synergy\Misc\HTTP;

/**
 * Description of AttachmentCtrl
 *
 * @author lada
 */
class AttachmentCtrl {

    private $attachmentDao;

    function __construct() {
        $this->attachmentDao = new AttachmentDAO();
    }

    /**
     * Returns specification attachment's path with given ID
     * @param int $id attachment ID
     * @return string path to the extension on local disc
     */
    public function getSpecificationAttachment($id) {
        return $this->attachmentDao->getSpecificationAttachment($id);
    }

    /**
     * Removes specification attachment and if the file is not used in other specification, deletes file as well
     * @param int $id specification attachment ID
     * @return boolean true on success
     */
    public function deleteSpecificationAttachment($id) {
        $path = $this->attachmentDao->getSpecificationAttachment($id);
        if (strlen($path) < 1)
            return false;
        $r = $this->attachmentDao->deleteSpecificationAttachment($id);
        Mediator::emit("specificationUpdated", $this->getSpecificationId($id));
        $count = $this->attachmentDao->countAttachmnets($path);
        if ($count < 1) {
            $this->deleteFile($path);
        }
        return $r;
    }
    
    public function getSpecificationId($attachmentId){
        return $this->attachmentDao->getSpeficiationId($attachmentId);
    }

    /**
     * Creates a new specification attachment, aka saves file and creates DB record
     * @param data $data data uploaded to server (file content)
     * @param string $fileName name of the uploaded file
     * @param int $id specification id
     * @return int ID of new attachment
     */
    public function saveSpecificationAttachment($data, $fileName, $id) {
        try {
            date_default_timezone_set('UTC');
            $timestamp = strtotime(date("Y-m-d H:i:s"));
            $fp = fopen(ATTACHMENT_PATH . $timestamp . "_" . $fileName, 'w');
            fwrite($fp, $data);
            fclose($fp);
        } catch (Exception $e) {
            $er = print_r($e, true);
            $logger = Synergy::getProvider("logger");
            $logger::log($er);
            HTTP::InternalServerError("Unable to write to file");
            return;
        }
        // save record to DB
        Mediator::emit("specificationUpdated", $id);
        return $this->attachmentDao->createSpecificationAttachment($id, $timestamp . "_" . $fileName);
    }

    /**
     * Creates a new run attachment, aka saves file and creates DB record
     * @param data $data data uploaded to server (file content)
     * @param string $fileName name of the uploaded file
     * @param int $id run id
     * @return boolean true on success
     */
    public function saveRunAttachment($data, $fileName, $id) {
        try {
            date_default_timezone_set('UTC');
            $timestamp = strtotime(date("Y-m-d H:i:s"));
            $fp = fopen(ATTACHMENT_PATH . $timestamp . "_" . $fileName, 'w');
            fwrite($fp, $data);
            fclose($fp);
        } catch (Exception $e) {
            $er = print_r($e, true);
            $logger = Synergy::getProvider("logger");
            $logger::log($er);
            HTTP::InternalServerError("Unable to write to file");
            return;
        }


        // save record to DB
        return $this->attachmentDao->createRunAttachment($id, $timestamp . "_" . $fileName);
    }

    /**
     * Removes file from disk
     * @param string $path full path to file
     */
    public function deleteFile($path) {
        unlink($path);
    }

    /**
     * Returns run attachment's path with given ID
     * @param int $id attachment ID
     * @return string path to the extension on local disk
     */
    public function getRunAttachment($id) {
        return $this->attachmentDao->getRunAttachment($id);
    }

    /**
     * Removes run attachment and if the file is not used in other run, deletes file as well
     * @param int $id attachment ID
     * @return boolean true on success
     */
    public function deleteRunAttachment($id) {
        $path = $this->attachmentDao->getRunAttachment($id);
        if (strlen($path) < 1)
            return false;
        $r = $this->attachmentDao->deleteRunAttachment($id);
        $count = $this->attachmentDao->countRunAttachmnets($path);
        if ($count < 1) {
            $this->deleteFile($path);
        }
        return $r;
    }

}

?>
