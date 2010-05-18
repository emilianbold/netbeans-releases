/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.lib.collab.xmpp;

import org.netbeans.lib.collab.CollaborationException;
import org.netbeans.lib.collab.Message;
import org.netbeans.lib.collab.MessageStatus;
import org.netbeans.lib.collab.MessageStatusListener;
import org.netbeans.lib.collab.NotificationService;
import org.netbeans.lib.collab.NotificationServiceListener;
import org.netbeans.lib.collab.Poll;

import org.jabberstudio.jso.JID;
import org.jabberstudio.jso.NSI;
import org.netbeans.lib.collab.xmpp.jso.iface.x.event.MessageEventExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Iterator;
import java.util.Collections;

/**
 *
 * 
 */
public class XMPPNotificationService implements NotificationService {

    private List _notificationServiceListeners = Collections.synchronizedList(new java.util.ArrayList());

    XMPPSession __session;

    private HashMap _messageStatusListeners = new HashMap();
    private HashMap _messageReplyListeners = new HashMap();


    /** Creates a new instance of XMPPNotificationService */
    public XMPPNotificationService(XMPPSession session) {
        __session = session;
    }

    public static int getApiStatus(MessageEventExtension evtNode) {
        NSI n = evtNode.getNSI();
        if (evtNode.hasMessageEvent(MessageEventExtension.OFFLINE)) {
            return MessageStatus.FAILED;
        } else if (evtNode.hasMessageEvent(MessageEventExtension.DELIVERED)) {
            return MessageStatus.RECEIVED;
        } else if (evtNode.hasMessageEvent(MessageEventExtension.DISPLAYED)) {
            return MessageStatus.READ;
        } else if (evtNode.hasMessageEvent(MessageEventExtension.COMPOSING)) {
            return MessageStatus.TYPING_ON;
        } else {
            return MessageStatus.TYPING_OFF;
        }
    }

    protected void processNormalMessage(org.jabberstudio.jso.Message in)
    {
	try {
            XMPPSessionProvider.debug("regular message or response ");
            MessageEventExtension xNode =
                (MessageEventExtension)in.getExtension(MessageEventExtension.NAMESPACE);
            if ((xNode != null) && (xNode.hasMessageID())) {
                MessageStatusListener msl =
                    (MessageStatusListener)_messageStatusListeners.get(xNode.getMessageID());
                if (msl != null) {
                    msl.onReceipt(in.getFrom().toString(), getApiStatus(xNode));
                }
                //if there is no body in the message then return
                if (!in.hasBody()) return;
            }
            String threadId = in.getThread();
            MessageStatusListener msl = null;
	    if (//null != threadId && 
                    (xNode == null || xNode.hasMessageID())) {
                msl = (MessageStatusListener)_messageReplyListeners.get(threadId);
	    }
            if (msl != null) {
                //processReplyMessage(in, msl);
                msl.onReply(__session.assembleMessages(new XMPPMessage(__session,in)));
            } else {
                XMPPSessionProvider.debug("msl is null thread " + threadId);
                //org.netbeans.lib.collab.Message m = new XMPPMessage(XMPPSession.this,in);
                _fireNotificationServiceListener(__session.assembleMessages(new XMPPMessage(__session,in)));
            }
        } catch(CollaborationException ce) {
            XMPPSessionProvider.error(ce.toString(),ce);
        } catch (Exception e) {
            XMPPSessionProvider.error(e.toString(), e);
        }

    }

    //////////////////////////////////
    ///NotificationService Impl///////
    //////////////////////////////////

    public void initialize(NotificationServiceListener listener)
                                                throws CollaborationException {
        addNotificationServiceListener(listener);
    }


