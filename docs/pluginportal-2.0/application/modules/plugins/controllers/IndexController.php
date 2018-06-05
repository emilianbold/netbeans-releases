<?php

class IndexController extends Zend_Controller_Action {

    public function init() {
        /* Initialize action controller here */
        $conf = $this->getInvokeArg('bootstrap')->getOptions();
        $this->config = $conf[$this->_request->getModuleName()];
        $this->_helper->layout->setLayout('plugins_public_ui');
        $this->view->config = $this->config;
        // SSO
        $this->_ssoUser = NbSsoUser::getInstance($this->config['sso']['cookieName'], $this->config['sso']['ignoredAgents'], $this->config['sso']['validationServiceUrl'], $this->config['sso']['httpClientClass'], $this->config['sso']['refreshSec'], $this->config['sso']['startLog'], $this->config['sso']['proxyHost'], $this->config['sso']['proxyPort']);
        $this->view->ssoUser = $this->_ssoUser;
        //echo '<!--'.$this->_ssoUser->getLog().'-->';
        $this->reCaptchaSecret = '6LfXnh8TAAAAADTX6YVVKLy5hMxbNvI3-PPlLnWO';
        
        $this->frontendOptions = array(
            'lifetime' => $this->config['cache']['lifetime'], // cache lifetim
            'automatic_serialization' => true
        );

        $this->backendOptions = array(
            'cache_dir' => $this->config['cache']['storage'], // Directory where to put the cache files
            'file_name_prefix' => 'zendcache'
        );
    }

    public function captchaAction() {
        // -------------------- EDIT THESE ----------------- //
        $images = array(
            'house' => 'https://netbeans.org/images_www/captchaimages/01.png',
            'key' => 'https://netbeans.org/images_www/captchaimages/04.png',
            'flag' => 'https://netbeans.org/images_www/captchaimages/06.png',
            'clock' => 'https://netbeans.org/images_www/captchaimages/15.png',
            'bug' => 'https://netbeans.org/images_www/captchaimages/16.png',
            'pen' => 'https://netbeans.org/images_www/captchaimages/19.png',
            'light bulb' => 'https://netbeans.org/images_www/captchaimages/21.png',
            'musical note' => 'https://netbeans.org/images_www/captchaimages/40.png',
            'heart' => 'https://netbeans.org/images_www/captchaimages/43.png',
            'world' => 'https://netbeans.org/images_www/captchaimages/99.png'
        );
        // ------------------- STOP EDITING ---------------- //

        $_SESSION['simpleCaptchaAnswer'] = null;
        $_SESSION['simpleCaptchaTimestamp'] = time();
        $SALT = "o^Gj" . $_SESSION['simpleCaptchaTimestamp'] . "7%8W";
        $resp = array();

        header("Content-Type: application/json");

        if (!isset($images) || !is_array($images) || sizeof($images) < 3) {
            $resp['error'] = "There aren\'t enough images!";
            echo json_encode($resp);
            exit;
        }

        if (isset($_POST['numImages']) && strlen($_POST['numImages']) > 0) {
            $numImages = intval($_POST['numImages']);
        } else if (isset($_GET['numImages']) && strlen($_GET['numImages']) > 0) {
            $numImages = intval($_GET['numImages']);
        }
        $numImages = ($numImages > 0) ? $numImages : 5;
        $size = sizeof($images);
        $num = min(array($size, $numImages));

        $keys = array_keys($images);
        $used = array();
        mt_srand(((float) microtime() * 587) / 33);
        for ($i = 0; $i < $num; ++$i) {
            $r = rand(0, $size - 1);
            while (array_search($keys[$r], $used) !== false) {
                $r = rand(0, $size - 1);
            }
            array_push($used, $keys[$r]);
        }
        $selectText = $used[rand(0, $num - 1)];
        $_SESSION['simpleCaptchaAnswer'] = sha1($selectText . $SALT);

        $resp['text'] = '' . $selectText;
        $resp['images'] = array();

        shuffle($used);
        for ($i = 0; $i < sizeof($used); ++$i) {
            array_push($resp['images'], array(
                'hash' => sha1($used[$i] . $SALT),
                'file' => $images[$used[$i]]
            ));
        }
        echo json_encode($resp);
        exit;
    }

    public function licenseListAction() {
        $plugins = Doctrine_Query::create()->from('PpPlugin p')
                        ->innerJoin('p.Binaries b')->innerJoin('b.Version vers')->innerJoin('b.Verification verif')
                        ->where('p.published=1')->andWhere('verif.status=1')
                        ->useResultCache(true)->execute(null, Doctrine_Core::HYDRATE_ARRAY);
        $this->view->plugins = $plugins;
        $this->initPurifier();
    }

