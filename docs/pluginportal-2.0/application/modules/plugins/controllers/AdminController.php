<?php

class AdminController extends Zend_Controller_Action {

    private $_paginateBy = 30;
    private $_paginatorSlidingRange = 15;

    public function init() {
        /* Initialize action controller here */
        $this->_helper->layout->setLayout('plugins_admin_ui');
        $conf = $this->getInvokeArg('bootstrap')->getOptions();
        $this->config = $conf[$this->_request->getModuleName()];
        $this->view->config = $this->config;
        $this->_ssoUser = NbSsoUser::getInstance($this->config['sso']['cookieName'],
                        $this->config['sso']['ignoredAgents'], $this->config['sso']['validationServiceUrl'],
                        $this->config['sso']['httpClientClass'], $this->config['sso']['refreshSec'],
                        $this->config['sso']['startLog'], $this->config['sso']['proxyHost'],
                        $this->config['sso']['proxyPort']);
        $this->view->ssoUser = $this->_ssoUser;
        $this->rejectNonAdminees();

        // get number of plugins for publishing
        $this->view->toBePublished = Doctrine_Query::create()->select('count(pluginid)')->from('PpPlugin')->where('published=?',
                        '0')->execute(null, Doctrine_Core::HYDRATE_SINGLE_SCALAR);
        
        $this->frontendOptions = array(
            'lifetime' => $this->config['cache']['lifetime'], // cache lifetim
            'automatic_serialization' => true
        );

        $this->backendOptions = array(
            'cache_dir' => $this->config['cache']['storage'], // Directory where to put the cache files
            'file_name_prefix' => 'zendcache'
        );
    }

    public function verifiersActivityAction() {
        $this->view->pageTitle = 'Admin | Verifiers activity';
        $this->view->activity = Doctrine_Query::create()->select('min(r.votedate) as joined, max(r.votedate) as last, count(r.id) as votes, v.userid, r.id')->from('PpVerificationRequest r')->innerJoin('r.Verifier v')->where('votedate IS NOT NULL')->groupBy('r.verifier_id')->orderBy('v.userid')->execute(null,
                Doctrine_Core::HYDRATE_ARRAY);
    }

    public function binaryIntegrityCheckAction() {
        $this->view->pageTitle = 'Admin | Binary Integrity Check';
        $bins = Doctrine_Query::create()->from('PpBinary b')->innerJoin('b.Plugin p')->orderBy('b.updated_on DESC')->execute(null,
                Doctrine_Core::HYDRATE_ARRAY);
        $missing = array();
        foreach ($bins as $b) {
            if (!empty($b['nbm_filename'])) {
                if (!file_exists($this->config['content']['filesystem_nbm_path'] . $b['nbm_filename'])) {
                    $missing[] = $b;
                }
            }
        }
        $this->view->missing = $missing;
    }

    public function freeSpaceAction() {
        // look for all binaries which are of same plugin_id, version_id and historic=1, and dump all except latest one
        $bins = Doctrine_Query::create()->select('b.plugin_id, b.binary_id, b.download_size, b.version_id, b.updated_on, b.nbm_filename')
                ->from('PpBinary b')
                //->where('b.plugin_id=?', '43317')
                ->where('b.historic=1')
                ->orderBy('b.plugin_id, b.version_id, b.binary_id desc')
                ->execute(null, Doctrine_Core::HYDRATE_ARRAY);
        $savedSize = 0;
        $lastPlugin = null;
        $lastVersion = null;
        echo 'CELKEM: '.count($bins).'<br/>';
        foreach ($bins as $b) {
            if ($lastPlugin !== $b['plugin_id']) {
                $lastPlugin = $b['plugin_id'];
                $lastVersion = $b['version_id'];
                echo '<div style="color: green">LP: '.$lastPlugin.'; LV: '.$lastVersion.' -- P:'.$b['plugin_id'].'; V: '.$b['version_id'].'; B: '.$b['binary_id']." - NEW P, NEW V, keeping</div>";
                continue;
            }
            if($lastVersion != $b['version_id']) {
                $lastVersion = $b['version_id'];
                echo '<div style="color: green">LP: '.$lastPlugin.'; LV: '.$lastVersion.' -- P:'.$b['plugin_id'].'; V: '.$b['version_id'].'; B: '.$b['binary_id']." - SAME P, NEW V, keeping</div>";
                continue;
            }
            // delete any remaining
            echo '<div style="color: red">LP: '.$lastPlugin.'; LV: '.$lastVersion.' -- P:'.$b['plugin_id'].'; V: '.$b['version_id'].'; B: '.$b['binary_id']." - SAME P, SAME V, deleting</div>";
            $size = filesize($this->config['content']['filesystem_nbm_path'] . $b['nbm_filename']);
            $savedSize+=$size;
            //$res=unlink($this->config['content']['filesystem_nbm_path'].$file);
        }
        echo '<br/><hr/><br/>';
        echo '<hr/>SAVED: ' . round($savedSize / (1024 * 1000), 3) . ' MB';
        die();
    }