    public void sendMessage(Message message, MessageStatusListener listener)
                                                throws CollaborationException {
      //try {
            org.jabberstudio.jso.Message m =
                    (org.jabberstudio.jso.Message)((XMPPMessage)message).getXMPPMessage();
            String[] recipients = message.getRecipients();
            String messageId = message.getMessageId();

            if (listener != null) {

                //Only threadId should be used as the key. So commenting the following code
                /*for(int i = 0; i < recipients.length ; i++) {
                    String key = getMessageListenerKey(threadId,recipients[i]);
                    _messageReplyListeners.put(key, listener);
                    key = getMessageListenerKey(messageId,recipients[i]);
                    _messageStatusListeners.put(key, listener);
                }*/

                if ((message.getContentType() != null) &&
                    (message.getContentType()).equalsIgnoreCase(XMPPMessage.POLL_TYPE))
                {
                    String content = message.getContent();
                    m.setBody(null);
                    Poll p = null;
                    try {
                        p = new Poll(content);
                    } catch (Exception e) {
                        e.printStackTrace();
                        throw new CollaborationException(e.toString());
                    }
                    m.add(p.getXDataForm());
                }

                MessageEventExtension msgEvtExt =
                    (MessageEventExtension)__session.getDataFactory()
                             .createExtensionNode(MessageEventExtension.NAME);
                msgEvtExt.addEvent(MessageEventExtension.OFFLINE);
                msgEvtExt.addEvent(MessageEventExtension.DELIVERED);
                msgEvtExt.addEvent(MessageEventExtension.DISPLAYED);
                // msgEvtExt.addEvent(MessageEventExtension.COMPOSING);
                m.add(msgEvtExt);
            }

            for(int i = 0; i < recipients.length ; i++) {
                m.setTo(new JID(recipients[i]));
                String threadId = __session.nextID("thread");
                threadId = m.getFrom() + threadId;
                String mid = __session.nextID("message");
                m.setThread(threadId);
                m.setID(mid);
                if (listener != null) {
                    _messageStatusListeners.put(mid, listener);
                    _messageReplyListeners.put(threadId, listener);
                }
                //_connection.send(m);
                __session.sendAllMessageParts((XMPPMessage)message);
            }
        /*} catch(StreamException se) {
            XMPPSessionProvider.error(se.toString(),se);
            throw new CollaborationException(se.toString());
        }*/
    }

    /**
     * create a message.
     * This Message object can then be used to generate an alert
     */
    public org.netbeans.lib.collab.Message createMessage(String destination)
                                      throws CollaborationException
    {
        JID recipient = __session.getDataFactory().createJID(destination);
        return new XMPPMessage(__session,recipient,
                        ((XMPPPrincipal)(__session.getPrincipal())).getJID());
    }

    /**
     * create a message.
     * This Message object can then be used to generate an alert
     */
    public org.netbeans.lib.collab.Message createMessage() throws CollaborationException {
        return new XMPPMessage(__session,
                        ((XMPPPrincipal)(__session.getPrincipal())).getJID());
    }

    //////////
    //Private methods
    //////////
    private String getMessageListenerKey(String key, String rcpt) {
        return key + ";" + rcpt;
    }

    private void _fireNotificationServiceListener(Message m){
    	__session.addWorkerRunnable(new NotificationServiceNotifier(m));
    }

    public void addNotificationServiceListener(NotificationServiceListener listener) {
        if (!_notificationServiceListeners.contains(listener))
            _notificationServiceListeners.add(listener);
    }
    
    public void removeNotificationServiceListener(NotificationServiceListener listener) {
        _notificationServiceListeners.remove(listener);
    }
    
    private class NotificationServiceNotifier implements Runnable {
        Message message;
        NotificationServiceNotifier(Message m) {
            message = m;
        }
        
        public void run() {
            synchronized(_notificationServiceListeners) {
                for(Iterator itr = _notificationServiceListeners.iterator(); itr.hasNext();) {
                    try {
                        NotificationServiceListener l = (NotificationServiceListener)itr.next();
                        if (l == null) continue;
                        l.onMessage(message);
                    } catch(Exception e) {
                        XMPPSessionProvider.error(e.toString(),e);
                    }
                }
            }
        }
    }
}