    public function myVerificationsAction() {
        if (NbSsoUser::getInstance()->isAuthenticated() && NbSsoUser::getInstance()->isVerifier()) {
            // save vote
            if ($this->_request->isPost()) {
                if ($this->_getParam('requestId')) {
                    // do not allow NOGO+empty comment
                    if ($this->_getParam('value') == -1 && !$this->_getParam('comment')) {
                        $this->setupFlash('Your justification for NoGo verdict is required.', 'error');
                    } else {
                        $req = Doctrine_Core::getTable('PpVerificationRequest')->find($this->_getParam('requestId'));
                        // check that user voting is really one asked for verification
                        if ($req && $req->Verifier->userid == NbSsoUser::getInstance()->getUsername()) {
                            $req->vote = (string) $this->_getParam('value');
                            $req->comment = strip_tags($this->_getParam('comment'));
                            $req->votedate = date('Y-m-d H:i:s');
                            $req->save();
                            $this->setupFlash('You just voted', 'success');

                            // Calculate if verification is overall GO/NOGO
                            // rule: PASSED =  0 NOGO and >=2 of votes are GO
                            $numrequests = Doctrine_Query::create()->select('COUNT(verification_id) as reqs')
                                    ->from('PpVerificationRequest')->where('verification_id=?', $req->verification_id)
                                    ->fetchOne(null, Doctrine_Core::HYDRATE_SINGLE_SCALAR);
                            $numGoes = Doctrine_Query::create()->select('COUNT(verification_id) as reqs')
                                    ->from('PpVerificationRequest')
                                    ->where('verification_id=?', $req->verification_id)->andWhere('vote=?', '1')
                                    ->fetchOne(null, Doctrine_Core::HYDRATE_SINGLE_SCALAR);
                            $numNogoes = Doctrine_Query::create()->select('COUNT(verification_id) as reqs')
                                    ->from('PpVerificationRequest')
                                    ->where('verification_id=?', $req->verification_id)->andWhere('vote=?', '-1')
                                    ->fetchOne(null, Doctrine_Core::HYDRATE_SINGLE_SCALAR);
                            if ($numNogoes > 0) {
                                $req->Verification->status = '-1';
                                $req->save();
                                $detail = 'There is ' . $numNogoes . ' NoGo casted threfore overal NoGo';
                                // regenerate catalogue
                                $catal = new CatalogueAntImplementation();
                                $catal->generateCatalogue($req->Verification->version, $this->config['content']['filesystem_nbm_path'], $this->config['java']['tmpDir'], $this->config['java']['home'], $this->config['java']['ant'], $this->config['java']['buildScriptPath']);
                                $catal->saveXml($this->config['content']['filesystem_catalogue_path']);
                                // create BZ issue for this if user just gave NOGO, but only if such issue does not exists yet
                                if ($this->_getParam('value') == -1 && empty($req->Verification->issue)) {
                                    $issueNumber = BugzillaIssue::submit($req->Verification->Plugin->plugin_name, NbSsoUser::getInstance()->getUsername(), $req->Verification->Plugin->author_userid, $this->_getParam('comment'), $req->Verification->version, $req->Verification->Plugin->publicid, $this->config['sso']['proxyHost'], $this->config['sso']['proxyPort']);
                                    if (!empty($issueNumber) && is_numeric($issueNumber)) {
                                        // also link the issue from the verification note
                                        $req->comment.= "\n\nhttps://netbeans.org/bugzilla/show_bug.cgi?id=" . $issueNumber;
                                        $req->Verification->issue = $issueNumber;
                                        $req->save();
                                    }
                                } elseif ($this->_getParam('value') == -1 && !empty($req->Verification->issue)) {
                                    // update the BZ issue with comment from user if there was NOGO before and user just adds another one
                                    $ret = BugzillaIssue::comment($req->Verification->Plugin->plugin_name, NbSsoUser::getInstance()->getUsername(), $req->Verification->Plugin->author_userid, $this->_getParam('comment'), $req->Verification->version, $req->Verification->Plugin->publicid, $req->Verification->issue, $this->config['sso']['proxyHost'], $this->config['sso']['proxyPort']);
                                }
                            } elseif ($numNogoes == 0 && $numGoes < 2) {
                                $req->Verification->status = '0';
                                $req->save();
                                $detail = '0 NoGos, ' . $numGoes . ' Gos of ' . $numrequests . ' requests, therefore overal Undecided';
                            } elseif ($numNogoes == 0 && $numGoes >= 2) {
                                $req->Verification->status = '1';
                                $req->save();
                                $detail = '0 NoGos, ' . $numGoes . ' Gos of ' . $numrequests . ' requests, therefore overal GO';
                                // drop verifications for prev versions of this binary, so it does not show up any more on UC
                                $verif=$req->Verification;
                                Doctrine_Query::create()->delete('PpVerificationRequest')->where('verification_id IN (select id from verificationimpl where version=? and plugin_pluginid='.$verif->plugin_pluginid.' AND id<>'.$verif->id.')',$verif->version)->execute();
                                Doctrine_Query::create()->delete('PpVerificationToRequest')->where('verificationImpl_ID IN (select id from verificationimpl where version=? and plugin_pluginid='.$verif->plugin_pluginid.' AND id<>'.$verif->id.')',$verif->version)->execute();
                                Doctrine_Query::create()->delete('PpVerification')->where('version=?', $verif->version)->andWhere('plugin_pluginid=? AND id<>'.$verif->id,$verif->plugin_pluginid)->execute();

                                // extract zip module and prefix it with binary_id
                                if (strstr($req->Verification->Binary->nbm_filename, '.zip')) {
                                    $req->Verification->Binary->unpackModulePack($this->config['content']['tmp_location_path'], $this->config['content']['filesystem_nbm_path'], $this->config['system']['unzip_executable']);
                                }
                                // regenerate catalogue
                                $catal = new CatalogueAntImplementation();
                                $catal->generateCatalogue($req->Verification->version, $this->config['content']['filesystem_nbm_path'], $this->config['java']['tmpDir'], $this->config['java']['home'], $this->config['java']['ant'], $this->config['java']['buildScriptPath']);
                                $catal->saveXml($this->config['content']['filesystem_catalogue_path']);
                            }

                            PpLog::logMe($req->Verification->Plugin, NbSsoUser::getInstance()->getUsername(), 'Vote cast by ' . NbSsoUser::getInstance()->getUsername() . ' for ' . $req->Verification->Plugin->plugin_name . ', UC ' . $req->Verification->version . ', vote value: ' . $req->vote . '; verification overall status recalculated: ' . $req->Verification->status . ' (' . $detail . ')');
                        }
                    }
                }

                // process search
                if ($this->_getParam('search')) {
                    $sQr = Doctrine_Query::create()->from('PpVerificationRequest r')
                            ->leftJoin('r.Verifier p')
                            ->leftJoin('r.Verification v')
                            ->leftJoin('v.Plugin g')
                            ->where('p.userid=?', NbSsoUser::getInstance()->getUsername())->groupBy('r.verification_id')
                            ->andWhere('g.plugin_name LIKE ?', '%' . $this->_getParam('search') . '%')
                            ->orderBy('r.requestdate desc');
                    $this->view->searchPager = new Doctrine_Pager($sQr, 1, 100);
                }
            }

            $this->view->pageTitle = 'My verifications';
            // setup pagination
            $paginate = 5;
            $page1 = ($this->_getParam('page1')) ? $this->_getParam('page1') : 1;
            $page2 = ($this->_getParam('page2')) ? $this->_getParam('page2') : 1;
            $page3 = ($this->_getParam('page3')) ? $this->_getParam('page3') : 1;
            // I have not voted yet and overall status is 0
            $vfrq2 = Doctrine_Query::create()->from('PpVerificationRequest r')
                    ->leftJoin('r.Verifier p')
                    ->leftJoin('r.Verification v')
                    ->where('p.userid=?', NbSsoUser::getInstance()->getUsername())->groupBy('r.verification_id')
                    ->andWhere('v.status=0')
                    ->andWhere('r.vote=0 OR r.vote IS NULL')
                    ->orderBy('r.requestdate desc');
            // I voted but overal status is already decided
            $vfrq3 = Doctrine_Query::create()->from('PpVerificationRequest r')
                    ->leftJoin('r.Verifier p')
                    ->leftJoin('r.Verification v')
                    ->where('p.userid=?', NbSsoUser::getInstance()->getUsername())->groupBy('r.verification_id')
                    ->andWhere('v.status<>0')
                    ->andWhere('r.votedate IS NULL')
                    ->orderBy('r.requestdate desc');
            // I already voted
            $vfrq1 = Doctrine_Query::create()->from('PpVerificationRequest r')
                    ->leftJoin('r.Verifier p')
                    ->leftJoin('r.Verification v')
                    ->where('p.userid=?', NbSsoUser::getInstance()->getUsername())->groupBy('r.verification_id')
                    //->andWhere('v.status<>0')
                    ->andWhere('r.vote<>0')
                    ->orderBy('r.requestdate desc');
            $this->view->pager1 = new Doctrine_Pager($vfrq1, $page1, $paginate);
            $this->view->pager2 = new Doctrine_Pager($vfrq2, $page2, $paginate);
            $this->view->pager3 = new Doctrine_Pager($vfrq3, $page3, $paginate);
        } else {
            $this->_redirect('/');
        }
        $this->initPurifier();
    }

    public function removeVerificationAction() {
        if ($this->_getParam('id') && NbSsoUser::getInstance()->isAuthenticated()) {
            $verif = Doctrine_Core::getTable('PpVerification')->find($this->_getParam('id'));
            // delete only if owner matches user logged in
            if ($verif && ($verif->Plugin->author_userid == NbSsoUser::getInstance()->getUsername()) || in_array(NbSsoUser::getInstance()->getUsername(), explode(',', $this->config['admin']['users']))) {
                $id = $verif->Plugin->publicid;
                $vers = $verif->version;
                PpLog::logMe($verif->Plugin, NbSsoUser::getInstance()->getUsername(), 'Verification deleted for version ' . $verif->version . ', id: ' . $this->_getParam('id'));
                // delete verification requests
                Doctrine_Query::create()->delete('PpVerificationRequest')->where('verification_id=?', $this->_getParam('id'))->execute();
                // delete join table items
                Doctrine_Query::create()->delete('PpVerificationToRequest')->where('verificationImpl_ID=?', $this->_getParam('id'))->execute();
                // delete verification
                $verif->delete();
                // regenerate catalogue
                $catal = new CatalogueAntImplementation();
                $catal->generateCatalogue($vers, $this->config['content']['filesystem_nbm_path'], $this->config['java']['tmpDir'], $this->config['java']['home'], $this->config['java']['ant'], $this->config['java']['buildScriptPath']);
                $catal->saveXml($this->config['content']['filesystem_catalogue_path']);
                // and redirect back
                $this->_redirect('/plugin/' . $id . '/?show=true');
            }
        }
    }

