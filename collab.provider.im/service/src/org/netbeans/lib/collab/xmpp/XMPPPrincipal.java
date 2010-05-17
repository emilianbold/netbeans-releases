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
import org.netbeans.lib.collab.xmpp.*;
import org.jabberstudio.jso.*;
import org.netbeans.lib.collab.CollaborationPrincipal;


/**
 *
 *
 */
public class XMPPPrincipal implements CollaborationPrincipal {

    String _displayName = null;

    private Properties _attrs = new Properties();
    JID _jid;

    /** Creates a new instance of XMPPPrincipal */
    public XMPPPrincipal(JID jid) {	
        setJID(jid);
    }

    public XMPPPrincipal(String jid) throws JIDFormatException {
        this(new JID(jid));	        
    }


    public XMPPPrincipal(JID jid, String displayName) {
        this(jid);	
        _displayName = displayName;        
    }

    public String getJid() {
        return getJID().toString();
    }
    
    public String getUID() {
	//return getJID().toBareJID().toString();
        return getJID().toString();
    }
    
    public JID getJID() { return _jid; }
    public void setJID(JID jid) { 
        _jid = jid;
    }

    public String getName() {
	return getJID().getNode();
    }

    public String getFQName() {
	return getJID().toBareJID().toString();
    }

    public String getDisplayName() {
        if (_displayName == null) {
            return getFQName();
        }
	return _displayName;
    }
    
    public String getDomainName() {
	return getJID().getDomain();
    }

    public String getResource() {
        return getJID().getResource();
    }

    /**
     * @deprecated
     */
    private void setAttribute(String name, Object value) {
        if (_attrs == null) _attrs = new Properties();
        _attrs.put(name, value);
    }

    public void setAttributes(Map attrs) {
        if (attrs instanceof Properties) {
            this._attrs = (Properties)attrs;
        } else {
            if (_attrs == null) _attrs = new Properties();
            _attrs.putAll(attrs);
        }
    }

    public Properties getAttributes() {
        return _attrs;
    }

    public Object getValue(String attributeName) {
        if (_attrs == null) return null;
        return _attrs.get(attributeName);
    }

    public void setProperty(String name, String value) {
        _attrs.put(name, value);
    }

    public String getProperty(String attributeName) {
        if (_attrs == null) return null;
        String val = null;
        Object o = _attrs.get(attributeName);
        if (o instanceof Set) {
            Set s = (Set)o;
            if (s != null && !s.isEmpty()) {
                val = (String)s.iterator().next();
            } 
        } else {
            val = (String)o;
        }
        return val;
    }

    public Set getAttributeValues(String attributeName) {
        if (_attrs == null) return null;
        Set val = null;
        Object o = _attrs.get(attributeName);
        if (o instanceof String) {
            val = new HashSet();
            val.add(o);
        } else {
            val = (Set)o;
        }
        return val;
    }

    public Enumeration propertyNames()
    {
	return _attrs.propertyNames();
    }

    /**
     * set the values of a single-valued or multi-valued attribute
     */
    public void setAttributeValues(String attribute, Set values) {
        if (_attrs == null) _attrs = new Properties();
        _attrs.put(attribute, values);
    }

	public boolean equals(Object o) {
	    return (o != null && o instanceof CollaborationPrincipal &&
		        ((CollaborationPrincipal)o).getUID().equals(getUID()));
    }

}
