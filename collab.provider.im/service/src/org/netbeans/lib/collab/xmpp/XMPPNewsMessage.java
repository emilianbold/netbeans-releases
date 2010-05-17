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

import java.util.*;

import org.netbeans.lib.collab.*;
import org.netbeans.lib.collab.xmpp.*;

import org.jabberstudio.jso.*;
import org.jabberstudio.jso.io.*;


import org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub.PubSubQuery;
import org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub.PubSubElement;
import org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub.PubSubEvent;
import org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub.PubSubItems;

import org.netbeans.lib.collab.xmpp.jso.impl.x.pubsub.PubSubQueryNode;


/**
 *
 */
public class XMPPNewsMessage extends XMPPMessage {
    
    XMPPNewsMessage() {
        
    }
    
    public XMPPNewsMessage(XMPPSession s, String node, JID from, JID to) 
          throws CollaborationException 
    {
        _session = s;
        _sdf = s.getDataFactory();
         XMPPNewsService newsService = (XMPPNewsService)s.getNewsService();
        InfoQuery iq = (InfoQuery)_sdf.createPacketNode(s.IQ_NAME, InfoQuery.class);
        iq.setID(s.nextID(PubSubQuery.NAMESPACE));
        iq.setType(InfoQuery.SET);       
        iq.setTo(to);
        
        // assuming that the news message will always contain publish
        // element, might have to change this in future.
        PubSubQuery pubsubquery =
                (PubSubQuery)_sdf.createExtensionNode(PubSubQuery.NAME,
                PubSubQueryNode.class);
        


        PubSubElement publishElement =
                (PubSubElement)pubsubquery.createPublishElement(XMPPNewsService.encode(node,
                newsService.getService().toString()));

        pubsubquery.add(publishElement);
        iq.addExtension(pubsubquery);
        _xmppMessage = iq;
        setOriginator(from.toString());
    }
    
    public XMPPNewsMessage(XMPPSession s, Packet message) 
           throws CollaborationException 
    {
        super(s,message);
    }
    
    public XMPPNewsMessage(StreamDataFactory sdf, InfoQuery iq) 
           throws CollaborationException 
    {
        _sdf = sdf;
        _xmppMessage = iq;
    }
    
    public String getMessageId() {
        String messageId = null;
        if (_xmppMessage instanceof org.jabberstudio.jso.Message) {
            PubSubEvent event = (PubSubEvent)_xmppMessage.getExtension(PubSubEvent.NAMESPACE);
            PubSubItems items = event.getPubSubItemsElement();
            StreamElement item = null;
            if (items.hasPubSubRetractItem()) {
                item = items.getPubSubRetractItem();                
            } else {
                // assume that it contains only one event notification
                item = (StreamElement)items.listPubSubItems().get(0);                
            }
            messageId = item.getID();
        } else {
            PubSubQuery query = (PubSubQuery)_xmppMessage.getExtension(PubSubQuery.NAMESPACE);
            if (query != null) {
                PubSubElement publishElem = query.getPublishElement();
                PubSubQueryNode.PubSubItemElement item = (PubSubQueryNode.PubSubItemElement)publishElem.getFirstElement(PubSubQueryNode.PubSubItemElement.NAME, PubSubQueryNode.PubSubItemElement.class);
                messageId = item.getID();
            }
        }
        return messageId;
    }
    
    public void setContent(org.w3c.dom.Element content) throws CollaborationException {
        try {
        DOMImporter importer = XMPPSession._jso.createDOMImporter(XMPPSession._jso.createStreamContext());
        StreamElement el = importer.read(content);
        getPublishElement().addPubSubItem(PubSubQuery.NAME.getLocalName() + 
                                          "-" + getUniqueMessageID(), el);
} catch (Exception e) {
  throw new CollaborationException(e);
}
    }

    private PubSubQueryNode.PubSubPublishElement getPublishElement() {
        List l = _xmppMessage.listElements(PubSubQuery.NAME,PubSubQuery.class);
        if (l.iterator().hasNext()) {
            StreamElement elem = (StreamElement)l.iterator().next();
            l = elem.listElements(PubSubQueryNode.PubSubPublishElement.NAME,
            PubSubQueryNode.PubSubPublishElement.class);
        }
        PubSubQueryNode.PubSubPublishElement publishElem = null;
        if (l.iterator().hasNext()) {
            publishElem = (PubSubQueryNode.PubSubPublishElement)l.iterator().next();
        }
        return publishElem;
    }

        
    public void setContent(String content) {
        getPublishElement().addPubSubItem(PubSubQuery.NAME.getLocalName() + 
                                          "-" + getUniqueMessageID(), content);
    }

    public void setContent(String content, String type) throws CollaborationException {
        if (type.indexOf("xml") >= 0) {
        try {
        XMLImporter importer = XMPPSession._jso.createXMLImporter(XMPPSession._jso.createStreamContext());
        StreamElement el = importer.read(content);
        getPublishElement().addPubSubItem(PubSubQuery.NAME.getLocalName() +
                                          "-" + getUniqueMessageID(), el);
} catch (Exception e) {
  throw new CollaborationException(e);
}

        } else {
            setContent(content);
        }
    }


    
    public String getContent() {
        if (_xmppMessage instanceof org.jabberstudio.jso.Message) {
            PubSubEvent event = (PubSubEvent)_xmppMessage.getExtension(PubSubEvent.NAMESPACE);
            PubSubItems items = event.getPubSubItemsElement();
            // assume that it contains only one event notification
            StreamElement item = (StreamElement)items.listPubSubItems().get(0);
            return item.normalizeTrimText();
        } else {
            PubSubQuery query = (PubSubQuery)_xmppMessage.getExtension(PubSubQuery.NAMESPACE);
            if (query != null) {
                PubSubElement publishElem = query.getPublishElement();
                PubSubQueryNode.PubSubItemElement item = (PubSubQueryNode.PubSubItemElement)publishElem.getFirstElement(PubSubQueryNode.PubSubItemElement.NAME, PubSubQueryNode.PubSubItemElement.class);
                return item.getContent();
            }
        }
        return null;
    }
    
    public void setContentType(String type) {
        
    }
    
    public String getContentType() {
        return null;
        
    }

   public String getOriginator() {
       return JIDUtil.decodedJID(_xmppMessage.getFrom());
   }
    
   public String toString() {
        return _xmppMessage.toString();
    }
}


