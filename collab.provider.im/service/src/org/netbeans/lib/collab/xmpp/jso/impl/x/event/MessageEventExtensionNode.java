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

package org.netbeans.lib.collab.xmpp.jso.impl.x.event;

import java.util.*;

import net.outer_planes.jso.ExtensionNode;
import net.outer_planes.jso.ElementNode;

import org.jabberstudio.jso.StreamDataFactory;
import org.jabberstudio.jso.StreamElement;
import org.jabberstudio.jso.StreamObject;
import org.jabberstudio.jso.NSI;

import org.netbeans.lib.collab.xmpp.jso.iface.x.event.MessageEventExtension;

/**
 *
 * @author Rahul Shah
 *
 */
public class MessageEventExtensionNode extends ExtensionNode implements MessageEventExtension {

    public static final NSI ELEM_ID = new NSI("id",NAMESPACE);

    public static final class MessageEventNode extends ElementNode {
        
        public MessageEventNode(StreamDataFactory sdf, NSI name) 
            throws IllegalArgumentException {
            super(sdf,name);
        }

        public MessageEventNode(StreamDataFactory sdf, StreamElement base) {
            super(sdf,base.getNSI());
        }
        
        public MessageEventNode(StreamElement parent, MessageEventNode base) {
            super(parent,base);
        }
        
        public StreamObject copy(StreamElement parent) {
            return new MessageEventNode(parent,this);
        }
        
    }
    
    public MessageEventExtensionNode(StreamDataFactory sdf) {
        super(sdf,NAME);
    }
    
    public MessageEventExtensionNode(StreamElement parent, MessageEventExtensionNode base) {
        super(parent,base);
    }
        
    public void addEvent(MessageEventExtension.EventType evt) throws IllegalArgumentException {
        addElement(evt.toString(), MessageEventNode.class);
    }
    
    public void removeEvent(MessageEventExtension.EventType evt) {
        Iterator itr = listElements(evt.toString(),MessageEventNode.class).iterator();
        if (itr.hasNext()) {
            MessageEventNode evtElem = (MessageEventNode)itr.next();
            evtElem.detach();
        }
    }
    
    public List getEvents() {
        Iterator itr = listElements(MessageEventNode.class).iterator();
        ArrayList l = new ArrayList();
        
        while (itr.hasNext()) {
            StreamElement elm = (StreamElement)itr.next();
            if (MessageEventExtension.COMPOSING.equals(elm.getLocalName())) {
                l.add(MessageEventExtension.COMPOSING);
            } else if (MessageEventExtension.DELIVERED.equals(elm.getLocalName())) {
                l.add(MessageEventExtension.DELIVERED);
            } else if (MessageEventExtension.DISPLAYED.equals(elm.getLocalName())) {
                l.add(MessageEventExtension.DISPLAYED);
            } else if (MessageEventExtension.OFFLINE.equals(elm.getLocalName())) {
                l.add(MessageEventExtension.OFFLINE);
            }
        }
        return l;
    }
    
    public void setMessageID(String id) {
        addElement(ELEM_ID).addText(id);
    }
    
    public String getMessageID() {
        Iterator itr = listElements(ELEM_ID).iterator();
        return itr.hasNext() ? ((ElementNode)itr.next()).normalizeTrimText():null;
    }
    
    public boolean hasMessageID() {
        Iterator itr = listElements(ELEM_ID).iterator();
        return itr.hasNext();
    }
    
    public boolean hasMessageEvent(MessageEventExtension.EventType evt) {
        return listElements(evt.toString()).iterator().hasNext();
    }
    
    public boolean containsMessageEvent() {
        return listElements(MessageEventNode.class).iterator().hasNext();
    }
    
    public StreamObject copy(StreamElement parent) {
        return new MessageEventExtensionNode(parent,this);
    }
    
}
