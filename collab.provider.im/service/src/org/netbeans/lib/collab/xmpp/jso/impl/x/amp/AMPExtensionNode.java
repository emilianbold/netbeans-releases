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

package org.netbeans.lib.collab.xmpp.jso.impl.x.amp;

import java.util.*;

import net.outer_planes.jso.ExtensionNode;
import net.outer_planes.jso.ElementNode;

import org.jabberstudio.jso.JID;
import org.jabberstudio.jso.NSI;
import org.jabberstudio.jso.Packet;
import org.jabberstudio.jso.PacketError;
import org.jabberstudio.jso.StreamDataFactory;
import org.jabberstudio.jso.StreamElement;
import org.jabberstudio.jso.StreamObject;

import org.netbeans.lib.collab.xmpp.jso.iface.x.amp.*;

/**
 *
 * @author Jacques Belissent
 *
 */
public class AMPExtensionNode extends ExtensionNode implements AMPExtension 
{

    public static final NSI ATTRNAME_PERHOP = new NSI("per-hop", null);
    public static final NSI ATTRNAME_STATUS = new NSI("status", null);
    public static final NSI ATTRNAME_FROM = new NSI("from", null);
    public static final NSI ATTRNAME_TO = new NSI("to", null);

    public AMPExtensionNode(StreamDataFactory sdf) {
        super(sdf, NAME);
    }
    
    public AMPExtensionNode(StreamDataFactory sdf, NSI nsi) {
        super(sdf,nsi);
    }

    public AMPExtensionNode(StreamElement parent,
                            AMPExtensionNode base) {
        super(parent, base);
    }
   
    public AMPRule addRule(AMPRule.Action action, Date expires) 
        throws IllegalArgumentException 
    {
        AMPRule rule = (AMPRule)addElement("rule", AMPRule.class);
        rule.setExpirationCondition(expires);
	rule.setAction(action);
        return rule;
    }
    
    public AMPRule addRule(AMPRule.Action action,
                           AMPRule.Disposition disp) 
        throws IllegalArgumentException 
    {
        AMPRule rule = (AMPRule)addElement("rule", AMPRule.class);
        rule.setDispositionCondition(disp);
	rule.setAction(action);
        return rule;
    }
    
    public AMPRule addRule(AMPRule.Action action,
                           AMPRule.ResourceMatcher m) 
        throws IllegalArgumentException 
    {
        AMPRule rule = (AMPRule)addElement("rule", AMPRule.class);
        rule.setResourceCondition(m);
	rule.setAction(action);
        return rule;
    }
    
    public void removeRule(AMPRule rule) {
        remove(rule);
    }
    
    public List listRules() {
        return listElements("rule");
    }

    public boolean getPerHopFlag() {
        String val = getAttributeValue(ATTRNAME_PERHOP);
        return (val != null) ? Boolean.valueOf(val).booleanValue() : false;
    }
    
    public void setPerHopFlag(boolean b) {
        setAttributeValue(ATTRNAME_PERHOP, Boolean.toString(b));
    }
    
    public StreamObject copy(StreamElement parent) {
        return new AMPExtensionNode(parent,this);
    }

    public AMPRule evaluate(Packet packet,
                            AMPRule.Disposition disp,
                            String resource,
                            Date currentDate)
    {
        for (Iterator i = listRules().iterator(); i.hasNext(); ) {
            AMPRule rule = (AMPRule)i.next();
            Object cond = rule.getConditionValue();
            AMPRule.ConditionType type = rule.getConditionType();
            if (cond instanceof AMPRule.Disposition && 
                AMPRule.DISPOSITION.equals(type)) {
                if (cond.equals(disp)) return rule;
            } else if (currentDate != null && cond instanceof Date &&
                       AMPRule.EXPIRATION.equals(type)) {
                if (currentDate.after((Date)cond)) return rule;
            } else if (resource != null &&
                       cond instanceof AMPRule.ResourceMatcher &&
                       AMPRule.RESOURCE.equals(type)) {
                String intended = packet.getTo().getResource();
                if ((resource.equals(intended) && AMPRule.EXACT.equals(cond)) ||
                    (!resource.equals(intended) && AMPRule.OTHER.equals(cond)) ||
                    (AMPRule.ANY.equals(cond) && !AMPRule.DEFER.equals(rule.getAction()))) {
                    return rule;
                }
            }
        }
        return null;
    }

    public AMPRule.Action getStatus()
    {
        String action = getAttributeValue(ATTRNAME_STATUS);
        return (action != null) ? AMPUtilities.getAction(action) : null;
    }

    public JID getFrom()
    {
        String s = getAttributeValue(ATTRNAME_FROM);
        return new JID(s);
    }

    public JID getTo()
    {
        String s = getAttributeValue(ATTRNAME_TO);
        return new JID(s);
    }

    public void setTo(JID jid)
    {
        setAttributeValue(ATTRNAME_TO, jid.toString());
    }

    public void setFrom(JID jid)
    {
        setAttributeValue(ATTRNAME_FROM, jid.toString());
    }

    public void setStatus(AMPRule.Action action) {
        setAttributeValue(ATTRNAME_STATUS, action.toString());
    }

}
