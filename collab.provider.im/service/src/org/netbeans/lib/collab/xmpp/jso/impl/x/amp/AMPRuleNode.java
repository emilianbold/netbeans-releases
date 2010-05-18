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

import java.util.Date;

import org.jabberstudio.jso.StreamDataFactory;
import org.jabberstudio.jso.StreamElement;
import org.jabberstudio.jso.StreamObject;
import org.jabberstudio.jso.StreamNode;
import org.jabberstudio.jso.NSI;
import org.jabberstudio.jso.format.DateTimeProfileFormat;
import net.outer_planes.jso.ElementNode;

import org.netbeans.lib.collab.xmpp.jso.iface.x.amp.*;

/**
 * implementation of AMPRule
 *
 * @author Jacques Belissent
 */
public class AMPRuleNode extends ElementNode implements AMPRule
{
    
    public static final NSI ATTRNAME_CONDITION = new NSI("condition", null);
    public static final NSI ATTRNAME_ACTION = new NSI("action", null);
    public static final NSI ATTRNAME_VALUE = new NSI("value", null);

    private static DateTimeProfileFormat df = 
        DateTimeProfileFormat.getInstance(DateTimeProfileFormat.DATETIME);
    
    /** Creates a new instance of AMPRuleNode */
    public AMPRuleNode(StreamDataFactory sdf, NSI name) {
        super(sdf, name);
    }
    
    public AMPRuleNode(StreamDataFactory sdf) {
        super(sdf, NAME);
    }
    
    protected AMPRuleNode(StreamElement parent, AMPRuleNode base) {
        super(parent, base);
    }
    
    public void setAction(AMPRule.Action action) 
        throws IllegalArgumentException 
    {
        setAttributeObject(ATTRNAME_ACTION, action);
    }
    
    public void setExpirationCondition(Date expires) 
        throws IllegalArgumentException 
    {
        setAttributeObject(ATTRNAME_CONDITION, EXPIRATION.toString());
        setAttributeValue(ATTRNAME_VALUE, df.format(expires));
    }
    
    public void setDispositionCondition(AMPRule.Disposition disp) 
        throws IllegalArgumentException 
    {
        setAttributeObject(ATTRNAME_CONDITION, DISPOSITION.toString());
        setAttributeValue(ATTRNAME_VALUE, disp.toString());
    }
    
    public void setResourceCondition(AMPRule.ResourceMatcher matcher) 
        throws IllegalArgumentException 
    {
        setAttributeObject(ATTRNAME_CONDITION, RESOURCE.toString());
        setAttributeValue(ATTRNAME_VALUE, matcher.toString());
    }
    
    
    public Object getConditionValue() {
        Object c = null;
        try {
            String cond = getAttributeValue(ATTRNAME_CONDITION);
            String val = getAttributeValue(ATTRNAME_VALUE);
            if (EXPIRATION.equals(cond)) {
                c = df.parse(val);
            } else if (DISPOSITION.equals(cond)) {
                c = AMPUtilities.getDisposition(val);
            } else if (RESOURCE.equals(cond)) {
                c = AMPUtilities.getResourceMatcher(val);
            }
        } catch (Exception e) {
        }
        return c;
    }

    public ConditionType getConditionType() {
        String cond = getAttributeValue(ATTRNAME_CONDITION);
        if (EXPIRATION.equals(cond)) { 
            return EXPIRATION;
        } else if (DISPOSITION.equals(cond)) {
            return DISPOSITION;
        } else if (RESOURCE.equals(cond)) {
            return RESOURCE;
        } else {
            return null;
        }
    }

    public AMPRule.Action getAction() {
        String action = getAttributeValue(ATTRNAME_ACTION);
        return (action != null) ? AMPUtilities.getAction(action) : null;
    }

    public StreamObject copy(StreamElement parent) {
        return new AMPRuleNode(parent, this);
    }

    public boolean equals(Object o) {
        if (o instanceof AMPRuleNode) {
            AMPRuleNode r = (AMPRuleNode)o;
            if (r.getAction() != null && 
                !r.getAction().equals(getAction())) return false;
            if (getAction() != null && 
                !getAction().equals(r.getAction())) return false;
            if (r.getConditionValue() != null && 
                !r.getConditionValue().equals(getConditionValue())) return false;
            if (getConditionValue() != null && 
                !getConditionValue().equals(r.getConditionValue())) return false;
            return true;
        } else {
            return false;
        }
    }

    public boolean matches(Object o) {
        if (o instanceof AMPRuleNode) {
            AMPRuleNode r = (AMPRuleNode)o;
            if (r.getAction() != null && 
                !r.getAction().equals(getAction())) return false;
            if (r.getConditionValue() != null && 
                !r.getConditionValue().equals(getConditionValue())) return false;
            return true;
        } else {
            return false;
        }
    }

}