    public function indexAction() {
        // init cache
        $this->initCache();
        $this->view->pageTitle = 'NetBeans Plugin Portal, NetBeans IDE Plugins Repository';
        // get featured plugin
        $fp = Doctrine_Query::create()->from('PpFeaturedPlugin f')->innerJoin('f.Plugin p')
                ->where('f.from<=?', date('Y-m-d'))
                ->andWhere('f.to>=?', date('Y-m-d'))
                ->limit(1)->fetchOne();

        $mostDownloaded = Doctrine_Query::create()->from('PpPlugin p')->where('p.published=1')->orderBy('downloads desc')->limit($this->config['content']['frontpage']['numItems'])->useResultCache(true)->execute();
        $bestRated = Doctrine_Query::create()->select('p.*')->from('PpPlugin p')->where('p.published=1')->orderBy('average_rating desc')->limit($this->config['content']['frontpage']['numItems'])->execute();
        $fresh = Doctrine_Query::create()->select('p.*')->from('PpPlugin p')->leftJoin('p.Binaries b')->where('p.published=1')->andWhere('p.date_last_updated is not null')->andWhere('b.historic=0')->orderBy('p.date_last_updated desc')->limit($this->config['content']['frontpage']['numItems'])->execute();
        if (!$this->view->cache->test('frontpage')) {
            $plugins = Doctrine_Query::create()->from('PpPlugin p')
                            ->leftJoin('p.Category')->leftJoin('p.Category2')->leftJoin('p.Category3')->innerJoin('p.Binaries b')->innerJoin('b.Version')->leftJoin('p.Verifications')
                            ->where('p.published=1')->andWhere('b.historic=0')
                            ->useResultCache(true)->execute(null, Doctrine_Core::HYDRATE_ARRAY);
        }
        $categories = Doctrine_Query::create()->from('PpDisplayCategory')->orderBy('displaycategory_name')->useResultCache(true)->execute(null, Doctrine_Core::HYDRATE_ARRAY);
        $versions = Doctrine_Query::create()->from('PpNetbeansVersion')->orderBy('version DESC')->useResultCache(true)->execute(null, Doctrine_Core::HYDRATE_ARRAY);
        // now load up variables to the view
        $this->view->fp = $fp->Plugin;
        $this->view->mostDownloaded = $mostDownloaded;
        $this->view->bestRated = $bestRated;
        $this->view->fresh = $fresh;
        $this->view->plugins = $plugins;
        $this->view->versions = $versions;
        $this->view->categories = $categories;
        // load up HTML Purifier
        $this->initPurifier();
    }

    public function pluginAction() {
        if ($this->_getParam('pluginid'))
            $this->_setParam('id', $this->_getParam('pluginid'));
        if ($this->_getParam('id')) {
            // load up HTML Purifier
            $this->initPurifier();
            // find plugin
            $pQ = Doctrine_Query::create()->from('PpPlugin p')->leftJoin('p.Binaries b')->leftJoin('b.Version v')->leftJoin('b.Verification x')
                    ->where('p.publicid=?', $this->_getParam('id'))->andWhere('b.historic=0')
                    ->orderBy('p.pluginid desc')->limit(1);
            if ($this->_getParam('show') != 'true') {
                $pQ->andWhere('p.published=1');
            }
            $plugin = $pQ->fetchOne();
            $this->view->log = Doctrine_Query::create()->from('PpLog')->where('pluginid=?', $plugin->publicid)->orderBy('id desc')->execute();
            $this->view->pageTitle = strip_tags($this->view->purifier->purify($plugin->plugin_name) . ' - NetBeans Plugin detail');
            $this->view->plugin = $plugin;
            $this->view->verifable = Doctrine_Query::create()->select('version')->from('PpNetbeansVersion')->where('verifable=?', 1)->orderBy('version desc')->useResultCache(true)->execute(null, Doctrine_Core::HYDRATE_SINGLE_SCALAR);
            if (NbSsoUser::getInstance()->isAuthenticated()) {
                $this->view->voted = Doctrine_Query::create()->select('rating_level')->from('PpRating')
                                ->where('userid=?', NbSsoUser::getInstance()->getUsername())->andWhere('plugin_pluginid=?', $plugin->publicid)->fetchOne(null, Doctrine_Core::HYDRATE_SINGLE_SCALAR);
            }
        } else {
            $this->_redirect('/');
        }
    }

    public function verificationRequestAction() {
        $this->rejectGuest();
        if ($this->_getParam('binary') && $this->_getParam('vers')) {
            $bin = Doctrine_Core::getTable('PpBinary')->find($this->_getParam('binary'));
            $plugin = $bin->Plugin;
            $existingVerif = Doctrine_Query::create()->select('count(id)')
                    ->from('PpVerification')->where('version=?', $this->_getParam('vers'))->andWhere('binary_id=?', $this->_getParam('binary'))
                    ->execute(null, Doctrine_Core::HYDRATE_SINGLE_SCALAR);
            if ($bin && $existingVerif == 0) {
                //PpLog::logMe($plugin, NbSsoUser::getInstance()->getUsername(), 'Verification requested for version ' . $this->_getParam('vers'));
                $bin->requestVerification($this->_getParam('vers'));
            }
            $this->_redirect('/plugin/' . $plugin->publicid . '/?show=true');
        }
    }

    public function pluginPublishStep1Action() {
        $this->rejectGuest();
        $err = 0;
	//die('We are sorry, plugin upload is temporarily disabled. Please try again later. We apologize for the inconvenience.');
        $this->view->pageTitle = "Publish plugin - Step 1";
        if ($this->_request->isPost() && NbSsoUser::getInstance()->isAuthenticated()) {
            if ($this->_getParam('type') == 'url') {
                $_SESSION['publish']['type'] = 'url';
                // do nothing, just jump to the step 2
                $this->_redirect('/plugin-publish-step2');
                die();
            } elseif ($this->_getParam('type') == 'file') {
                $_SESSION['publish']['type'] = 'file';
                /* upload file, unpack, examine manifest and prepopulate fields */
                $upload = new Zend_File_Transfer_Adapter_Http();
                $upload->setDestination($this->config['content']['filesystem_nbm_path']);
                $upload->addValidator('Extension', false, $this->config['content']['plugin_extensions']);

                // upload received file(s)
                if ($upload->isValid()) {
                    try {
                        //$info = $upload->getFileInfo('userfile');
                        $newName = time() . '_' . str_replace(' ','_',$_FILES["userfile"]["name"]); //$info['userfile']['name'];
                        // Rename uploaded file using Zend Framework
                        $fullFilePath = $this->config['content']['filesystem_nbm_path'] . $newName;
                        //var_dump($info);
                        // and move it
                        //$filterFileRename = new Zend_Filter_File_Rename(array('target' => $fullFilePath, 'overwrite' => true));
                        //$filterFileRename->filter($upload->getFileName('userfile'));
                        move_uploaded_file($_FILES["userfile"]["tmp_name"], $fullFilePath);
                        chmod($fullFilePath, 0755);
                        PpLog::logMe(null, NbSsoUser::getInstance()->getUsername(), 'Plugin uploaded: ' . $newName);
                        // save some basic info
                        $plugin = new PpPlugin();
                        $plugin->download_size = $_FILES["userfile"]["size"];
                        $plugin->nbm_filename = $newName;
                        // if it's nbm, unpack and preload detail from manifest
                        if (substr($newName, -4, 4) == '.nbm') {
                            // now extract it and load info
                            if (!$plugin->unpackPluginAndLoadInfoXML(substr($newName, 0, -4), $fullFilePath, $this->config['content']['tmp_location_path'], $this->config['system']['unzip_executable'])) {
                                // missing manifest,  setup err msg and stay on the page
                                $_SESSION['flash']['msg'] = '<span class="error">ERROR: archive seems to be missing Info/info.xml manifest file</span>';
                                PpLog::logMe(null, NbSsoUser::getInstance()->getUsername(), 'ERR: we were not able to unpack and load infor from .nbm file ' . $newName);
                                $err = 1;
                            }
                            // check if module has moduleauthor and summary in manifest - It's hard req
                            if (empty($plugin->_moduleAuthor)) {
                                $this->setupFlash('Error: There is moduleauthor attribute missing in the manifest file. <a href="http://wiki.netbeans.org/FaqPluginRequirements">See wiki for more info on plugin requirements.</a>', 'error');
                                $err = 1;
                            }
                            if (empty($plugin->_summary)) {
                                $this->setupFlash('Error: There is OpenIDE-Module-Short-Description attribute missing in the manifest file. <a href="http://wiki.netbeans.org/FaqPluginRequirements">See wiki for more info on plugin requirements.</a>', 'error');
                                $err = 1;
                            }
                            if (empty($plugin->_license) || trim($plugin->_license) == "[NO LICENSE SPECIFIED]") {
                                $this->setupFlash('Error: There is no license specified in the manifest file. <a href="http://wiki.netbeans.org/FaqPluginRequirements">See wiki for more info on plugin requirements.</a>', 'error');
                                $err = 1;
                            }
                        } elseif (substr($newName, -4, 4) == '.zip') {
                            // extract and check if all sub modules are signed
                            if (!$plugin->unpackPluginPackAndVerifySignatures(substr($newName, 0, -4), $fullFilePath, $this->config['content']['tmp_location_path'], $this->config['system']['unzip_executable'])) {
                                $this->setupFlash('Error: There are some required attributes missing in the plugin pack. <a href="http://wiki.netbeans.org/FaqPluginRequirements">See wiki for more info on plugin requirements.</a><br/>' . implode('<br/>', $plugin->log), 'error');
                                $err = 1;
                            }
                        }
                        if (!$err) {
                            // now save the dump to session so it survices redirect and ressurect it on the other side then
                            $_SESSION['uploaded_plugin'] = $plugin->toArray();
                            // jeez, new field needs to be set manually
                            $_SESSION['uploaded_plugin']['hard_dependency'] = $plugin->hard_dependency;
                            // and finally redirect to step2
                            $this->_redirect('/plugin-publish-step2');
                        }
                    } catch (Exception $e) {
                        // any exception? let's print it
                        $this->setupFlash('Error: ' . $e->getMessage(), 'error');
                    }
                } else {
                    // invalid file extension msg
                    $this->setupFlash('Error: Only <b>.nbm</b> and <b>.zip</b> file formats allowed.', 'error');
                }
            }
        }
    }

