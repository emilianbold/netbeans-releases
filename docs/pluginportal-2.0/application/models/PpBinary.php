<?php

/**
 * Description of PpBinary
 *
 * @author janpirek
 */
class PpBinary extends BasePpBinary {

    const ZIP_MODULE_PREF = 'z';

    private $_basePath = null;
    public $_moduleAuthor = null;
    public $_showInClient = null;
    public $_summary = null;
    public $_license = null;
    public $infoAsXml;
    public $log = array();

    /**
     * Request verificationof plugin
     * - register verification, verification requests DB records, send emails
     * @param string $version
     * @param string $note
     * @return void
     */
    public function requestVerification($version, $note = '') {
        // 1) register verification
        $verification = new PpVerification();
        $verification->id = DbAutoincrement::getNewValue();
        $verification->status = 0;
        $verification->mailsent = 1;
        $verification->note = 'Requested on ' . date('Y-m-d H:i');
        $verification->plugin_pluginid = $this->Plugin->pluginid;
        $verification->binary_id = $this->binary_id;
        $verification->version = $version;
        $verification->save();
        DbAutoincrement::increaseCounter();
        PpLog::logMe($this->Plugin, NbSsoUser::getInstance()->getUsername(), 'Verification registered for version ' . $version . ', with id: ' . $verification->id);
        // 2) register verfication requests for all verifiers
        $verifiers = Doctrine_Query::create()->from('PpVerifier')->orderBy('userid')->execute();
        if ($verifiers->count() > 0) {
            foreach ($verifiers as $v) {
                // register each verificatino request and mail verifier
                $vRequest = new PpVerificationRequest();
                $vRequest->id = DbAutoincrement::getNewValue();
                $vRequest->requestdate = date('Y-m-d H:i:s');
                $vRequest->vote = '0';
                $vRequest->mailsent = '1';
                $vRequest->verification_id = $verification->id;
                $vRequest->verifier_id = $v->id;
                $vRequest->save();
                PpLog::logMe($this->Plugin, NbSsoUser::getInstance()->getUsername(), 'Verification request registered for verifier ' . $v->userid . ', with id: ' . $vRequest->id);
                DbAutoincrement::increaseCounter();
                // now save it to joint table as well
                $jt = new PpVerificationToRequest();
                $jt->verificationImpl_ID = $verification->id;
                $jt->verificationRequests_ID = $vRequest->id;
                $jt->save();
                // TODO - mail verifier
                $this->sendNotificationOnverificationRequestPerson($vRequest);
                // TODO - mail admin alias
            }
            // now mail to alias
            //$this->sendNotificationOnverificationRequestAlias($vRequest->Verification);
        }
    }

    /**
     * Send emailto teh verification alias
     * @param PpVerification $verificationRequest
     */
    public function sendNotificationOnverificationRequestAlias($verification) {
        $subject = PpPreference::preference()->verificationemailaliassubject;
        $subject = str_replace('${Plugin.plugin_name}', $this->Plugin->plugin_name, $subject);
        $body = PpPreference::preference()->verificationemailaliasbody;
        $body = str_replace('${Plugin.plugin_name}', $this->Plugin->plugin_name, $body);
        $body = str_replace('${VerificationRequest.requestDate}', date('Y-m-d'), $body);
        $body = str_replace('${Verification.version}', $verification->version, $body);
        $body = str_replace('${link.PluginDetailPage}', 'http://plugins.netbeans.org/plugin/' . $this->Plugin->publicid, $body);
        $body = str_replace('\n', "\n", $body);
        $mail = new Zend_Mail();
        $mail->setFrom('webmaster@netbeans.org', 'NetBeans Webmaster');
        $mail->addTo(PpPreference::preference()->verificationemailalias);
        $mail->setSubject($subject);
        $mail->setBodyText($body);
        $mail->send();
    }

    /**
     * Send mail to verifier
     * @param PpVerificationRequest $verificationRequest
     */
    public function sendNotificationOnverificationRequestPerson($verificationRequest) {
        $subject = PpPreference::preference()->verificationsubject;
        $subject = str_replace('${Plugin.plugin_name}', $this->Plugin->plugin_name, $subject);
        $body = PpPreference::preference()->verificationemailbody;
        $body = str_replace('${Plugin.plugin_name}', $this->Plugin->plugin_name, $body);
        $body = str_replace('${link.MyVerificationsPage}', 'http://plugins.netbeans.org/my-verifications/', $body);
        $body = str_replace('${Verification.version}', $verificationRequest->Verification->version, $body);
        $body = str_replace('${link.PluginDetailPage}', 'http://plugins.netbeans.org/plugin/' . $this->Plugin->publicid, $body);
        $body = str_replace('\n', "\n", $body);
        $mail = new Zend_Mail();
        $mail->setFrom('webmaster@netbeans.org', 'NetBeans Webmaster');
        $mail->addTo($verificationRequest->Verifier->userid . '@netbeans.org');
        $mail->setSubject($subject);
        $mail->setBodyText($body);
        $mail->send();
    }

