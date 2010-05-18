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
import org.jabberstudio.jso.*;

import org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub.PubSubQuery;
import org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub.PubSubItems;
import org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub.PubSubEvent;
import org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub.EntityContainer;
import org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub.PubSubEntityElement;

/**
 *
 *
 * @author Vijayakumar Palaniappan
 *
 */
public class XMPPPersonalContact extends XMPPPersonalStoreEntry
                                 implements PersonalContact
{

    java.util.ArrayList _addresses;
    CollaborationPrincipal _principal = null;

    int _inboundSubscription = SUBSCRIPTION_STATUS_CLOSED;
    int _outboundSubscription = SUBSCRIPTION_STATUS_CLOSED;

    /** Creates a new instance of XMPPPersonalContact */
    public XMPPPersonalContact(XMPPSession s, String name, String jid) 
    {
        
        super(s, name, PersonalStoreEntry.CONTACT, jid);
        _addresses = new java.util.ArrayList();
        if (jid != null) addAddress(PersonalContact.IM, jid, 0);
    }
    
    public void addAddress(String type, String id, int priority) {
        if (getEntryId() == null) setEntryId(id);
        _addresses.add(id);
    }
    
    public String getAddress(String str) {
        if (_addresses.size() > 0) return (String)_addresses.get(_addresses.size()-1);
        else return null;
    }
    
    public void removeAddress(String str, String str1) throws org.netbeans.lib.collab.CollaborationException {
        _addresses.remove(0);
    }
    
    public java.util.List getAddresses(String str) {
        return _addresses;
    }

    /*protected void setPrincipal(XMPPPrincipal p) {
        _principal = p;
    }*/
    
    public CollaborationPrincipal getPrincipal() {
        if (_principal == null) {
            try {
            CollaborationPrincipal[] cp = null;

            // search only if request is for current user - shouldn't we not be
            // returning this from XMPPSession ?
            if (getEntryId().equals(_session.getCurrentUserJID().toBareJID().toString())){
                cp = _personalStoreService.searchPrincipals(PersonalStoreService.SEARCHTYPE_EQUALS, getEntryId());
            }
            if (cp == null || cp.length != 1) {
                _principal = new XMPPPrincipal(new JID(_jid), _displayName);
            } else {
                _principal = cp[0];
            }
            } catch (Exception e) {
                _principal = new XMPPPrincipal(new JID(_jid), _displayName);
            }
        }
        return _principal;
    }
    
    public String toString() {
        return super.toString() +
                " ToSubs: " +  getInboundSubscriptionStatus() + 
                " FromSubs: " + getOutboundSubscriptionStatus();
    }

    public int getOutboundSubscriptionStatus() {
        return _inboundSubscription;
    }
    
    public void setOutboundSubscriptionStatus(int i) {
        _inboundSubscription = i;
    }
    
    public int getInboundSubscriptionStatus() {
        return _outboundSubscription;
    }
    
    public void setInboundSubscriptionStatus(int i) {
        _outboundSubscription = i;
    }

    void handlePersonalEvent(org.jabberstudio.jso.Message in) {

        PubSubEvent event = (PubSubEvent)in.getExtension(PubSubEvent.NAMESPACE);

        if (event != null) {
            PersonalStoreEvent pse = null;
            if (event.hasPubSubItemsElement()) {
                PubSubItems items = event.getPubSubItemsElement();
                String node = items.getNodeIdentifier();
                if (items.hasPubSubRetractItem() || event.hasDeleteElement()) {
                    pse = new PersonalStoreEvent(PersonalStoreEvent.TYPE_DATA,
                            this, node, null);
		    getPrincipal().setProperty(node, null);

                } else {
                    PubSubItems itemsElement = event.getPubSubItemsElement();
                    for (Iterator iter = itemsElement.listPubSubItems().iterator();
                         iter.hasNext();) {
                        StreamElement item = (StreamElement)iter.next();
			// no support for more than one el.
			String content = item.getFirstElement().toString();
                        pse = new PersonalStoreEvent(PersonalStoreEvent.TYPE_DATA,
                                this, node, content);

		        getPrincipal().setProperty(node, content);
                        
                        // only on item at a time according to PEP spec
                        break;
                    }
                }
                
                if (pse != null) {
                    _personalStoreService._firePersonalStoreServiceListeners(pse);
                }
                
            } else {
                XMPPSessionProvider.info("[XMPPSession#personalEvent]: no message event extension");
            }
        }
        
    }
}
