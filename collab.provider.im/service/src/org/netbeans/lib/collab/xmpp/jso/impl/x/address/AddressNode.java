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

package org.netbeans.lib.collab.xmpp.jso.impl.x.address;

import org.jabberstudio.jso.JID;
import org.jabberstudio.jso.StreamDataFactory;
import org.jabberstudio.jso.StreamElement;
import org.jabberstudio.jso.StreamObject;
import org.jabberstudio.jso.StreamNode;
import org.jabberstudio.jso.NSI;
import org.jabberstudio.jso.format.DateTimeProfileFormat;
import net.outer_planes.jso.ElementNode;

import org.netbeans.lib.collab.xmpp.jso.iface.x.address.*;

/**
 * implementation of AMPRule
 *
 * @author Jacques Belissent
 */
public class AddressNode extends ElementNode implements Address {
    
    public static final NSI ATTRNAME_TYPE      = new NSI("type", null);
    public static final NSI ATTRNAME_DELIVERED = new NSI("delivered", null);
    public static final NSI ATTRNAME_JID       = new NSI("jid", null);
    
    /** Creates a new instance of AMPRuleNode */
    public AddressNode(StreamDataFactory sdf, NSI name) {
        super(sdf, name);
    }
    
    public AddressNode(StreamDataFactory sdf) {
        super(sdf, NAME);
    }
    
    protected AddressNode(StreamElement parent, AddressNode base) {
        super(parent, base);
    }
    
    public void setType(Address.Type type)
    throws IllegalArgumentException {
        setAttributeObject(ATTRNAME_TYPE, type);
    }
    
    public void setDelivered(boolean b)
    throws IllegalArgumentException {
        setAttributeValue(ATTRNAME_DELIVERED, Boolean.toString(b));
    }
    
    public void setJID(JID jid) throws IllegalArgumentException {
        setAttributeObject(ATTRNAME_JID, jid);
    }
    
    
    public JID getJID() {
        Object      val = getAttributeObject(ATTRNAME_JID);
        JID         jid = null;
        
        if (val instanceof JID) {
            jid = (JID)val;
        } else if (val != null) {
            try {
                jid = JID.valueOf(val);
                setAttributeObject(ATTRNAME_JID, jid);
            } catch (IllegalArgumentException iae) {}
        }
        
        return jid;
    }
    
    public boolean isDelivered() {
        String bs = getAttributeValue(ATTRNAME_DELIVERED);
        return bs != null && new Boolean(bs).booleanValue();
    }
    
    public Address.Type getType() {
        Address.Type t = null;
        Object val = getAttributeObject(ATTRNAME_TYPE);
        if (val instanceof Address.Type) {
            t = (Address.Type)val;
        } else if (val != null) {
            try {
                //todo t = JID.valueOf(val);
                setAttributeObject(ATTRNAME_TYPE, t);
            } catch (IllegalArgumentException iae) {}
        }
        return t;
    }
    
    public StreamObject copy(StreamElement parent) {
        return new AddressNode(parent, this);
    }
    
    public boolean equals(Object o) {
        if (o instanceof AddressNode) {
            AddressNode r = (AddressNode)o;
            if (r.getJID() != null &&
                    !r.getJID().equals(getJID())) return false;
            if (getJID() != null &&
                    !getJID().equals(r.getJID())) return false;
            return true;
        } else {
            return false;
        }
    }
    
}