    public function migrateV2Action() {
        $unknownVersion = array();
        $migratedFromVersion = 0;
        $migratedFromVerification = 0;
        //ALTER TABLE `log` ADD `binary_id` INT NULL
        //ALTER TABLE `verificationimpl` ADD `binary_id` INT NULL
        // dump Binary first
        $conn = Doctrine_Manager::getInstance()->getConnection('plugins');
        //$conn->execute('TRUNCATE TABLE binaryimpl');

        echo "<h2>V2 DB migration</h2>";
        // take all published plugins
        $plugins = Doctrine_Query::create()->from('PpPlugin p')
                ->leftJoin('p.Versions')->leftJoin('p.Verifications')
                ->where('p.pluginid IN (6416,6152,36000,28922,1435,2651,6664)')
                ->execute(null, Doctrine_Core::HYDRATE_ARRAY);
        foreach ($plugins as $p) {
            $vers = array();
            echo $p['publicid'] . ' : ' . $p['plugin_name'] . '<br/>';
            // check versions and also verifications and merge them to one list of versions
            if (!empty($p['Versions'])) {
                foreach ($p['Versions'] as $v) {
                    $vers[$v['version']] = $v['id'];
                }
            }
            if (!empty($p['Verifications'])) {
                foreach ($p['Verifications'] as $v) {
                    $version = Doctrine_Query::create()->from('PpNetbeansVersion v')->where('v.version=?', $v['version'])->fetchOne();
                    $vers[$version->version] = $version->id;
                }
            }
            // manual version fix for 7.1 Geertjan;'s plugins
            $vers['7.1'] = 40181;
            if (!empty($vers)) {
                $migratedFromVersion++;
                // plugin has some version assigned
                foreach ($vers as $version => $vid) {
                    // create binary record
                    $bin = new PpBinary();
                    $bin->plugin_id = $p['pluginid'];
                    $bin->version_id = $vid;
                    $bin->updated_on = $p['date_last_updated'];
                    $bin->signed = $p['signed'];
                    $bin->nbm_filename = $p['nbm_filename'];
                    $bin->nbm_url = $p['nbm_url'];
                    $bin->download_size = $p['download_size'];
                    $bin->whats_new = $p['new_version_comments'];
                    $bin->save();
                    echo ' ... ' . $version;
                    // check for verification and link it ot binary as well
                    $ver = Doctrine_Query::create()->select()->from('PpVerification')
                                    ->where('plugin_pluginid=?', $p['pluginid'])->andWhere('version=?', $version)->fetchOne();
                    if ($ver) {
                        $ver->binary_id = $bin->binary_id;
                        $ver->save();
                        // now check if the plugin is zip and unpack it and prefix it with binary id
                        if (strstr($p['nbm_filename'], '.zip') && $ver->status == 1) {
                            $bin->unpackModulePack($this->config['content']['tmp_location_path'],
                                    $this->config['content']['filesystem_nbm_path'],
                                    $this->config['system']['unzip_executable']);
                            echo ' ... it is verified .zip - unpacking and prefixing with binary_id';
                        }
                    }
                }
            } else {
                $unknownVersion[] = $p['plugin_name'];
                $unknownVersionByUser[$p['author_userid']] = $p['plugin_name'];
            }
            echo '<hr/>';
        }
        echo '<hr/><hr/>';
        echo 'Migrated from version: ' . $migratedFromVersion . '<br/>';
        echo 'Unable to guess from Version or Verification: ' . count($unknownVersion) . '<hr>';
        arsort($unknownVersionByUser);
        foreach ($unknownVersionByUser as $u => $l) {
            echo $u . ' : ' . count($l) . ' plugins<br/>';
        }
        echo '<hr/>';
        foreach ($unknownVersion as $l) {
            echo $l . '<br/>';
        }
        echo '<hr/>';
        die();
    }

    // old method of UC generation
    public function generateCatalogueAction() {
        $catal = new Catalogue();
        $catal->generateCatalogue($this->_getParam('version'), $this->config['content']['filesystem_nbm_path'],
                $this->config['content']['tmp_location_path'], $this->config['system']['unzip_executable'],
                $this->config['content']['www_nbm_path']);
        if ($this->_getParam('screen')) {
            $catal->renderXml();
        } else {
            $msg = $catal->saveXml($this->config['content']['filesystem_catalogue_path']);
            echo 'Catalogue for version ' . $this->_getParam('version') . ' generated and saved - <a href="' . $msg . '" target="_blank" style="color:blue">' . $msg . '</a><hr/>';
            echo implode('<br/>', $catal->log);
        }
        die();
    }

    public function generateCatalogue2Action() {
        $catal = new CatalogueAntImplementation();
        $catal->generateCatalogue($this->_getParam('version'), $this->config['content']['filesystem_nbm_path'],
                $this->config['java']['tmpDir'], $this->config['java']['home'], $this->config['java']['ant'],
                $this->config['java']['buildScriptPath']);
        if ($this->_getParam('screen')) {
            $catal->renderXml();
        } else {
            $msg = $catal->saveXml($this->config['content']['filesystem_catalogue_path'], 'catalog.xml');
            echo 'Catalogue for version ' . $this->_getParam('version') . ' generated and saved - <a href="' . $msg . '" target="_blank" style="color:blue">' . $msg . '</a><hr/>';
            echo implode('<br/>', $catal->log);
        }
        die();
    }

    // old method of UC generation
    public function generateExperimentalCatalogueAction() {
        $catal = new Catalogue();
        $catal->generateExperimentalCatalogue($this->_getParam('version'),
                $this->config['content']['filesystem_nbm_path'], $this->config['content']['tmp_location_path'],
                $this->config['system']['unzip_executable'], $this->config['content']['www_nbm_path']);
        if ($this->_getParam('screen')) {
            $catal->renderXml();
        } else {
            $msg = $catal->saveXml($this->config['content']['filesystem_experimental_catalogue_path'],
                    'catalog-experimental.xml');
            echo 'Catalog for version ID ' . $this->_getParam('version') . ' generated and saved - <a href="' . $msg . '" target="_blank" style="color:blue">' . $msg . '</a><hr/>';
            echo implode('<br/>', $catal->log);
        }
        die();
    }

    public function generateExperimentalCatalogue2Action() {
        $catal = new CatalogueAntImplementation();
        $catal->generateExperimentalCatalogue($this->_getParam('version'),
                $this->config['content']['filesystem_nbm_path'], $this->config['java']['tmpDir'],
                $this->config['java']['home'], $this->config['java']['ant'], $this->config['java']['buildScriptPath']);
        if ($this->_getParam('screen')) {
            //die(print_r($catal->log));
            $catal->renderXml();
        } else {
            $msg = $catal->saveXml($this->config['content']['filesystem_experimental_catalogue_path'],
                    'catalog-experimental.xml');
            echo 'Catalog for version ID ' . $this->_getParam('version') . ' generated and saved - <a href="' . $msg . '" target="_blank" style="color:blue">' . $msg . '</a><hr/>';
            echo implode('<br/>', $catal->log);
        }
        die();
    }

    public function indexAction() {
        $this->view->pageTitle = 'Admin | Plugin Portal';
    }

    public function logsAction() {
        $this->view->pageTitle = 'Admin | Log viewer';
    }

