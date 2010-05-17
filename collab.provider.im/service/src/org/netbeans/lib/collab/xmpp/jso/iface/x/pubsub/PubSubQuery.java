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

package org.netbeans.lib.collab.xmpp.jso.iface.x.pubsub;

import java.util.List;

import org.jabberstudio.jso.Extension;
import org.jabberstudio.jso.NSI;
import org.jabberstudio.jso.JID;

/**
 *
 */
public interface PubSubQuery  extends Extension {

    /**
     * Namespace governing <code>PubSubQuery</code>
     */

    public static final String NAMESPACE = "http://jabber.org/protocol/pubsub";

    public static final String NAMESPACE_OWNER = NAMESPACE + "#owner";

    /**
     * Qualified name for <tt>PubSubQuery</tt> ({http://jabber.org/protocol}pubsub).
     */				 
    
    public static final NSI NAME = new NSI("pubsub", NAMESPACE);
    
    public static final NSI NAME_OWNER = new NSI("pubsub",NAMESPACE_OWNER);
    
    public PubSubElement createCreateElement(String node) throws IllegalArgumentException;
    
    public PubSubElement createSubscribeElement(String node, JID subscriber) throws IllegalArgumentException;
    
    public PubSubElement createUnSubscribeElement(String node,JID subscriber) throws IllegalArgumentException;
    
    public PubSubElement createPublishElement(String node) throws IllegalArgumentException;
    
    public PubSubElement createPublishElement(String node,String msgId,String content) throws IllegalArgumentException;
    
    public PubSubElement createDeleteElement(String node) throws IllegalArgumentException;
    
    public PubSubEntityElement createEntityElement(String node,JID jid,String affiliation,String subscriptionStatus ) throws IllegalArgumentException;
    
    public EntityContainer createAffiliationsElement(List entities) throws IllegalArgumentException;
    
    public EntityContainer createAffiliationsElement() throws IllegalArgumentException;
    
    public EntityContainer createEntitiesElement(String node) throws IllegalArgumentException;
    
    public PubSubElement createRetractElement(String node,String msgId) throws IllegalArgumentException;
    
    public PubSubElement createConfigureElement(String node) throws IllegalArgumentException;
    
    public PubSubElement createConfigureElement();
        
    public PubSubElement getCreateElement();
    
    public PubSubElement getDeleteElement();
    
    public PubSubElement getSubscribeElement();
    
    public PubSubElement getUnSubscribeElement();
    
    public PubSubElement getPublishElement();
    
    public PubSubElement getConfigureElement();
    
    public PubSubElement getPubSubRetractElement();
    
}
