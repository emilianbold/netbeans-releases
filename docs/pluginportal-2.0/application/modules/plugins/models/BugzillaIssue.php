<?php

/**
 * Class for filing BZ issue on the background of action
 */
class BugzillaIssue {

    private static $submissionUrl = 'https://netbeans.org/bugzilla/post_bug.cgi';
    private static $commentUrl = 'https://netbeans.org/bugzilla/process_bug.cgi';
    private static $tokenUrl = 'https://netbeans.org/bugzilla/show_bug.cgi';

    /**
     * Submit new ticlet to nb.org bugzilla
     * @param string $pluginName
     * @param string $reporter
     * @param string $assignedTo
     * @param string $description
     * @param string $version
     * @return int status of the submission - 0 for error, number of the bugzilla issue on success
     */
    public static function submit($pluginName, $reporter, $assignedTo, $description, $version, $pluginId, $proxyHost, $proxyPort) {
        $ret = 0;
        // use http client component
        try {
            $client = new Zend_Http_Client(self::$submissionUrl, array(
                'maxredirects' => 0,
                'timeout' => 30,
                'strict' => false,
                'adapter' => 'Zend_Http_Client_Adapter_Proxy',
                'proxy_host' => $proxyHost,
                'proxy_port' => $proxyPort));
            // set all fields
            $client->setParameterGet('reporter', $reporter . '@netbeans.org');
            $client->setParameterGet('assigned_to', $assignedTo . '@netbeans.org');
            $client->setParameterGet('issue_file_loc', 'http://');
            $client->setParameterGet('form_name', 'enter_issue');
            $client->setParameterGet('product', 'updatecenters');
            $client->setParameterGet('component', 'Pluginportal');
            $client->setParameterGet('version', $version);
            $client->setParameterGet('priority', 'P3');
            $client->setParameterGet('op_sys', 'All');
            $client->setParameterGet('rep_platform', 'All');
            $client->setParameterGet('cf_bug_type', 'DEFECT');
            $client->setParameterGet('short_desc', 'NoGo for plugin ' . $pluginName);
            $client->setParameterGet('comment', $description . "\n\nhttp://plugins.netbeans.org/plugin/" . $pluginId);

            // set auth cookies
            $client->setCookie('SSO', $_COOKIE['SSO']);
            $client->setCookie('_junction2_session', $_COOKIE['_junction2_session']);

            $response = $client->request('POST');
            if ($response->isSuccessful()) {
                // try to parse output and get the issue number so we can crosslink from PP
                // link is  show_bug.cgi?id=205933
                preg_match("#show_bug\.cgi\?id=(\d+)#i", $response->getBody(), $matches);
                if (!empty($matches[1])) {
                    $ret = $matches[1];
                }
            }
        } catch (Exception $e) {
            echo $e->getMessage();
        }
        return $ret;
    }

    /**
     * Comment on existing ticket in nb.org bugzilla
     * @param string $pluginName
     * @param string $reporter
     * @param string $assignedTo
     * @param string $description
     * @param string $version
     * @return int status of the submission - 0 for error, number of the bugzilla issue on success
     */
    public static function comment($pluginName, $reporter, $assignedTo, $description, $version, $pluginId, $issueId, $proxyHost, $proxyPort) {
        $ret = 0;
        // use http client component
        try {

            // uff, need to get form token first <input type="hidden"  name="token" value="1323680731-44e4330edc7ccb337690ce4e365216fe">
            $tclient = new Zend_Http_Client(self::$tokenUrl . '?id=' . $issueId, array(
                'maxredirects' => 0,
                'timeout' => 30,
                'strict' => false,
                'adapter' => 'Zend_Http_Client_Adapter_Proxy',
                'proxy_host' => $proxyHost,
                'proxy_port' => $proxyPort));
            $tclient->setCookie('SSO', $_COOKIE['SSO']);
            $tclient->setCookie('_junction2_session', $_COOKIE['_junction2_session']);
            $tresponse = $tclient->request('GET');
            if ($tresponse->isSuccessful()) {
                preg_match('#\<input type=\"hidden\" name=\"token\" value=\"(.*)\"\>#iUm', $tresponse->getBody(), $matches);
                if (!empty($matches[1])) {
                    $token = $matches[1];
                }
                // if we have token, let's try to submit
                if ($token) {
                    $client = new Zend_Http_Client(self::$commentUrl, array(
                        'maxredirects' => 0,
                        'timeout' => 30));
                    // set all fields
                    $client->setParameterGet('id', $issueId);
                    $client->setParameterGet('reporter', $reporter . '@netbeans.org');
                    $client->setParameterGet('token', $token);
                    $client->setParameterGet('issue_file_loc', 'http://');
                    ;
                    $client->setParameterGet('product', 'updatecenters');
                    $client->setParameterGet('component', 'Pluginportal');
                    $client->setParameterGet('version', $version);
                    $client->setParameterGet('priority', 'P3');
                    $client->setParameterGet('op_sys', 'All');
                    $client->setParameterGet('rep_platform', 'All');
                    $client->setParameterGet('cf_bug_type', 'DEFECT');
                    ;
                    $client->setParameterGet('comment', $description);

                    // set auth cookies
                    $client->setCookie('SSO', $_COOKIE['SSO']);
                    $client->setCookie('_junction2_session', $_COOKIE['_junction2_session']);

                    $response = $client->request('POST');
                    if ($response->isSuccessful()) {
                        $ret = 1;
                    }
                }
            }
        } catch (Exception $e) {
            echo $e->getMessage();
        }
        return $ret;
    }

}

?>