    public function ajaxLogsAction() {
        $this->initPurifier();
        $this->_helper->layout->disableLayout();
        $ret = array();
        $numAllLogs = Doctrine_Query::create()->select('count(id) as c')->from('PpLog')->execute(null,
                Doctrine_Core::HYDRATE_SINGLE_SCALAR);
        // setup base query
        $logQr = Doctrine_Query::create()
                ->from('PpLog as l');
        //->leftJoin('l.Plugin as p')
        //->orderBy('l.id desc, l.stamp desc');
        // prepare pagination variables
        $paginate = ($this->_getParam('iDisplayLength')) ? $this->_getParam('iDisplayLength') : $this->_paginateBy;
        // watch out, DT are not sending page number but offset, so recalc it to Doctrine Pager page number
        $page = $this->_getParam('iDisplayStart') / $paginate + 1;

        $search = $this->_getParam('sSearch');
        if (!empty($search)) {
            $s = html_entity_decode($this->_getParam('sSearch'));
            //$logQr->leftJoin('l.Plugin as p')
            $logQr->where(' l.plugin_name LIKE ? OR l.userid LIKE ? OR l.stamp LIKE ?',
                    array('%' . $s . '%', '%' . $s . '%', '%' . $s . '%'));
        }
        // Paginate
        $logQr->limit($paginate);
        $logQr->offset($this->_getParam('iDisplayStart'));
        // Sorting
        if ($this->_getParam('iSortCol_0')) {
            $sortdir = ($this->_getParam('sSortDir_0')) ? $this->_getParam('sSortDir_0') : 'desc';
            switch ($this->_getParam('iSortCol_0')) {
                case 0: $col = 'l.id';
                    break;
                case 1: $col = 'l.stamp';
                    break;
                case 2: $col = 'p.plugin_name';
                    $logQr->leftJoin('l.Plugin as p');
                    break;
                case 3: $col = 'l.userid';
                    break;
                case 4: $col = 'l.log';
                    break;
            }
            $logQr->orderBy($col . ' ' . $sortdir);
        } else {
            $logQr->orderBy('l.id desc, l.stamp desc');
        }
        $items = $logQr->execute();
        // setup return data for dataTables
        $ret['sEcho'] = $this->_getParam('sEcho');
        $ret['iTotalRecords'] = $numAllLogs;
        $ret['iTotalDisplayRecords'] = $numAllLogs;
        $aa = array();
        if ($items->count() > 0) {
            foreach ($items as $i) {
                $a[] = $i->id;
                $a[] = $i->stamp;
                $a[] = ($i->plugin_name) ? htmlentities($i->plugin_name) : '';
                $a[] = $i->userid;
                $a[] = '<b>' . $i->log . '</b>';
                $aa[] = $a;
                unset($a);
            }
        }

        $ret['aaData'] = $aa;
        // send JSON and DIE
        echo json_encode($ret);
        die();
    }

    public function publishingAction() {
        $this->initPurifier();
        $this->view->pageTitle = "Admin | Publishing of new plugins";
        $this->view->plugins = Doctrine_Query::create()->from('PpPlugin')
                        ->where('published=?', '0')
                        ->orderBy('plugin_name')->execute();
    }

    public function deletePluginAction() {
        if ($this->_getParam('id')) {
            $plugin = Doctrine_Core::getTable('PpPlugin')->find($this->_getParam('id'));
            // delete nbm/zip, images
            if ($plugin) {
                if ($plugin->nbm_filename) {
                    unlink($this->config['content']['filesystem_nbm_path'] . $plugin->nbm_filename);
                }
                if ($plugin->image_filename) {
                    unlink($this->config['content']['filesystem_img_path'] . $plugin->image_filename);
                }
                if ($plugin->fullsize_image_filename) {
                    unlink($this->config['content']['filesystem_img_path'] . $plugin->fullsize_image_filename);
                }
                // update UC for all versions which were verified for this plugin
                if ($plugin->Verifications) {
                    $vrfs = $plugin->Verifications->toArray();
                    $plugin->Verifications->delete();
                    foreach ($vrfs as $v) {
                        $catal = new CatalogueAntImplementation();
                        $catal->generateCatalogue($v['version'], $this->config['content']['filesystem_nbm_path'],
                                $this->config['java']['tmpDir'], $this->config['java']['home'],
                                $this->config['java']['ant'], $this->config['java']['buildScriptPath']);
                        $catal->saveXml($this->config['content']['filesystem_catalogue_path']);
                    }
                }
                // delete data
                Doctrine_Query::create()->delete('PpPluginToVersion')->where('plugin_id=?', $plugin->pluginid)->execute();
                PpLog::logMe($plugin, NbSsoUser::getInstance()->getUsername(), 'Plugin deleted');
                $plugin->delete();
                $_SESSION['flash']['msg'] = '<li>Plugin deleted</li>';
                $_SESSION['flash']['type'] = 'success';
                $back = ($this->_getParam('land')) ? $this->_getParam('land') : 'publishing';
                $this->invalidateCache();
                $this->invalidateZendCache();
                $this->_redirect('admin/' . $back);
                die();
            }
        }
    }

    public function approvePluginAction() {
        if ($this->_getParam('id') && $this->_getParam('approve')) {
            $plugin = Doctrine_Core::getTable('PpPlugin')->find($this->_getParam('id'));
            if ($plugin) {
                if ($this->_getParam('approve') == 'y') {
                    $plugin->published = 1;
                    PpLog::logMe($plugin, NbSsoUser::getInstance()->getUsername(), 'Plugin published');
                    $_SESSION['flash']['msg'] = '<li>Plugin <i>' . htmlspecialchars($plugin->plugin_name) . '</i> published</li>';
                } elseif ($this->_getParam('approve') == 'n') {
                    $plugin->published = '0';
                    PpLog::logMe($plugin, NbSsoUser::getInstance()->getUsername(), 'Plugin unpublished');
                    $_SESSION['flash']['msg'] = '<li>Plugin <i>' . htmlspecialchars($plugin->plugin_name) . '</i> unpublished</li>';
                }
                $plugin->save();

                // now put it right to the experimental catalog, but extract zips if it has such binaries
                foreach ($plugin->Binaries as $bin) {
                    if (strstr($bin->nbm_filename, '.zip')) {
                        $bin->unpackModulePack($this->config['content']['tmp_location_path'],
                                $this->config['content']['filesystem_nbm_path'],
                                $this->config['system']['unzip_executable']);
                    }
                }
                $vers = $plugin->getNetbeansVersionsAsArray();
                foreach ($vers as $version) {
                    $catal = new CatalogueAntImplementation();
                    $catal->generateExperimentalCatalogue($version, $this->config['content']['filesystem_nbm_path'],
                            $this->config['java']['tmpDir'], $this->config['java']['home'],
                            $this->config['java']['ant'], $this->config['java']['buildScriptPath']);
                    $msg = $catal->saveXml($this->config['content']['filesystem_experimental_catalogue_path'],
                            'catalog-experimental.xml');
                    $_SESSION['flash']['msg'] .='<li>plugin put on experimental catalog ' . $msg . '</li>';
                }
                $_SESSION['flash']['type'] = 'success';
                $this->invalidateCache();
                $this->invalidateZendCache();
                $this->_redirect('/admin/' . $this->_getParam('land'));
            }
        }
    }

