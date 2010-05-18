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

/**
 *
 *
 * @author Vijayakumar Palaniappan
 *
 */
public class XMPPPersonalProfile extends XMPPPersonalStoreEntry
                                 implements PersonalProfile
{

    java.util.Map _props;
    java.util.Map _attrs;
    java.util.Map _ISServicePolicyAttrs;
    java.util.Map _properties;
    XMPPSession _session;

    /** Creates a new instance of XMPPPersonalProfile */
    public XMPPPersonalProfile(XMPPSession s, java.util.Properties p, java.util.Properties a, 
                                java.util.Properties isServiceAttrs) {
        super(s, null, PersonalStoreEntry.PROFILE, null);
        init(s, p, a, isServiceAttrs); 
    }

    public XMPPPersonalProfile(XMPPSession s, String jid, String name, java.util.Properties p,
                                java.util.Properties a, java.util.Properties isServiceAttrs) {
        super(s, name, PersonalStoreEntry.PROFILE, jid);
        init(s, p, a, isServiceAttrs); 
    }
    
    /**
     * initialize all the instance variables in one place
     */
    private void init(XMPPSession s, java.util.Properties p,
                    java.util.Properties a, java.util.Properties ISServiceAttrs) {        
        _session = s;
        _props = p;
        _attrs = a;
        _ISServicePolicyAttrs = ISServiceAttrs;
        _properties = new java.util.HashMap();
        if (p != null) _properties.putAll(_props);
        if (a != null) _properties.putAll(_attrs);
        if (ISServiceAttrs != null) _properties.putAll(_ISServicePolicyAttrs);
    }
    
    public int size() {
        return _properties.size();
    }
    
    public java.util.Map getProperties() {
	return _properties;
    }
    
    public java.util.Map getProperties(boolean all) {
        if (all) return _properties;
	else return _props;
    }
    
    public java.util.Set getProperty(String str) {
        
	Object o = _properties.get(str);
        if (o == null) return null;
        if (o instanceof java.util.Set) return (java.util.Set)o;
        java.util.Set s = new java.util.HashSet();
        s.add(o);
        return s;         
    }
    
    public String getProperty(String str, String str1) {
        Object o = _properties.get(str);
        if (o == null) return str1;
        if (o instanceof String) {
            return (String)o;
        } else if (o instanceof java.util.Set) {
            java.util.Set v = (java.util.Set)o;
            if (v.size() == 0) return str1;
            else return (String)v.iterator().next();
        } else {
            return str1;
        }
    }
    
    public java.util.Set getProperty(String str, java.util.Set set) {
        java.util.Set s = getProperty(str);
        if (s == null) return set;
        return s;
        /*
	Object o = _properties.get(str);
        if (o == null) return set;
        if (o instanceof java.util.Set) return (java.util.Set)o;
        java.util.Set s = new java.util.HashSet();
        s.add(o);
        return s;
         */
    }
    
    public java.util.Set getPropertyNames() {
	return _properties.keySet();
    }
    
    public void removeProperty(String str) {
        if ((_attrs != null && _attrs.get(str) != null) ||
            (_ISServicePolicyAttrs != null) && (_ISServicePolicyAttrs.get(str) != null)) 
        {
            return;
        }
        _properties.remove(str);
        _props.remove(str);
    }
    
    public void setProperty(String str, java.util.Set set) {
        /*
        if (_attrs.get(str) == null
            || _ISServicePolicyAttrs.get(str)==null) {
             // it is neither a user attr nor a policy attr, safe to change   
            _properties.put(str, set);
            _props.put(str, set);
        }*/
        storeProperty(str, set);
    }
    
    public void setProperty(String str, String str1) {
        //if (_properties.get(str) == null) {
        /*
        if (_attrs.get(str) == null
            || _ISServicePolicyAttrs.get(str)==null) {
            _properties.put(str, str1);
            _props.put(str, str1);
        }
         */
        storeProperty(str, str1);
    }
    
    private void storeProperty(String str, Object o) {
        if ((_attrs != null && _attrs.get(str) != null) ||
            (_ISServicePolicyAttrs != null) && (_ISServicePolicyAttrs.get(str) != null)) 
        {
            return;
        }
        // it is neither a user attr nor a policy attr, safe to change
        _properties.put(str, o);
        _props.put(str, o);
    }

    public void clear(boolean all) {
        if (all) {
            if (_attrs != null) _attrs.clear();
            if (_ISServicePolicyAttrs != null) _ISServicePolicyAttrs.clear();
        }
        _props.clear();
        _properties.clear();
        if (_attrs != null) _properties.putAll(_attrs);
        if (_ISServicePolicyAttrs != null) _properties.putAll(_ISServicePolicyAttrs);
    }

    public void save() throws CollaborationException {
        XMPPPersonalStoreService pss = 
            (XMPPPersonalStoreService)_session.getPersonalStoreService();       
        Properties m = (Properties)((Properties)_props).clone();
        for (Iterator i = m.keySet().iterator(); i.hasNext();) {
            String property = (String)i.next();
            if ((_attrs != null) && (_attrs.containsKey(property))) {
                _props.remove(property);
            } else if ((_ISServicePolicyAttrs != null) &&
                       (_ISServicePolicyAttrs.containsKey(property))) 
            {
                _props.remove(property);
            }
        }
        pss.saveProfile(this);
    }
    
}
