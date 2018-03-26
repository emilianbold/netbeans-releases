<?php

/**
 * BinController used for cli bin execution
 *
 * @author honza
 */
class BinController extends Zend_Controller_Action {

    public function init() {
        /* Initialize action controller here */
        $conf = $this->getInvokeArg('bootstrap')->getOptions();
        $this->config = $conf[$this->_request->getModuleName()];
        $this->_helper->layout->setLayout('blank');
        $this->view->config = $this->config;
    }

    public function featuredPluginsQueueNotificationAction() {
        // get featured plugin
        $fpQueue = Doctrine_Query::create()->from('PpFeaturedPlugin f')->innerJoin('f.Plugin p')
                        ->where('f.from>=?', date('Y-m-d'))
                        ->andWhere('f.to>=?', date('Y-m-d'))
                        ->execute();
        // send notification if there are just 2 remaining
        if($fpQueue->count()<=1) {
            $transport = new Zend_Mail_Transport_Smtp('localhost');
                    Zend_Mail::setDefaultFrom('jan.pirek@oracle.com', 'Honza Pirek');
                    Zend_Mail::setDefaultReplyTo('jan.pirek@oracle.com', 'Honza Pirek');
                    $mail = new Zend_Mail();
                    $mail->addTo($this->config['featured']['notificationEmail'].'@netbeans.org', '');
                    $mail->addCc('jan.pirek@oracle.com', '');
                    $mail->setSubject('[NetBeans PluginPortal] Featured plugin queue running low on items.');
                    $mail->setBodyText('Hello,
this is to let you know that there is last plugin in the queue of featured plugins.

Please log in to PP and add some to the queue.

http://plugins.netbeans.org/admin/featured

Cheers,
Honza');
                    $res=$mail->send($transport);
                    if($res) {
                        echo 'Notifiaction email sent to '.$this->config['featured']['notificationEmail'].' '.PHP_EOL;
                    } else {
                        echo 'Something went wrong during sending, uff..'.PHP_EOL;
                    }
        } else {
            echo 'There are still enough plugins in the queue ('.$fpQueue->count().'), not mailing'.PHP_EOL;
        }

        die();
    }

}