    public function pluginListAction() {
        $this->initPurifier();
        $this->view->pageTitle = 'Admin | Plugins Repository';
        $this->view->categories = Doctrine_Query::create()->from('PpDisplayCategory')->orderBy('displaycategory_name')->useResultCache(true)->execute(null,
                Doctrine_Core::HYDRATE_ARRAY);
        $this->view->versions = Doctrine_Query::create()->from('PpNetbeansVersion')->orderBy('version')->useResultCache(true)->execute(null,
                Doctrine_Core::HYDRATE_ARRAY);
        $this->view->plugins = Doctrine_Query::create()->from('PpPlugin p')->leftJoin('p.Category')->leftJoin('p.Versions')->innerJoin('p.Binaries b')->innerJoin('b.Version')
                        ->where('p.published=1')->andWhere('b.historic=0')->execute(null, Doctrine_Core::HYDRATE_ARRAY);
    }

    public function pluginCategoriesAction() {
        $this->view->pageTitle = "Plugin Categories Admin";
        $this->view->pc = Doctrine_Query::create()->from('PpDisplayCategory')->orderBy('displaycategory_name')->execute();
    }

    public function pluginCategoryDeleteAction() {
        if ($this->_getParam('id')) {
            $pc = Doctrine_Core::getTable('PpDisplayCategory')->find($this->_getParam('id'));
            if ($pc) {
                $pc->delete();
                $this->invalidateCache();
                $this->succesMessage('Category deleted');
            }
        } else {
            $this->errorMessage('Missing Category ID');
        }
        $this->_forward('plugin-categories');
    }

    public function pluginCategoryEditAction() {
        if ($this->_request->isPost()) {
            $data = $this->_getParam('pc');
            if ($data) {
                if ($data['id']) {
                    $pcc = Doctrine_Core::getTable('PpDisplayCategory')->find($data['id']);
                } else {
                    $pcc = new PpDisplayCategory();
                    $pcc->id = DbAutoincrement::getNewValue();
                    DbAutoincrement::increaseCounter();
                }
                $pcc->displaycategory_name = $data['displaycategory_name'];
                $pcc->save();
                $this->invalidateCache();
                $this->succesMessage('Item saved');
                $this->_setParam('id', $pcc->id);
            }
        }
        if ($this->_getParam('id')) {
            $pc = Doctrine_Core::getTable('PpDisplayCategory')->find($this->_getParam('id'));
            if ($pc) {
                $this->view->heading = 'Edit Plugin Category: ' . $pc->displaycategory_name;
                $this->view->pc = $pc;
            }
        } else {
            $this->view->heading = 'Add Plugin Category';
        }
    }

    public function nbVersionsAction() {
        $this->view->pageTitle = "NetBeans Version Admin";
        $this->view->vers = Doctrine_Query::create()->from('PpNetbeansVersion')->orderBy('version desc')->execute();
    }

    public function nbVersionDeleteAction() {
        if ($this->_getParam('id')) {
            $pc = Doctrine_Core::getTable('PpNetbeansVersion')->find($this->_getParam('id'));
            if ($pc) {
                $pc->delete();
                $this->succesMessage('Version deleted');
                $this->invalidateCache();
            }
        } else {
            $this->errorMessage('Missing Version ID');
        }
        $this->_forward('nb-versions');
    }