    public function pluginPublishStep2Action() {
        try {
            $this->rejectGuest();
            $this->initPurifier();
            $plugin = new PpPlugin();
            if (isset($_SESSION['uploaded_plugin']) && !empty($_SESSION['uploaded_plugin'])) {
                $plugin->fromArray($_SESSION['uploaded_plugin']);
                $plugin->hard_dependency = $_SESSION['uploaded_plugin']['hard_dependency'];
                $this->view->plugin = $plugin;
            }
            $this->view->pageTitle = "Publish plugin - Step 2";
            $this->view->versions = Doctrine_Query::create()->select('version')->from('PpNetbeansVersion')->where('verifable=?', 1)->orderBy('version')->useResultCache(true)->execute();
            $this->view->categories = Doctrine_Query::create()->select('displaycategory_name')->from('PpDisplayCategory')->where('displaycategory_name<>""')->orderBy('displaycategory_name')->useResultCache(true)->execute();
            if ($this->_request->isPost() && NbSsoUser::getInstance()->isAuthenticated()) {
                // whack plugin form session
                unset($_SESSION['uploaded_plugin']);
                // verify the fields
                $flds = array(
                    'plugin_name' => 'Plugin name missing',
                    'license_type' => 'License type missing',
                    'summary' => 'Summary missing',
                    'description' => 'Description missing'
                );
                if ($_SESSION['publish']['type'] == 'url') {
                    $flds['home_page_url'] = 'Plugin homepage url missing';
                    $logString = 'with just URL';
                } else {
                    $logString = 'with .nbm/zip file';
                }
                $flds2 = array('0' => 'Plugin category missing');
                $flds3 = array('0' => 'Supported NetBeans version is missing');
                $valid = $this->validateFields($this->_getParam('plugin'), $flds);
                $valid2 = $this->validateFields($this->_getParam('categs'), $flds2);
                $valid3 = $this->validateFields($this->_getParam('versions_supported'), $flds3);
                // load up fields with data
                $plugin->fromArray($this->_getParam('plugin'));
                $plugin->categoryid = (int) $_POST['categs'][0];
                $plugin->categoryid2 = (int) $_POST['categs'][1];
                $plugin->categoryid3 = (int) $_POST['categs'][2];

                // validate for SPAM content using our own AntiSpam service
                $antiSpam = new AntiSpamValidator($this->config['antispam']['url'], $this->config['sso']['proxyHost'], $this->config['sso']['proxyPort']);
                $isNotSpamPost = $antiSpam->validate($plugin->home_page_url.' '.$plugin->summary.' '.$plugin->description.' '.$plugin->new_version_comments, $plugin->plugin_name, $_SERVER['REMOTE_ADDR'], NbSsoUser::getInstance()->getUsername());
                if(!$isNotSpamPost) {
                    // oups, it's SPAM, notify user and do not continue
                    $this->view->plugin = $plugin;
                    $_SESSION['flash']['msg'].='<li>Either your plugin description contains spam or your account was blocked due to some malicious activity in the past. If you believe this happened in error, please <a href="mailto:geertjan@apache.org?subject=Spam">let us know</a>.</li>';
                    $_SESSION['flash']['type'] = 'error';
                }

                if ($valid && $valid2 && $valid3 && $isNotSpamPost) {
                    $plugin->date_last_updated = date('Y-m-d');
                    $plugin->date_added = date('Y-m-d');
                    $plugin->published = '0';
                    $plugin->pluginid = DbAutoincrement::getNewValue();
                    $plugin->publicid = $plugin->pluginid;
                    $plugin->author_userid = NbSsoUser::getInstance()->getUsername();
                    DbAutoincrement::increaseCounter();
                    // handle image upload
                    $upload = new Zend_File_Transfer_Adapter_Http();
                    $upload->setDestination($this->config['content']['filesystem_img_path']);
                    $upload->addValidator('Extension', false, $this->config['content']['img_extensions']);
                    if ($upload->isValid(array('thumbnail'))) {
                        try {
                            $upload->receive(array('thumbnail'));
                            $info = $upload->getFileInfo();
                            $thumbNewName = time() . '_' . $info['thumbnail']['name'];
                            $thumbFilter = new Zend_Filter_File_Rename(array('target' => $this->config['content']['filesystem_img_path'] . $thumbNewName, 'overwrite' => true));
                            $thumbFilter->filter($upload->getFileName('thumbnail'));
                            $plugin->image_filename = $thumbNewName;
                            chmod($this->config['content']['filesystem_img_path'] . $thumbNewName, 0755);
                        } catch (Exception $e) {
                            die($e->getMessage());
                        }
                    }
                    if ($upload->isValid(array('full'))) {
                        try {
                            $upload->receive(array('full'));
                            $info = $upload->getFileInfo();
                            $fullNewName = time() . '_' . $info['full']['name'];
                            $fullFilter = new Zend_Filter_File_Rename(array('target' => $this->config['content']['filesystem_img_path'] . $fullNewName, 'overwrite' => true));
                            $fullFilter->filter($upload->getFileName('full'));
                            $plugin->fullsize_image_filename = $fullNewName;
                            chmod($this->config['content']['filesystem_img_path'] . $fullNewName, 0755);
                        } catch (Exception $e) {
                            die($e->getMessage());
                        }
                    }
                    // ok, let's sanitize some params from xss etc
                    $plugin->plugin_name = strip_tags($plugin->plugin_name);
                    $plugin->summary = strip_tags($plugin->summary);
                    $plugin->home_page_url = strip_tags($plugin->home_page_url);
                    $plugin->license_type = strip_tags($plugin->license_type);
                    $plugin->description = strip_tags($plugin->description, '<p>,<table>,<tr>,<td>,<ol>,<ul>,<li>,<h1>,<h2>,<h3>');
                    $plugin->signed = str_replace(array('(', ')'), '', $plugin->signed);
                    $plugin->save();
                    // save also the binary
                    $bin = new PpBinary();
                    $bin->plugin_id = $plugin->pluginid;
                    $bin->version_id = $this->_getParam('versions_supported');
                    $bin->updated_on = $plugin->date_last_updated;
                    $bin->signed = str_replace(array('(', ')'), '', $plugin->signed);
                    $bin->nbm_filename = $plugin->nbm_filename;
                    $bin->nbm_url = $plugin->nbm_url;
                    $bin->download_size = $plugin->download_size;
                    $bin->whats_new = $plugin->new_version_comments;
                    $bin->hard_dependency = $plugin->hard_dependency;
                    $bin->save();
//                // assign versions supported
//                if ($this->_getParam('versions_supported')) {
//                    $vs = $this->_getParam('versions_supported');
//
//                        $pv = new PpPluginToVersion;
//                        $pv->plugin_id = $plugin->pluginid;
//                        $pv->version_id = $v;
//                        $pv->save();
//
//                }
                    // log it
                    PpLog::logMe($plugin, $plugin->author_userid, 'Plugin saved ' . $logString . '; id: ' . $plugin->pluginid);
                    unset($_SESSION['publish']['type']);
                    //die(var_dump($plugin));
                    // now redirect to user plugin list
                    $this->setupFlash('Your plugin was successfully registered.<br/>
                                    Now it needs to be reviewed by our staff and approved in order to become
                                    publicly visible. This step can take up to several days but typically 12 hours.
                                    If it takes longer, please <a href="https://netbeans.org/bugzilla/enter_bug.cgi?product=updatecenters&component=Pluginportal&short_desc=Approval%20request%20for%20%3Cplugin_name%3E">submit a ticket</a>.
                                    We will inform you once your plugin gets available. <br/><br/>Thanks for your patience.<br/><b>The NetBeans team</b>');
                    $this->_redirect('/my-plugins');
                } else {
                    $this->view->plugin = $plugin;
                }
            }
            $this->view->allowBinaryUpdate = false;
            $this->view->allowChangeVersion = true;
        } catch (\Exception $e) {
            echo $e->getMessage();
        }
    }

    public function addBinaryAction() {
        $this->rejectGuest();
        if ($this->_getParam('id')) {
            $plugin = Doctrine_Core::getTable('PpPlugin')->find($this->_getParam('id'));
            $bin = new PpBinary();
            $bin->plugin_id = $plugin->pluginid;
            $this->initPurifier();
            $this->processUploadedBinary($bin, $plugin);
        }
        $this->view->plugin = $plugin;
        $this->view->bin = $bin;
        $this->view->allowBinaryUpdate = true;
        $this->view->allowChangeVersion = true;
        $this->view->pageTitle = 'Add Plugin\'s Binary: ' . $plugin->plugin_name;
        $assignedVersions = $plugin->getNetbeansVersionsAsArray();
        $this->view->versions = Doctrine_Query::create()->select('version')
                        ->from('PpNetbeansVersion')
                        ->whereNotIn('id', $assignedVersions)->orderBy('version DESC')->useResultCache(true)->execute();
    }

    public function editBinaryAction() {
        $this->rejectGuest();
        if ($this->_getParam('id')) {
            $bin = Doctrine_Core::getTable('PpBinary')->find($this->_getParam('id'));
            $plugin = $bin->Plugin;
            $this->initPurifier();
            $this->processUploadedBinary($bin, $plugin);

            $this->view->pageTitle = 'Edit Plugin\'s Binary: ' . $plugin->plugin_name;
            //$this->view->versions = Doctrine_Query::create()->select('version')->from('PpNetbeansVersion')->where('verifable=?', 1)->orderBy('version')->useResultCache(true)->execute();
            $this->view->versions = Doctrine_Query::create()->select('version')->from('PpNetbeansVersion')->orderBy('version DESC')->useResultCache(true)->execute();
            $this->view->plugin = $plugin;
            $this->view->bin = $bin;
            $this->view->allowBinaryUpdate = true;
            $this->view->allowChangeVersion = false;
        }
    }

    private function processUploadedBinary($bin, $plugin) {
        // handle post
        if ($this->_request->isPost()) {
            $bin->updated_on = date('Y-m-d H:i:s');
            $bin->fromArray($this->_getParam('binary'));
            $flds = array('version_id' => 'Supported NetBeans version missing');
            $valid = $this->validateFields($this->_getParam('binary'), $flds);
            $antiSpam = new AntiSpamValidator($this->config['antispam']['url'], $this->config['sso']['proxyHost'], $this->config['sso']['proxyPort']);
            if(!$antiSpam->validate($bin->whats_new, $plugin->plugin_name, $_SERVER['REMOTE_ADDR'], NbSsoUser::getInstance()->getUsername())) {
                // oups, it's SPAM, notify user and do not continue
                $_SESSION['flash']['msg'].='<li>Either your plugin description contains spam or your account was blocked due to some malicious activity in the past. If you believe this happened in error, please <a href="mailto:geertjan@apache.org?subject=Spam">let us know</a>.</li>';
                $_SESSION['flash']['type'] = 'error';
                return;
            }
            if ($valid) {
                $err = 0;
                // load new data from form fields
                //PpLog::logMe($plugin, NbSsoUser::getInstance()->getUsername(), 'Binary details updated id: ' . $bin->binary_id);

                /* if user also uploads new .nbm or .zip we must verify and save it */;
                if (!empty($_FILES['userfile']['name'])) {
                    // we are now uploading new one, so reset passed $bin to fresh new state (do not use passed one so we create new record in DB instead of updating old one)
                    $b=new PpBinary();
                    $b->fromArray($this->_getParam('binary'));
                    $b->plugin_id=$bin->plugin_id;
                    $b->updated_on = date('Y-m-d H:i:s');
                    $bin=$b;

                    PpLog::logMe($plugin, NbSsoUser::getInstance()->getUsername(), 'Uploading new binary for version ' . $bin->Version->version);
                    $upload2 = new Zend_File_Transfer_Adapter_Http();
                    $upload2->setDestination($this->config['content']['filesystem_nbm_path']);
                    $upload2->addValidator('Extension', false, $this->config['content']['plugin_extensions']);
                    if ($upload2->isValid('userfile')) {
                        try {
                            //$upload2->receive('userfile');
                            //$info = $upload2->getFileInfo('userfile');
                            PpLog::logMe($plugin, NbSsoUser::getInstance()->getUsername(), 'Binary uploaded successfully - ' . $upload2->getFileName('userfile'));
                            $newName = time() . '_' . str_replace(' ','_',$_FILES["userfile"]["name"]); //$
                            // Rename uploaded file using Zend Framework
                            $fullFilePath = $this->config['content']['filesystem_nbm_path'] . $newName;
                            //var_dump($info);
                            // and move it
                            //$filterFileRename = new Zend_Filter_File_Rename(array('target' => $fullFilePath, 'overwrite' => true));
                            //$filterFileRename->filter($upload->getFileName('userfile'));
                            move_uploaded_file($_FILES["userfile"]["tmp_name"], $fullFilePath);
                            chmod($fullFilePath, 0755);
                            // save some basic info
                            $binaryTmp = new PpPlugin();
                            $binaryTmp->download_size = $_FILES["userfile"]["size"];
                            $binaryTmp->nbm_filename = $newName;
                            // if it's nbm, unpack and preload detail from manifest
                            if (substr($newName, -4, 4) == '.nbm') {
                                $log = ', .nbm type';
                                // now extract it and load info
                                if (!$binaryTmp->unpackPluginAndLoadInfoXML(substr($newName, 0, -4), $fullFilePath, $this->config['content']['tmp_location_path'], $this->config['system']['unzip_executable'])) {
                                    // missing manifest,  setup err msg and stay on the page
                                    $this->setupFlash('ERROR: archive seems to be missing Info/info.xml manifest file', 'error');
                                    $err = 1;
                                }
                                // check if module has moduleauthor and summary in manifest - It's hard req
                                if (empty($binaryTmp->_moduleAuthor)) {
                                    $this->setupFlash('Error: There is moduleauthor attribute missing in the manifest file. <a href="http://wiki.netbeans.org/FaqPluginRequirements">See wiki for more info on plugin requirements.</a>', 'error');
                                    $err = 1;
                                }
                                if (empty($binaryTmp->_summary)) {
                                    $this->setupFlash('Error: There is OpenIDE-Module-Short-Description attribute missing in the manifest file. <a href="http://wiki.netbeans.org/FaqPluginRequirements">See wiki for more info on plugin requirements.</a>', 'error');
                                    $err = 1;
                                }
                                if (empty($binaryTmp->_license) || trim($binaryTmp->_license) == "[NO LICENSE SPECIFIED]") {
                                    $this->setupFlash('Error: There is no license specified in the manifest file. <a href="http://wiki.netbeans.org/FaqPluginRequirements">See wiki for more info on plugin requirements.</a>', 'error');
                                    $err = 1;
                                }
                            } else {
                                $binaryTmp->unpackPluginPackAndVerifySignatures(substr($newName, 0, -4), $fullFilePath, $this->config['content']['tmp_location_path'], $this->config['system']['unzip_executable']);
                                $log.=', .zip type';
                            }
                            if (!$err) {
                                // mark all prev binaries for this version as historic
                                Doctrine_Query::create()->update('PpBinary')->set('historic=?', 1)->where('plugin_id=?', $bin->plugin_id)->andWhere('version_id=?',$bin->version_id)->andWhere('historic=?',0)->execute();

                                // update/create binary with new values
                                $bin->nbm_filename = $binaryTmp->nbm_filename;
                                $bin->signed = $binaryTmp->signed;
                                $bin->download_size = $binaryTmp->download_size;
                                $bin->hard_dependency = $binaryTmp->hard_dependency;
                                unset($binaryTmp);
                                // save binary so we have it's id
                                $bin->save();
                                // also touch plugin date_;ast_updated
                                $p = Doctrine_Core::getTable('PpPlugin')->find($bin->plugin_id);
                                if ($p) {
                                    $p->date_last_updated = date('Y-m-d');
                                    $p->save();
                                }
                                PpLog::logMe($plugin, NbSsoUser::getInstance()->getUsername(), 'Binary successfully saved ' . $bin->nbm_filename . ', binary_id: ' . $bin->binary_id . ', version: ' . $bin->Version->version);

                                // now also update experimental catalog
                                if (strstr($bin->nbm_filename, '.zip')) {
                                    $bin->unpackModulePack($this->config['content']['tmp_location_path'], $this->config['content']['filesystem_nbm_path'], $this->config['system']['unzip_executable']);
                                }
                                $catal = new CatalogueAntImplementation();
                                $catal->generateExperimentalCatalogue($bin->version_id, $this->config['content']['filesystem_nbm_path'], $this->config['java']['tmpDir'], $this->config['java']['home'], $this->config['java']['ant'], $this->config['java']['buildScriptPath']);
                                $msg = $catal->saveXml($this->config['content']['filesystem_experimental_catalogue_path'], 'catalog-experimental.xml');
                            }
                        } catch (Exception $e) {
                            // any exception? let's print it
                            $this->setupFlash('ERROR: ' . $e->getMessage(), 'error');
                        }
                    } else {
                        // invalid file extension msg
                        $err = 1;
                        $this->setupFlash('ERROR: Only <b>.nbm</b> and <b>.zip</b> file formats allowed.', 'error');
                    }
                }

                if (!$err) {
                    // and save binary data
                    $bin->save();
                    $this->setupFlash('Binary saved');
                    $this->invalidateCache();
                    $this->_redirect('/my-plugins/');
                }
            }
        }
    }

    public function editPluginAction() {
        $this->rejectGuest();
        if ($this->_getParam('id')) {
            $this->initPurifier();
            $plugin = Doctrine_Core::getTable('PpPlugin')->find($this->_getParam('id'));
            $rating = $plugin->average_rating;
            // setup the type
            if (!empty($plugin->nbm_filename)) {
                $_SESSION['publish']['type'] == 'file';
            } else {
                $_SESSION['publish']['type'] = 'url';
            }
            // stop processing if owner is not the one logged in :)
            if (!in_array(NbSsoUser::getInstance()->getUsername(), explode(',', $this->config['admin']['users']))) {
                if (($plugin->author_userid != NbSsoUser::getInstance()->getUsername()) || !$plugin) {
                    $this->_redirect('/');
                    die();
                }
            }
            // process post
            if ($this->_request->isPost()) {
                $flds = array(
                    'plugin_name' => 'Plugin name missing',
                    'license_type' => 'License type missing',
                    'summary' => 'Summary missing',
                    'description' => 'Description missing',
                );
                if ($_SESSION['publish']['type'] == 'url') {
                    $flds['home_page_url'] = 'Plugin homepage url missing';
                }
                $flds2 = array('0' => 'Plugin category missing');
                //$flds3 = array('0' => 'Supported NetBeans version(s) missing');
                $valid = $this->validateFields($this->_getParam('plugin'), $flds);
                $valid2 = $this->validateFields($this->_getParam('categs'), $flds2);
                //$valid3 = $this->validateFields($this->_getParam('versions_supported'), $flds3);
                // load up the fields
                $plugin->fromArray($this->_getParam('plugin'));
                $plugin->categoryid = (int) $_POST['categs'][0];
                $plugin->categoryid2 = (int) $_POST['categs'][1];
                $plugin->categoryid3 = (int) $_POST['categs'][2];
                $antiSpam = new AntiSpamValidator($this->config['antispam']['url'], $this->config['sso']['proxyHost'], $this->config['sso']['proxyPort']);
                $isNotSpam = $antiSpam->validate($plugin->home_page_url.' '.$plugin->summary.' '.$plugin->description.' '.$plugin->new_version_comments, $plugin->plugin_name, $_SERVER['REMOTE_ADDR'], NbSsoUser::getInstance()->getUsername());
                if(!$isNotSpam) {
                    // oups, it's SPAM, notify user and do not continue
                    $this->view->plugin = $plugin;
                    $_SESSION['flash']['msg'].='<li>Either your plugin description contains spam or your account was blocked due to some malicious activity in the past. If you believe this happened in error, please <a href="mailto:geertjan@apache.org?subject=Spam">let us know</a>.</li>';
                    $_SESSION['flash']['type'] = 'error';
                }
                if ($valid && $valid2 && $isNotSpam) {
                    PpLog::logMe($plugin, NbSsoUser::getInstance()->getUsername(), 'Plugin edit initiated');
                    $err = 0;
                    $plugin->date_last_updated = date('Y-m-d');
                    //$plugin->date_added = date('Y-m-d');
                    //$plugin->published = '0';
                    // assign versions supported
                    if ($this->_getParam('versions_supported')) {
                        $vs = $this->_getParam('versions_supported');
                        // drop all previous values so it can handle also deselecting
                        Doctrine_Query::create()->delete('PpPluginToVersion')->where('plugin_id=?', $plugin->pluginid)->execute();
                        // and now reinsert
                        foreach ($vs as $v) {
                            $pv = new PpPluginToVersion;
                            $pv->plugin_id = $plugin->pluginid;
                            $pv->version_id = $v;
                            $pv->save();
                        }
                    }
                    // handle image upload
                    $upload = new Zend_File_Transfer_Adapter_Http();
                    $upload->setDestination($this->config['content']['filesystem_img_path']);
                    $upload->addValidator('Extension', false, $this->config['content']['img_extensions']);

                    if ($upload->isValid(array('thumbnail'))) {
                        try {
                            $upload->receive(array('thumbnail'));
                            $info = $upload->getFileInfo();
                            $thumbNewName = time() . '_' . $info['thumbnail']['name'];
                            $thumbFilter = new Zend_Filter_File_Rename(array('target' => $this->config['content']['filesystem_img_path'] . $thumbNewName, 'overwrite' => true));
                            $thumbFilter->filter($upload->getFileName('thumbnail'));
                            $plugin->image_filename = $thumbNewName;
                            PpLog::logMe($plugin, NbSsoUser::getInstance()->getUsername(), 'New thumbnail image added');
                            chmod($this->config['content']['filesystem_img_path'] . $thumbNewName, 0755);
                        } catch (Exception $e) {
                            die($e->getMessage());
                        }
                    }
                    if ($upload->isValid(array('full'))) {
                        try {
                            $upload->receive(array('full'));
                            $info = $upload->getFileInfo();
                            $fullNewName = time() . '_' . $info['full']['name'];
                            $fullFilter = new Zend_Filter_File_Rename(array('target' => $this->config['content']['filesystem_img_path'] . $fullNewName, 'overwrite' => true));
                            $fullFilter->filter($upload->getFileName('full'));
                            $plugin->fullsize_image_filename = $fullNewName;
                            PpLog::logMe($plugin, NbSsoUser::getInstance()->getUsername(), 'New full image added');
                            chmod($this->config['content']['filesystem_img_path'] . $fullNewName, 0755);
                        } catch (Exception $e) {
                            die($e->getMessage());
                        }
                    }

                    // and save data of the original plugin, but only if we did not create new as copy
                    if (!$pluginNew) {
                        $plugin->save();
                        PpLog::logMe($plugin, NbSsoUser::getInstance()->getUsername(), 'Plugin edited, id:' . $plugin->pluginid . ', pubid:' . $plugin->publicid);
                    }
                    unset($_SESSION['publish']['type']);

                    if (!$err) {
                        $this->setupFlash('Plugin saved');
                        $this->invalidateCache();
                        $this->_redirect('/my-plugins/');
                    }
                } else {
                    $this->view->plugin = $plugin;
                }
            }
            $this->view->pageTitle = 'Edit Plugin: ' . $plugin->plugin_name;
            //$this->view->versions = Doctrine_Query::create()->select('version')->from('PpNetbeansVersion')->where('verifable=?', 1)->orderBy('version')->useResultCache(true)->execute();
            $this->view->versions = Doctrine_Query::create()->select('version')->from('PpNetbeansVersion')->orderBy('version DESC')->useResultCache(true)->execute();
            $this->view->categories = Doctrine_Query::create()->select('displaycategory_name')->from('PpDisplayCategory')->where('displaycategory_name<>""')->orderBy('displaycategory_name')->useResultCache(true)->execute();
            $this->view->plugin = $plugin;
            $this->view->allowBinaryUpdate = false;
            $this->view->allowChangeVersion = false;
            $this->view->hideWhatsNew = true;
            // setup version data for the form
            if ($plugin->Versions->count() > 0) {
                foreach ($plugin->Versions as $v) {
                    $_POST['versions_supported'][] = $v->id;
                }
            }
        }
    }

    public function deletePluginAction() {
        $this->rejectGuest();
        if ($this->_getParam('id')) {
            $plugin = Doctrine_Core::getTable('PpPlugin')->find($this->_getParam('id'));
            // stop processing if owner is not the one logged in :)
            if (($plugin->author_userid != NbSsoUser::getInstance()->getUsername()) || !$plugin) {
                $this->_redirect('/');
                die();
            }
            if ($plugin) {
                // delete binaries
                if (count($plugin->Binaries) > 0) {
                    foreach ($plugin->Binaries as $bin) {
                        $this->deleteBinary($bin);
                    }
                }
                // delete nbm/zip, images
                if ($plugin->image_filename) {
                    @unlink($this->config['content']['filesystem_img_path'] . $plugin->image_filename);
                }
                if ($plugin->fullsize_image_filename) {
                    @unlink($this->config['content']['filesystem_img_path'] . $plugin->fullsize_image_filename);
                }

                // now really whack the plugin and it's asocications
                PpLog::logMe($plugin, NbSsoUser::getInstance()->getUsername(), 'Plugin deleted  id: ' . $plugin->pluginid . ', pubid: ' . $plugin->publicid);
                Doctrine_Query::create()->delete('PpPluginToVersion')->where('plugin_id=?', $plugin->pluginid)->execute();
                $plugin->delete();
                $this->setupFlash('Plugin deleted', 'success');
                $this->invalidateCache();
            }
        }
        $this->_redirect('/my-plugins');
        die();
    }

    public function deleteBinaryAction() {
        $this->rejectGuest();
        if ($this->_getParam('id')) {
            $bin = Doctrine_Core::getTable('PpBinary')->find($this->_getParam('id'));
            // stop processing if owner is not the one logged in :)
            if (($bin->Plugin->author_userid != NbSsoUser::getInstance()->getUsername()) || !$bin) {
                $this->_redirect('/');
                die();
            }
            if ($this->deleteBinary($bin)) {
                $this->setupFlash('Binary deleted', 'success');
            } else {
                $this->setupFlash('Binary deletion failed', 'error');
            }
        }
        $this->_redirect('/my-plugins');
        die();
    }

    private function deleteBinary($bin) {
        // delete nbm/zip
        if ($bin instanceof PpBinary) {
            if ($bin->nbm_filename) {
                @unlink($this->config['content']['filesystem_nbm_path'] . $bin->nbm_filename);
            }
            // update UC for version which was verified for this binary
            if ($bin->Verification) {
                $v = $bin->Verification->version;
                $bin->Verification->delete();
                $catal = new CatalogueAntImplementation();
                $catal->generateCatalogue($v, $this->config['content']['filesystem_nbm_path'], $this->config['java']['tmpDir'], $this->config['java']['home'], $this->config['java']['ant'], $this->config['java']['buildScriptPath']);
                $catal->saveXml($this->config['content']['filesystem_catalogue_path']);
            }
            $bin->delete();
            PpLog::logMe($plugin, NbSsoUser::getInstance()->getUsername(), 'Binary deleted, id: ' . $bin->binary_id . ', pubid: ' . $plugin->publicid);
            $this->invalidateCache();
            return true;
        }
        return false;
    }

    public function myPluginsAction() {
        $this->rejectGuest();
        $this->view->pageTitle = 'My Plugins';
        $this->view->plugins = Doctrine_Query::create()->from('PpPlugin p')
                        ->where('author_userid=?', NbSsoUser::getInstance()->getUsername())
                        ->leftJoin('p.Binaries b')
                        ->andWhere('p.published<2')->andWhere('b.historic=0')
                        ->orderBy('p.plugin_name')->execute();
        $this->initPurifier();
    }

    private function validateFields($post, $flds) {
        foreach ($flds as $f => $m) {
            if (!isset($post[$f]) || empty($post[$f])) {
                $_SESSION['flash']['msg'].='<li>' . $m . '</li>';
                $_SESSION['flash']['type'] = 'error';
                return false;
            }
        }
        return true;
    }

    public function downloadAction() {
        if ($this->_getParam('plugin')) {
            $bin = Doctrine_Core::getTable('PpBinary')->find($this->_getParam('plugin'));
            $plugin = Doctrine_Core::getTable('PpPlugin')->find($bin->plugin_id);
            if ($bin) {
                // save the hit on download but only if not Google bot
                if (!preg_match('/google|spider|crawler|curl|yahoo|archiver|^$/i', $_SERVER['HTTP_USER_AGENT'])) {
                    $plugin->downloads = $plugin->downloads + 1;
                    $plugin->save();
                }
                if (!$bin->nbm_filename && $plugin->home_page_url) {
                    $url = str_replace('javascript', '', htmlspecialchars(strip_tags($plugin->home_page_url)));
                    $url = (strstr($url, 'http://') || strstr($url, 'https://')) ? $url : 'http://' . $url;
                    $this->_redirect($url);
                    die();
                } elseif ($bin->nbm_filename) {
                    $filename = $this->config['content']['filesystem_nbm_path'] . $bin->nbm_filename;
                    if (file_exists($filename)) {
                        // fire up file download
                        header('Content-type: application/octet-stream');
                        header("Content-Length: " . filesize($filename));
                        header('Content-Disposition: attachment; filename="' . basename($filename) . '"');
                        header('Content-Transfer-Encoding: binary');
                        readfile($filename);
                        die();
                    } else {
                        // file missing page
                        $this->view->pageTitle = 'File missing';
                        $this->view->id = $this->_getParam('plugin');
                    }
                }
            }
        }
    }

    public function commentAction() {
        $antiSpam = new AntiSpamValidator($this->config['antispam']['url'], $this->config['sso']['proxyHost'], $this->config['sso']['proxyPort']);
        if(!$antiSpam->validate($this->_getParam('title'), $this->_getParam('comment_summary'), $_SERVER['REMOTE_ADDR'], NbSsoUser::getInstance()->getUsername())) {
            // oups, it's SPAM, notify user and do not continue
            $this->setupFlash("Error: your comment contains SPAM");
            $this->_redirect('/plugin/' . $this->_getParam('plugin'));
            die();
        }
        if ($this->_getParam('plugin') && $this->_ssoUser->getUsername() != 'guest' && $this->_getParam('title') && $this->_getParam('comment_summary')) {
            $recaptcha = new \ReCaptcha\ReCaptcha($this->reCaptchaSecret, null, $this->config['sso']['proxyHost'], $this->config['sso']['proxyPort']);
            $resp = $recaptcha->verify($_POST['g-recaptcha-response'], $_SERVER['REMOTE_ADDR']);
            // check captcha
            if ($resp->isSuccess()) {
                $this->initPurifier();
                $c = new PpComment();
                $c->comment_summary = $this->view->purifier->purify($this->_getParam('comment_summary'));
                $c->title = strip_tags($this->view->purifier->purify($this->_getParam('title')));
                $c->userid = $this->_ssoUser->getUsername();
                $c->plugin_pluginid = $this->_getParam('plugin');
                $c->date_entered = date('Y-m-d');
                $c->datetime_entered = date('Y-m-d H:i:s');
                $c->id = DbAutoincrement::getNewValue();
                $c->save();
                DbAutoincrement::increaseCounter();
                $c->Plugin->sendNotificationMailOnComment($c);
                $this->_redirect('/plugin/' . $this->_getParam('plugin'));
            } else {
                $err = 1;
                $this->setupFlash("Error: you have not selected corect image");
                $this->_redirect('/plugin/' . $this->_getParam('plugin'));
            }
        } else {
            $this->_redirect('/');
        }
    }

    public function commentDeleteAction() {
        if ($this->_getParam('cid') && $this->_getParam('pid')) {
            $comm = Doctrine_Core::getTable('PpComment')->find($this->_getParam('cid'));
            if ($comm) {
                // deleteonly if user is admin or coment owner
		if ($this->_ssoUser->isAuthenticated() && ($comm->Plugin->author_userid == $this->_ssoUser->getUsername() || $comm->userid == $this->_ssoUser->getUsername() || in_array($this->_ssoUser->getUsername(), explode(',', $this->config['admin']['users'])))) {
                    $comm->delete();
                    $this->_redirect('/plugin/' . $this->_getParam('pid') . '#comm');
                    die();
                }
            }
        } else {
            $this->_redirect('/');
        }
    }

    public function commentDetailAction() {
        $this->_helper->layout->disableLayout();
        if ($this->_getParam('cid') && $this->_getParam('pid')) {
            $comm = Doctrine_Core::getTable('PpComment')->find($this->_getParam('cid'));
            if ($comm && ($this->_ssoUser->isAuthenticated() && ($comm->userid == $this->_ssoUser->getUsername() || in_array($this->_ssoUser->getUsername(), explode(',', $this->config['admin']['users']))))) {
                $this->view->c = $comm;
                $this->view->cid = $this->_getParam('cid');
                $this->view->pid = $this->_getParam('pid');
            } else {
                die('You are not authorized to do this.');
            }
        } else {
            die('No comment selected');
        }
    }

    public function commentEditAction() {
        if ($this->_getParam('cid') && $this->_getParam('pid')) {
            $comm = Doctrine_Core::getTable('PpComment')->find($this->_getParam('cid'));
            if ($comm) {
                // deleteonly if user is admin or coment owner
		if ($this->_ssoUser->isAuthenticated() && ($comm->Plugin->author_userid == $this->_ssoUser->getUsername() || $comm->userid == $this->_ssoUser->getUsername() || in_array($this->_ssoUser->getUsername(), explode(',', $this->config['admin']['users'])))) {
                    $this->initPurifier();
                    $comm->comment_summary = $this->view->purifier->purify($this->_getParam('comment_summary'));
                    $comm->title = strip_tags($this->view->purifier->purify($this->_getParam('title')));
                    $comm->save();
                    $this->_redirect('/plugin/' . $this->_getParam('pid') . '#comm');
                    die();
                }
            }
        } else {
            $this->_redirect('/');
        }
    }

    public function rateAction() {
        $this->_helper->layout->disableLayout();
        if ($this->_getParam('plugin') && $this->_getParam('rating') && $this->_ssoUser->getUsername() != 'guest') {
            // check if user did not voted already
            $vote = Doctrine_Query::create()->from('PpRating')
                    ->where('userid=?', $this->_ssoUser->getUsername())
                    ->andWhere('plugin_pluginid=?', $this->_getParam('plugin'))
                    ->fetchOne();
            if (!$vote) {
                // save new vote and update also avg rating
                $v = new PpRating();
                $v->userid = $this->_ssoUser->getusername();
                $v->plugin_pluginid = $this->_getParam('plugin');
                $v->rating_level = $this->_getParam('rating');
                $v->date_rated = date('Y-m-d');
                $v->id = DbAutoincrement::getNewValue();
                $v->save();
                DbAutoincrement::increaseCounter();
                PpPlugin::updateAverageRating($this->_getParam('plugin'));
                echo 'Your rating saved.';
            } else {
                echo 'You already rated.';
            }
        }
        die();
    }

    public function getRatingAction() {
        if ($this->_getParam('plugin')) {
            $plugin = Doctrine_Core::getTable('PpPlugin')->find($this->_getParam('plugin'));
            if ($plugin) {
                echo round($plugin->average_rating, 2) . ', by ' . $plugin->Ratings->count() . ' users';
            }
        }
        die();
    }

    public function logoutAction() {
        // drop session so user is logged out for real
        unset($_SESSION['netbeansSso']);
        setcookie('PHPSESSID', '', time() - 36000);
        // and redir to kenai for logout from junction
        header('Location: https://netbeans.org/people/logout?original_uri=http://plugins.netbeans.org');
        die();
    }

    public function nbmOwnerListAction() {
        $this->_helper->layout->disableLayout();
        $bins = Doctrine_Query::create()->select('b.nbm_filename, p.author_userid')->from('PpBinary b')->innerJoin('b.Plugin p')
                        ->where('b.nbm_filename IS NOT NULL AND b.nbm_filename<>\'\'')->orderBy('b.binary_id')->execute(null, Doctrine_Core::HYDRATE_ARRAY);
        foreach ($bins as $b) {
            if (!strstr($b['nbm_filename'], '.zip')) {
                echo $b['nbm_filename'] . ' ' . $b['Plugin']['author_userid'] . "\n";
            } else {
                // get the list pf modules in the zip :(
                $cmd = escapeshellcmd($this->config['system']['unzip_executable'] . ' -l ' . $this->config['content']['filesystem_nbm_path'] . $bin->nbm_filename) . ' | grep nbm | sed "s/^ *//;s/ *$//;s/ \{1,\}/ /g" | cut -d\' \' -f4';
                //$this->logme($cmd);
                $out = null;
                exec($cmd, $out);
                if (!empty($out)) {
                    foreach ($out as $file) {
                        echo $file . ' ' . $b['Plugin']['author_userid'] . "\n";
                    }
                }
            }
        }
    }

    private function rejectGuest() {
        if (!NbSsoUser::getInstance()->isAuthenticated()) {
            $this->setupFlash('You have to be logged in to submit plugin', 'error');
            $this->_redirect('/');
        }
    }

    private function succesMessage($msg) {
        $this->view->message = '<ul class="success"><li>' . $msg . '</li></ul>';
    }

    private function setupFlash($msg, $type = "success") {
        $_SESSION['flash']['type'] = $type;
        $_SESSION['flash']['msg'].='<li>' . $msg . '</li>';
    }

    private function initPurifier() {
        // load up HTML Purifier
        require_once 'htmlpurifier/HTMLPurifier.standalone.php';
        $config = HTMLPurifier_Config::createDefault();
        $config->set('HTML.Doctype', 'HTML 4.01 Transitional');
        $config->set('Attr.EnableID', true);
        $config->set('Cache.SerializerPath', $this->config['content']['tmp_location_path']);
        $this->view->purifier = new HTMLPurifier($config);
    }

    private function initCache() {    
        // getting a Zend_Cache_Core object
        $cache = Zend_Cache::factory('Output', 'File', $this->frontendOptions, $this->backendOptions);
        //$cache->
        $this->view->cache = $cache;
    }

    private function invalidateCache() {
        Doctrine_Query::create()->delete('Cache')->execute();
    }

}

class Plugins_IndexController extends IndexController {

}

