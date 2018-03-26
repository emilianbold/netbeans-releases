<?php

/**
 * validator using our own anti-spam service on http://services.netbeans.org/AntiSpam/
 *
 */
class AntiSpamValidator {

    private $_proxyHost;
    private $_proxyPort;
    private $_serviceUrl = '';
    private $_log = array();
    private $_client;

    const SERVICE_METHOD = 'check_content';

    public function __construct($serviceUrl, $proxyHost, $proxyPort) {
        try {
            $this->_proxyHost = $proxyHost;
            $this->_proxyPort = $proxyPort;
            $this->_serviceUrl = $serviceUrl;
            $this->_client = new Zend_Http_Client($serviceUrl,
                    array(
                'maxredirects' => 0,
                'timeout' => 30,
                'strict' => false,
                'adapter' => 'Zend_Http_Client_Adapter_Proxy',
                'proxy_host' => $proxyHost,
                'proxy_port' => $proxyPort));
            // set all fields
            $this->_client->setParameterPost('method', AntiSpamvalidator::SERVICE_METHOD);
            return $this;
        } catch (Exception $e) {
            $this->log($e->getMessage());
        }
    }

    public function validate($body, $title, $ip, $username) {
        $this->_client->setParameterPost('post_title', $title);
        $this->_client->setParameterPost('post_body', $body);
        $this->_client->setParameterPost('author_mail', $username . '@netbeans.org');
        $this->_client->setParameterPost('author_ip', $ip);

        $response = $this->_client->request('POST');
        if ($response->isSuccessful()) {
            $reponseAsArray = (array) simplexml_load_string($response->getBody());
            if ($reponseAsArray['response'] === 'true') {
                return true;
            }
            $this->log('This post is SPAM');
            return false;
        } else {
            $this->log('SPAM validator request failed with following: ' . printr($response, TRUE));
            return false;
        }
        return false;
    }

    private function log($msg) {
        $this->_log[] = date("Y-m-d H:i:s") . ' - ' . $msg;
    }

}
