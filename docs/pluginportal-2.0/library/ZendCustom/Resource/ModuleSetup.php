<?php

class ZendCustom_Resource_ModuleSetup extends Zend_Application_Resource_ResourceAbstract {

    public function init() {
        $this->_getModuleSetup();
    }

    protected function _startDoctrine($module, $connection_string, $encoding) {
        if (!$module || !$connection_string || !$encoding) {
            // doctrine not needed at all...
            return;
        }

        //require_once 'Doctrine/Doctrine.php';
        //spl_autoload_register(array('Doctrine', 'autoload'));

        $manager = Doctrine_Manager::getInstance();
        $manager->setAttribute(Doctrine_Core::ATTR_QUOTE_IDENTIFIER, true);
        $manager->setAttribute(Doctrine::ATTR_MODEL_LOADING, Doctrine::MODEL_LOADING_CONSERVATIVE);

        try {
            $connection = $manager->connection($connection_string, $module);
            $connection->setCharset($encoding);
        } catch (Exception $exc) {
            //echo $exc->getTraceAsString();
        }
        $appOptions = $this->getBootstrap()->getOptions();
        // start DB caching if configured
        if ($appOptions[$module]['doctrine']['cache']['use'] == '1') {
            try {
                $cacheDriver = new Doctrine_Cache_Db(array('connection' => $connection, 'tableName' => 'cache'));
                //$cacheDriver->createTable();
                if ($appOptions[$module]['doctrine']['cache']['queryCache'] == 1) {
                    $manager->setAttribute(Doctrine_Core::ATTR_QUERY_CACHE, $cacheDriver);
                }
                if ($appOptions[$module]['doctrine']['cache']['resultCache'] == 1) {
                    $manager->setAttribute(Doctrine_Core::ATTR_RESULT_CACHE, $cacheDriver);
                }
                $lifespan = ($appOptions[$module]['doctrine']['cache']['lifespan']) ? $appOptions[$module]['doctrine']['cache']['lifespan'] : 300;
                $manager->setAttribute(Doctrine_Core::ATTR_RESULT_CACHE_LIFESPAN, $lifespan);
            } catch (Exception $e) {
                die($e->getMessage());
            }
        }
    }

    /**
     * Load the module's ini files
     *
     * @return void
     */
    protected function _getModuleSetup() {
        $bootstrap = $this->getBootstrap();

        if (!($bootstrap instanceof Zend_Application_Bootstrap_Bootstrap)) {
            throw new Zend_Application_Exception('Invalid bootstrap class');
        }

        $bootstrap->bootstrap('FrontController');
        $front = $bootstrap->getResource('FrontController');
        $modules = $front->getControllerDirectory();

        foreach (array_keys($modules) as $module) {
            $configPath = $front->getModuleDirectory($module)
                    . DIRECTORY_SEPARATOR . 'configs';
            if (file_exists($configPath)) {
                $cfgdir = new DirectoryIterator($configPath);
                $appOptions = $this->getBootstrap()->getOptions();

                foreach ($cfgdir as $file) {
                    if ($file->isFile()) {
                        $filename = $file->getFilename();
                        $options = $this->_loadOptions($configPath
                                        . DIRECTORY_SEPARATOR . $filename);
                        if (($len = strpos($filename, '.')) !== false) {
                            $cfgtype = substr($filename, 0, $len);
                        } else {
                            $cfgtype = $filename;
                        }

                        if (strtolower($cfgtype) == 'module') {
                            if (array_key_exists($module, $appOptions)) {
                                if (is_array($appOptions[$module])) {
                                    $appOptions[$module] =
                                            array_merge($appOptions[$module], $options);
                                } else {
                                    $appOptions[$module] = $options;
                                }
                            } else {
                                $appOptions[$module] = $options;
                            }
                        } else {
                            $appOptions[$module]['resources'][$cfgtype] = $options;
                        }
                    }
                }
                $this->getBootstrap()->setOptions($appOptions);

                // now setup doctrine connection for the module if such configured
                if ($appOptions[$module]['doctrine']['connection_string']) {
                    $enc = ($appOptions[$module]['doctrine']['encoding']) ? $appOptions[$module]['doctrine']['encoding'] : 'UTF8';
                    $this->_startDoctrine($module, $appOptions[$module]['doctrine']['connection_string'], $enc);
                }
            } else {
                continue;
            }
        }
    }

    /**
     * Load the config file
     *
     * @param string $fullpath
     * @return array
     */
    protected function _loadOptions($fullpath) {
        if (file_exists($fullpath)) {
            switch (substr(trim(strtolower($fullpath)), -3)) {
                case 'ini':
                    $cfg = new Zend_Config_Ini($fullpath, $this->getBootstrap()
                                            ->getEnvironment());
                    break;
                case 'xml':
                    $cfg = new Zend_Config_Xml($fullpath, $this->getBootstrap()
                                            ->getEnvironment());
                    break;
                default:
                    throw new Zend_Config_Exception('Invalid format for config file');
                    break;
            }
        } else {
            throw new Zend_Application_Resource_Exception('File does not exist');
        }
        return $cfg->toArray();
    }

}