    public function nbVersionEditAction() {
        if ($this->_request->isPost()) {
            $data = $this->_getParam('pc');
            if ($data) {
                if ($data['id']) {
                    $pcc = Doctrine_Core::getTable('PpNetbeansVersion')->find($data['id']);
                } else {
                    $pcc = new PpNetbeansVersion();
                    $pcc->id = DbAutoincrement::getNewValue();
                    DbAutoincrement::increaseCounter();
                }
                $pcc->version = $data['version'];
                $pcc->verifable = $data['verifable'];
                $pcc->save();
                $this->invalidateCache();
                $this->succesMessage('Item saved');
                $this->_setParam('id', $pcc->id);
                // send notification about new versin to Marian
                if (!$data['id']) {
                    $transport = new Zend_Mail_Transport_Smtp('localhost');
                    Zend_Mail::setDefaultFrom('geertjan@apache.org', 'Geertjan Wielenga ');
                    Zend_Mail::setDefaultReplyTo('geertjan@apache.org', 'Geertjan Wielenga ');
                    $mail = new Zend_Mail();
                    $mail->addTo('geertjan@apache.org', '');
                    $mail->setSubject('New UC catalog on Plugin Portal');
                    $mail->setBodyText('Cau Marian,
this is an automatic email to inform you, that there is new NetBeans version added to
the pluginportal and that there will be new UC catalog for that version available
once some plugin gets verified for that version.

Catalog url will be: http://plugins.netbeans.org/nbpluginportal/updates/' . $data['version'] . '/catalog.xml.gz

Jirka');
                    $mail->send($transport);
                }
            }
        }
        if ($this->_getParam('id')) {
            $pc = Doctrine_Core::getTable('PpNetbeansVersion')->find($this->_getParam('id'));
            if ($pc) {
                $this->view->heading = 'Edit NetBeans Version: ' . $pc->version;
                $this->view->pc = $pc;
            }
        } else {
            $this->view->heading = 'AddNetBeans Version';
        }
    }

    public function verifiersAction() {
        $this->view->pageTitle = "Plugin Verifiers";
        $this->view->ver = Doctrine_Query::create()->from('PpVerifier')->orderBy('userid')->execute();
    }

    public function verifierDeleteAction() {
        if ($this->_getParam('id')) {
            $v = Doctrine_Core::getTable('PpVerifier')->find($this->_getParam('id'));
            if ($v) {
                PpLog::logMe(null, NbSsoUser::getInstance()->getUsername(), 'Verifier removed: ' . $v->userid);
                $v->delete();
                $this->succesMessage('Verifier deleted');
            }
        } else {
            $this->errorMessage('Missing Version ID');
        }
        $this->_forward('verifiers');
    }

    public function verifierAddAction() {
        if ($this->_getParam('userid')) {
            $v = new PpVerifier();
            $v->userid = $this->_getParam('userid');
            $v->id = DbAutoincrement::getNewValue();
            $v->save();
            PpLog::logMe(null, NbSsoUser::getInstance()->getUsername(), 'Added new verifier: ' . $v->userid);
            DbAutoincrement::increaseCounter();
            $this->succesMessage('Verifier added');
            // if there are any undecided verifications for any plugin, let's also generate requests for this new verifier
            $verificatons = Doctrine_Query::create()->from('PpVerification')->where('status=?', '0')->orderBy('id desc')->execute();
            if ($verificatons->count() > 0) {
                $verifiable = Doctrine_Query::create()->select('version')->from('PpNetbeansVersion')->where('verifable=?',
                                1)->orderBy('version desc')->execute(null, Doctrine_Core::HYDRATE_SINGLE_SCALAR);
                foreach ($verificatons as $ver) {
                    if (in_array($ver->version, $verifiable)) {
                        $vr = new PpVerificationRequest();
                        $vr->verification_id = $ver->id;
                        $vr->verifier_id = $v->id;
                        $vr->requestdate = date('Y-m-d H:i:s');
                        $vr->id = DbAutoincrement::getNewValue();
                        $vr->save();
                        PpLog::logMe(null, NbSsoUser::getInstance()->getUsername(),
                                'Generating verification request for new verifier ' . $v->userid . ' for undecided verification id:' . $vr->verification_id);
                        DbAutoincrement::increaseCounter();
                    }
                }
            }
            $this->_forward('verifiers');
        }
        $this->_forward('verifiers');
    }

    /**
     * holy shooot, there was txt input fr specification of nb versions, so it's now complete mess
     * this method will yty to migrate it to 1:N plugin:version DB scheme.. may the force be with us!
     */
    public function migrateVersionsAction() {
        $log = array();
        // truncate the join table so we can run the script again and again
        $conn = Doctrine_Manager::getInstance()->getConnection('plugins');
        $conn->execute('TRUNCATE TABLE plugin_version');

        $this->view->pageTitle = 'Versions Migration';
        $plugins = Doctrine_Query::create()->select('pluginid, versions_supported')->from('PpPlugin')->orderBy('pluginid')->execute(null,
                Doctrine_Core::HYDRATE_ARRAY);
        $versionsq = Doctrine_Query::create()->select('id, version')->from('PpNetbeansVersion')->execute(null,
                Doctrine_Core::HYDRATE_ARRAY);
        // make better array for versions checking
        $versions = array();
        foreach ($versionsq as $v) {
            $versions[$v['version']] = $v['id'];
        }
        // here comes main magic
        foreach ($plugins as $p) {
            //$log[]='Processing plugin '.$p['pluginid'];
            //$v=str_ireplace(array('*','+','x','netbeans'),'',$p['versions_supported'])
            $vrs = explode(' ',
                    trim(preg_replace('#[\ ]+#', ' ', preg_replace('#[^\d|\.|\ ]#i', ' ', $p['versions_supported']))));
            foreach ($vrs as $v) {
                $v = preg_replace('#^(\d)\.$#', '\1.0', $v);
                $v = preg_replace('#^(\d)$#', '\1.0', $v);
                $v = trim($v, '.');
                $cleanedVersions[$v]+=1;
                if ($versions[$v]) {
                    // we have match with official versions in DB, let's save
                    //echo 'saving '.$p['pluginid'].' - '.$v.'<br>';
                    try {
                        $conn->execute('INSERT INTO plugin_version VALUES (' . $p['pluginid'] . ',' . $versions[$v] . ')');
                    } catch (Exception $e) {
                        echo $e->getMessage() . '<br>';
                    }
                }
            }
        }
        var_dump($cleanedVersions);
        $this->view->report = $log;
    }

    /**
     * go through all plugins with .nbm/zips and check if they are signed and save that info into db so users can request verification
     */
    public function migrateValidatePluginSignaturesAction() {
        $plugins = Doctrine_Query::create()->from('PpPlugin')->where('nbm_filename IS NOT NULL')->andWhere('signed=0')->execute();
        foreach ($plugins as $p) {
            $pTmp = new PpPlugin();
            $pTmp->nbm_filename = $p->nbm_filename;
            if (substr($p->nbm_filename, -4, 4) == '.nbm') {
                $pTmp->unpackPluginAndLoadInfoXML(substr($pTmp->nbm_filename, 0, -4),
                        $this->config['content']['filesystem_nbm_path'] . $pTmp->nbm_filename,
                        $this->config['content']['tmp_location_path'], $this->config['system']['unzip_executable']);
            } else {
                $pTmp->unpackPluginPackAndVerifySignatures(substr($pTmp->nbm_filename, 0, -4),
                        $this->config['content']['filesystem_nbm_path'] . $pTmp->nbm_filename,
                        $this->config['content']['tmp_location_path'], $this->config['system']['unzip_executable']);
            }
            echo $pTmp->nbm_filename . ' - signed: ' . $pTmp->signed . '<br/>';
            if ($pTmp->signed == 1) {
                $p->signed = 1;
                $p->save();
            }
            unset($pTmp);
        }
        die('<br/><br/>END');
    }

    /**
     * migrate old categs to new schema
     */
    public function migrateCategoriesAction() {
        require_once 'category-migration-mapping.php';
        // first truncate and load up categories
        Doctrine_Query::create()->delete()->from('PpDisplayCategory')->execute();
        $newIds = array();
        foreach ($categs as $cat) {
            $c = new PpDisplayCategory();
            $c->displaycategory_name = $cat;
            $c->id = DbAutoincrement::getNewValue();
            $c->save();
            $newIds[$cat] = $c->id;
            DbAutoincrement::increaseCounter();
        }
        $plugins = Doctrine_Query::create()->from('PpPlugin')->where('displaycategory IS NOT NULL')->execute();
        foreach ($plugins as $p) {
            if ($p->displaycategory) {
                if ($mapping[$p->displaycategory]) {
                    echo $p->plugin_name . ': found mapping ' . $p->displaycategory . '->' . $mapping[$p->displaycategory] . ' - id: ' . $newIds[$mapping[$p->displaycategory]] . '<br/>';
                    $p->categoryid = $newIds[$mapping[$p->displaycategory]];
                    $p->save();
                } else {
                    echo $p->plugin_name . ': <b>NOT FOUND mapping</b><br/>';
                    $p->categoryid = $newIds[$mapping['Uncategorized']];
                    $p->save();
                }
            }
        }
        die();
    }

    /**
     * recalculate ratings from average to bayesian average, ucing coef 0.75
     * BA = ((AvgNumOfRatingsForAll * 0.75 * AvgRatingForAll) + TotalRating) / (RateCount + (AvgNumOfRatingsForAll * 0.75))
     */
    public function migrateRecalculateRatingsAction() {
        $const = 3;
        // AvgNumOfRatingsForAll is the average number of ratings for all books shown (where RateCount > 0)
        $allVotes = Doctrine_Query::create()->select('count(id)')->from('PpRating')->fetchOne(null,
                Doctrine_Core::HYDRATE_SINGLE_SCALAR);
        $allPlugins = Doctrine_Query::create()->select('pluginid')->from('PpPlugin')->groupBy('publicid')->execute();
        $AvgNumOfRatingsForAll = $allVotes / $allPlugins->count();

        // AvgRatingForAll is the average unweighted rating for all books shown (where RateCount >
        $pl = Doctrine_Query::create()->select('plugin_pluginid, count(id) as c, sum(rating_level) as s')->from('PpRating')->groupBy('plugin_pluginid')->execute();
        $n = 0;
        $sumAvg = 0;
        foreach ($pl as $p) {
            $n+=$p->c;
            $sumAvg+=$p->s; // / $p->c;
        }
        $AvgRatingForAll = $sumAvg / $n;
        // now recalculate each rated
        $pl = Doctrine_Query::create()->select('plugin_pluginid, count(id) as c, sum(rating_level) as s')->from('PpRating r')->groupBy('plugin_pluginid')->execute();
        foreach ($pl as $p) {
            $bayessAvg = (($AvgNumOfRatingsForAll * $const * $AvgRatingForAll) + $p->s) / ($p->c + ($AvgNumOfRatingsForAll * $const));
            // check if plugin still exists
            $plug = Doctrine_Query::create()->from('PpPlugin')->where('pluginid=?', $p->plugin_pluginid)->fetchOne();
            if (!empty($plug)) {
                echo 'pluginId: ' . $p->plugin_pluginid . ' : votes ' . $p->c . '; orig ' . round($plug->average_rating,
                        3) . '; bayess ' . round($bayessAvg, 3) . '<br/>';
                $plug->average_rating = round($bayessAvg, 3);
                $plug->save();
                Doctrine_Query::create()->update('PpPlugin p')->set('p.average_rating', '?', $bayessAvg)->where('publicid=?',
                        $plug->publicid)->execute();
            }
        }
        die();
    }

    public function miscAction() {
        if ($this->_request->isPost()) {
            if ($this->_getParam('featuredplugin')) {
                Doctrine_Query::create()->update('PpPreference')->set('featuredplugin=?',
                        (int) $this->_getParam('featuredplugin'))->execute();
                $this->succesMessage('Featured Plugin updated');
                $this->invalidateCache();
            }
        }
        $this->view->vers = Doctrine_Query::create()->from('PpNetbeansVersion')->orderBy('version desc')->execute();
        $this->initPurifier();
        $this->view->fpSelect = Doctrine_Query::create()->select('publicid, plugin_name')->from('PpPlugin')
                        ->where('publicid=?', PpPreference::preference()->featuredplugin)->fetchOne();
        // stats on plugins
        $pdo = Doctrine_Manager::getInstance()->getCurrentConnection(); //->getDbh();
        $qr = "select mesic, count(mesic) as publikovano from (select date_format(date_added,'%Y-%m') as mesic from pluginimpl where date_added>='2011-02-01' group by publicid, date_format(date_added,'%Y-%m')) as xx group by mesic order by mesic desc";
        $stmt = $pdo->execute($qr);
        $this->view->publishingStats = $stmt->fetchAll();

        // verification requests stats
        $qr = "select mesic, count(mesic) as verif from(select date_format(requestdate,'%Y-%m') as mesic from verificationrequestimpl where requestdate>='2011-02-01' group by verification_id, date_format(requestdate,'%Y-%m')) as xx group by mesic order by mesic desc";
        $stmt = $pdo->execute($qr);
        $this->view->verificationStats = $stmt->fetchAll();

        // number of owners for new ide version notification
        //  * were updated in the previous 12 months
        //  * belong to the Top 10 of the most downloaded plugins
        //  * belong to the Top 10 of the highest ranked plugins
        $this->view->owners = $this->getOwnersForNewVersionNotification();
    }

    public function masterVoteAction() {
        if ($this->_request->isPost()) {
            if ($this->_getParam('vid')) {
                $verif = Doctrine_Core::getTable('PpVerification')->find($this->_getParam('vid'));
                if ($verif) {
                    switch ($this->_getParam('vote')) {
                        case -1:
                            $s = 'NoGo';
                            break;
                        case 1:
                            $s = 'Go';
                            // unzip pack if it is pack
                            if (strstr($verif->Binary->nbm_filename, '.zip')) {
                                $verif->Binary->unpackModulePack($this->config['content']['tmp_location_path'],
                                        $this->config['content']['filesystem_nbm_path'],
                                        $this->config['system']['unzip_executable']);
                            }
                            break;
                        default:
                            $s = 'Undecided';
                            break;
                    }
                    $verif->status = $this->_getParam('vote');
                    // now save it and generate UC for that version
                    $verif->save();
                    // drop verifications for prev versions of this binary, so it does not show up any more on UC
                    Doctrine_Query::create()->delete('PpVerificationRequest')->where('verification_id IN (select id from verificationimpl where version=? and plugin_pluginid=' . $verif->plugin_pluginid . ' AND id<>' . $verif->id . ')',
                            $verif->version)->execute();
                    Doctrine_Query::create()->delete('PpVerificationToRequest')->where('verificationImpl_ID IN (select id from verificationimpl where version=? and plugin_pluginid=' . $verif->plugin_pluginid . ' AND id<>' . $verif->id . ')',
                            $verif->version)->execute();
                    Doctrine_Query::create()->delete('PpVerification')->where('version=?', $verif->version)->andWhere('plugin_pluginid=? AND id<>' . $verif->id,
                            $verif->plugin_pluginid)->execute();

                    $catal = new CatalogueAntImplementation();
                    $catal->generateCatalogue($verif->version, $this->config['content']['filesystem_nbm_path'],
                            $this->config['java']['tmpDir'], $this->config['java']['home'],
                            $this->config['java']['ant'], $this->config['java']['buildScriptPath']);
                    $catal->saveXml($this->config['content']['filesystem_catalogue_path'], 'catalog.xml');
                    PpLog::logMe($verif->Plugin, NbSsoUser::getInstance()->getUsername(),
                            'MASTER Go/NoGo casted for ' . $verif->version . ' verification by admin ' . NbSsoUser::getInstance()->getUsername() . ': ' . $s);
                    echo 'Verification changed, catalog re-generated';
                    die();
                }
            }
        }
    }

    public function ajaxVerificationsAction() {
        $this->_helper->layout->disableLayout();
        $this->view->verifications = Doctrine_Query::create()->select()->from('PpVerification')->where('plugin_pluginid=?',
                        $this->_getParam('vid'))->execute();
    }

    public function featuredSuggestAction() {
        if ($this->_getParam('term')) {
            $hit = Doctrine_Query::create()->from('PpPlugin')
                    ->where('published=1')->andWhere('plugin_name LIKE ?', '%' . $this->_getParam('term') . '%')
                    ->execute();
            $res = array();
            foreach ($hit as $h) {
                $res[] = array('id' => $h->publicid, 'label' => $h->plugin_name, 'value' => $h->plugin_name);
            }
            echo json_encode($res);
        }
        die();
    }

    public function pluginSuggestAction() {
        if ($this->_getParam('term')) {
            $hit = Doctrine_Query::create()->from('PpPlugin')
                    ->where('published=1')->andWhere('plugin_name LIKE ?', '%' . $this->_getParam('term') . '%')
                    ->execute();
            $res = array();
            foreach ($hit as $h) {
                $res[] = array('id' => $h->pluginid, 'label' => $h->plugin_name, 'value' => $h->plugin_name);
            }
            echo json_encode($res);
        }
        die();
    }

    public function dependencyChecklistAction() {
        $this->view->pageTitle = 'Admin | List of recent plugins with hard Dependency';
        $bins = Doctrine_Query::create()->from('PpBinary b')->innerJoin('b.Plugin p')->Where('b.updated_on>"2013-01-01 00:00:00"')->orderBy('b.updated_on DESC')->execute(null,
                Doctrine_Core::HYDRATE_ARRAY);
        $dep = array();
        foreach ($bins as $b) {
            if (!empty($b['nbm_filename'])) {
                if (file_exists($this->config['content']['filesystem_nbm_path'] . $b['nbm_filename'])) {
                    $p = new PpPlugin();
                    $p->unpackPluginAndLoadInfoXML(substr($b['nbm_filename'], 0, -4),
                            $this->config['content']['filesystem_nbm_path'] . $b['nbm_filename'],
                            $this->config['content']['tmp_location_path'], $this->config['system']['unzip_executable']);
                    if ($p->hard_dependency == 1) {
                        $dep[] = $b;
                    }
                    unset($p);
                }
            }
        }
        $this->view->dep = $dep;
    }

    // check for all uncategorized plugins and send owners mail asking them fixing that
    public function migrateMailerCategAction() {
        $plugins = Doctrine_Query::create()->from('PpPlugin')
                ->where('author_userid<>"nbadmin"')->andWhere('(categoryid IS NULL OR categoryid="37925")')
                ->orderBy('plugin_name')
                ->execute(null, Doctrine_Core::HYDRATE_ARRAY);
        $transport = new Zend_Mail_Transport_Smtp('localhost');
        Zend_Mail::setDefaultFrom('geertjan@apache.org', 'Geertjan Wielenga ');
        Zend_Mail::setDefaultReplyTo('geertjan@apache.org', 'Geertjan Wielenga ');
        foreach ($plugins as $p) {
            echo $p['pluginid'] . ' ' . $p['plugin_name'] . ' [' . $p['author_userid'] . ']<br/>';
            $mail = new Zend_Mail();
            $mail->addTo($p['author_userid'] . '@netbeans.org', '');
            //$mail->addTo('geertjan@apache.org', '');
            $mail->setSubject('Your plugin ' . $p['plugin_name'] . ' has no category assigned');
            $mail->setBodyText('Hello ' . $p['author_userid'] . ',

as you might know we have recently deployed new version of our NetBeans
Plugin Portal [1]. We have of course migrated all plugins however since
your plugin ' . $p['plugin_name'] . ' [2] didn\'t have set any plugin category we
turn to you with request for help.

[1] http://plugins.netbeans.org
[2] http://plugins.netbeans.org/plugin/' . $p['publicid'] . '/

In order to fix this please login to the Plugin Portal, click "My
Plugins" in the navigation toolbar, find the ' . $p['plugin_name'] . ' plugin entry
and click "Edit". Then select from "Category 1" pull-down menu the most
suitable category based on functionality your plugin provides and
finally push "Save Plugin" button.

If you do not categorize your plugin by the end of April, we will put
your plugin to "Uncategorized" category. If you have any questions
regarding this matter, don\'t hesitate to contact me.

Thanks for your cooperation!
--
Geertjan Wielenga
NetBeans Community Manager
http://www.netbeans.org ');
            $mail->send($transport);
            unset($mail);
        }
        die();
    }

    // check for all uncategorized plugins and send owners mail asking them fixing that
    public function migrateMailerVersAction() {
        $plugins = Doctrine_Query::create()->from('PpPlugin p')
                ->leftJoin('p.Versions v')
                ->where('v.id IS NULL')
                ->orderBy('p.plugin_name')
                ->execute(null, Doctrine_Core::HYDRATE_ARRAY);
        $transport = new Zend_Mail_Transport_Smtp('localhost');
        Zend_Mail::setDefaultFrom('geertjan@apache.org', 'Geertjan Wielenga ');
        Zend_Mail::setDefaultReplyTo('geertjan@apache.org', 'Geertjan Wielenga ');
        foreach ($plugins as $p) {
            echo $p['pluginid'] . ' ' . $p['plugin_name'] . ' [' . $p['author_userid'] . ']<br/>';
            $mail = new Zend_Mail();
            $mail->addTo($p['author_userid'] . '@netbeans.org', '');
            //$mail->addTo('geertjan@apache.org', '');
            $mail->setSubject('Your plugin ' . $p['plugin_name'] . ' has no NetBeans version ');
            $mail->setBodyText('Hello ' . $p['author_userid'] . ',

as you might know we have recently deployed new version of our NetBeans
Plugin Portal [1]. We have of course migrated all plugins however since
your plugin ' . $p['plugin_name'] . ' [2] didn\'t have set any NetBeans version it
works with we turn to you with request for help.

[1] http://plugins.netbeans.org
[2] http://plugins.netbeans.org/plugin/' . $p['publicid'] . '/

In order to fix this please login to the Plugin Portal, click "My
Plugins" in the navigation toolbar, find the ' . $p['plugin_name'] . ' plugin entry
and click "Edit". Then check one or more "Supported NetBeans Versions"
and finally push "Save Plugin" button.

If you do not set any NetBeans version by the end of April, we will
estimate suitable NetBeans version from the date when you last updated
your plugin. If you have any questions regarding this matter, don\'t
hesitate to contact me.

Thanks for your cooperation!
--
Geertjan Wielenga
NetBeans Community Manager
http://www.netbeans.org ');
            $mail->send($transport);
            unset($mail);
        }
        die();
    }

    private function succesMessage($msg) {
        $this->view->message = '<div class="success">' . $msg . '</div>';
    }

    private function errorMessage($msg) {
        $this->view->message = '<div class="error">' . $msg . '</div>';
    }

    private function rejectNonAdminees() {
        $adminees = explode(',', $this->config['admin']['users']);
        if (!$this->_ssoUser->isAuthenticated() || $this->_ssoUser->getUsername == 'guest' || !in_array($this->_ssoUser->getUsername(),
                        $adminees)) {
            header("HTTP/1.0 403 Forbiden");
            die('Access denied! You may need to login first.');
        }
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

    private function invalidateCache() {
        Doctrine_Query::create()->delete('Cache')->execute();
    }

    public function featuredAction() {
        if ($this->_request->isPost()) {
            $plugin = $this->_getParam('plugin');
            $from = $this->_getParam('from');
            if ($plugin && $from) {
                $to = date('Y-m-d', strtotime('+13 days', strtotime($from)));
                $fp = new PpFeaturedPlugin();
                $fp->plugin_id = $plugin;
                $fp->from = $from;
                $fp->to = $to;
                $fp->save();
                $this->succesMessage('Plugin queued');
            } else {
                $this->errorMessage('Missing or wrong parameters');
            }
        }
        $plugins = Doctrine_Query::create()->from('PpFeaturedPlugin f')->innerJoin('f.Plugin p')
                ->where('f.to>=?', date('Y-m-d'))
                ->orderBy('f.from')
                ->execute();
        $pluginsOld = Doctrine_Query::create()->from('PpFeaturedPlugin f')->innerJoin('f.Plugin p')
                ->where('f.to<?', date('Y-m-d'))
                ->orderBy('f.from desc')->limit(52)->offset(0)
                ->execute();
        $this->view->plugins = $plugins;
        $this->view->pluginsOld = $pluginsOld;
    }

    public function featuredDeleteAction() {
        if ($this->_getParam('id')) {
            $pc = Doctrine_Core::getTable('PpFeaturedPlugin')->find($this->_getParam('id'));
            if ($pc) {
                $pc->delete();
                $this->invalidateCache();
                $this->succesMessage('Plugin deleted from Queue');
            }
        } else {
            $this->errorMessage('Missing param');
        }
        $this->_forward('featured');
    }

    public function notifyOwnersAction() {
        $owners = $this->getOwnersForNewVersionNotification();
        $mail = $this->_getParam('mail');
        $version = $this->_getParam('version');
        $subject = $this->_getParam('subject');
        $from = $this->_getParam('from') ? $this->_getParam('from') : 'geertjan@apache.org';
        if (!empty($owners) && $mail && $version) {
            $mail = str_replace('[VERSION]', $version, $mail);
            Zend_Mail::setDefaultFrom($from, '');
            Zend_Mail::setDefaultReplyTo($from, '');
            $transport = new Zend_Mail_Transport_Smtp('localhost');
            foreach ($owners as $pid => $data) {
                $body = str_replace(array('[OWNER]', '[PLUGIN]'), array($data['owner'], $data['plugin']), $mail);
                $mailer = new Zend_Mail();
                $mailer->addTo($data['owner'] . '@netbeans.org', '');
                $mailer->setSubject(str_replace(array('[VERSION]', '[PLUGIN]'), array($version, $data['plugin']),
                                $subject));
                $mailer->setBodyText($body);
                $mailer->send($transport);
                unset($mailer);
            }
            $this->succesMessage('Notification about new version sent to ' . count($owners) . ' plugin owners!');
        } else {
            $this->errorMessage('New Version Notification email - No version or subject or body entered!');
        }
        $this->_forward('misc');
    }

    /**
     * get plugin owners for New version notification email
     * rules: get plugin owners which:
     * 1) were updated in the previous 12 months
     * 2) belong to the Top 10 of the most downloaded plugins
     * 3) belong to the Top 10 of the highest ranked plugins
     *
     * @return array of array('owner'=>$p['author_userid'],'plugin'=>$p['plugin_name'])
     */
    private function getOwnersForNewVersionNotification() {
        $owners = array();
        $recentUpdated = Doctrine_Query::create()->from('PpPlugin p')
                ->where('p.date_last_updated>=?', date('Y-m-d', strtotime('-6 months')))
                ->execute(null, Doctrine_Core::HYDRATE_ARRAY);
        foreach ($recentUpdated as $p) {
            $owners[$p['pluginid']] = array('owner' => $p['author_userid'], 'plugin' => $p['plugin_name']);
        }
        $top10Downloads = Doctrine_Query::create()->from('PpPlugin p')
                ->orderBy('p.downloads desc')->limit(10)->offset(0)
                ->execute(null, Doctrine_Core::HYDRATE_ARRAY);
        foreach ($top10Downloads as $p) {
            $owners[$p['pluginid']] = array('owner' => $p['author_userid'], 'plugin' => $p['plugin_name']);
        }
        $top10Ranked = Doctrine_Query::create()->from('PpPlugin p')
                ->orderBy('p.average_rating desc')->limit(10)->offset(0)
                ->execute(null, Doctrine_Core::HYDRATE_ARRAY);
        foreach ($top10Ranked as $p) {
            $owners[$p['pluginid']] = array('owner' => $p['author_userid'], 'plugin' => $p['plugin_name']);
        }
        return $owners;
    }
    
    private function invalidateZendCache() {    
        // getting a Zend_Cache_Core object
        $cache = Zend_Cache::factory('Output', 'File', $this->frontendOptions, $this->backendOptions);
        $cache->clean(Zend_Cache::CLEANING_MODE_ALL);
    }

}

class Plugins_AdminController extends AdminController {

}