    public function unpackAndLoadInfoForCatalogue($nbmPath, $tmpPath, $unzip) {
        $cmd = $unzip . ' ' . $nbmPath . $this->nbm_filename . ' -d ' . $tmpPath . $this->nbm_filename;
        if (file_exists($nbmPath . $this->nbm_filename)) {
            $this->logme(' * unpacking: ' . $cmd);
            exec(escapeshellcmd($cmd), $out);
            if (file_exists($tmpPath . $this->nbm_filename . '/Info/info.xml')) {
                // parse the info manifest
                $this->infoAsXml = simplexml_load_file($tmpPath . $this->nbm_filename . '/Info/info.xml');
                $this->logme(' * loaded info.xml');
                // cleanup
                exec(escapeshellcmd('rm -rf ' . $tmpPath . $this->nbm_filename));
            } else {
                $this->logme(' * <span class="red">ERROR</span> missing info.xml or unzip unsuccessfull');
            }
        } else {
            $this->logme(' * <span class="red">ERROR</span> missing plugin .nbm file ' . $nbmPath . $this->nbm_filename);
        }
    }

    public function getModuleChunkForUcXml($wwwNbmPath) {
        if (!empty($this->infoAsXml)) {
            $ret = '<module ';
            $attrs = $this->infoAsXml->attributes();
            $ret.='codenamebase="' . htmlspecialchars((string) $attrs['codenamebase']) . '"
        distribution="' . $wwwNbmPath . urlencode($this->nbm_filename) . '"
        downloadsize="' . htmlspecialchars($this->download_size) . '" homepage="' . htmlspecialchars($this->Plugin->home_page_url) . '"
        license="' . htmlspecialchars($this->plugin_id) . '"
        moduleauthor="' . ((!empty($attrs['moduleauthor'])) ? htmlspecialchars((string) $attrs['moduleauthor']) : htmlspecialchars($this->Plugin->author_userid)) . '" releasedate="' . htmlspecialchars((string) $attrs['releasedate']) . '"
        needsrestart="' . ((!empty($attrs['needsrestart'])) ? htmlspecialchars((string) $attrs['needsrestart']) : 'true') . '">';
            $manifAttr = $this->infoAsXml->manifest->attributes();
            $ret.="\n\t<manifest ";
            $hasAttr = false;
            foreach ($manifAttr as $k => $v) {
                if (!in_array($k, array('OpenIDE-Module-Build-Version'))) {
                    $ret.=$k . '="' . htmlspecialchars($v) . '" ';
                    if (trim($k) == 'AutoUpdate-Show-In-Client') {
                        $hasAttr = true;
                    }
                }
            }
            $this->logme(' * parameters extracted from info.xml');
            // we need to add AutoUpdate-Show-In-Client=true attribute if it's missing so UC xml is valid
            if (!$hasAttr) {
                $ret.='AutoUpdate-Show-In-Client="true" ';
                $this->logme(' * WARNING: manifest is missing AutoUpdate-Show-In-Client, adding it manually');
            }
            $ret.="></manifest>\n</module>\n";
            return $ret;
        }
    }

    public function getLicenseChunkForUcXml() {
        $ret = '';
        if ($this->infoAsXml->license) {
            $this->logme(' * license extracted from info.xml');
            $ret.='<license name="' . $this->plugin_id . '">' . htmlspecialchars($this->infoAsXml->license) . '</license>' . "\n";
        }
        return $ret;
    }

    /**
     * Unpack the zip module pack and prefix contained modules with the module id_ so we can find it
     * @param string $tmpPath
     * @param string $nbmPath
     */
    public function unpackModulePack($tmpPath, $nbmPath, $unzip) {
        $cmd = $unzip . ' ' . $nbmPath . $this->nbm_filename . ' -d ' . $tmpPath . $this->nbm_filename;
        exec(escapeshellcmd($cmd), $out);
        if (is_dir($tmpPath . $this->nbm_filename)) {
            // now for every nbm move it to $nbmPath and prefix it with this module ID_
            $files = scandir($tmpPath . $this->nbm_filename);
            if ($files) {
                foreach ($files as $f) {
                    if ($f != '.' && $f != '..') {
                        // move it
                        PpLog::logMe($this->Plugin, NbSsoUser::getInstance()->getUsername(), 'Verified module is .zip, extracting the contained file to the download folder: ' . $this->binary_id . '_' . $f);
                        @copy($tmpPath . $this->nbm_filename . '/' . $f, $nbmPath . self::ZIP_MODULE_PREF . $this->binary_id . '_' . $f);
                        chmod($nbmPath . self::ZIP_MODULE_PREF . $this->binary_id . '_' . $f, 0755);
                    }
                }
            }
        }
        // cleanup
        exec(escapeshellcmd('rm -rf ' . $tmpPath . $this->nbm_filename));
    }

    private function logme($msg) {
        $this->log[] = $msg;
    }

}

?>
