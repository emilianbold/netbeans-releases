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

import org.netbeans.lib.collab.*;

/**
 *
 *
 * @author Vijayakumar Palaniappan
 *
 */
public class XMPPPersonalFolder extends XMPPPersonalStoreEntry
                                implements PersonalStoreFolder
{

    java.util.Vector _contactEntries;
    java.util.Vector _groupEntries;
    java.util.Vector _gatewayEntries;

    /** Creates a new instance of XMPPPersonalFolder */
    public XMPPPersonalFolder(XMPPSession s, String name) {
        super(s, name, PersonalStoreEntry.CONTACT_FOLDER, null);
        _contactEntries = new java.util.Vector();
        _groupEntries = new java.util.Vector();
        _gatewayEntries = new java.util.Vector();
    }
    
    public java.util.Collection getEntries() throws CollaborationException {
	java.util.Vector _entries = new java.util.Vector();
        _entries.addAll(_contactEntries);
        _entries.addAll(_groupEntries);
        _entries.addAll(_gatewayEntries);  
        return _entries;
    }
    
    public java.util.Collection getEntries(String type) throws CollaborationException {
        if (type.equals(PersonalStoreEntry.CONTACT)) {
            return _contactEntries;
        } else if (type.equals(PersonalStoreEntry.GROUP)) {
            return _groupEntries;
        } else if (type.equals(PersonalStoreEntry.GATEWAY)) {
            return _gatewayEntries;
        } else {
            return null;
        }
    }
    
    public PersonalStoreEntry getEntry(String jid) throws CollaborationException {
        java.util.Vector _entries = _contactEntries;
        for (java.util.Enumeration e = _entries.elements(); 
                                                    e.hasMoreElements(); ) {
            PersonalStoreEntry pse = (PersonalStoreEntry)e.nextElement();
            if (pse.getEntryId().equals(jid)) return pse;
        }
        _entries = _gatewayEntries;
        for (java.util.Enumeration e = _entries.elements(); 
                                                    e.hasMoreElements(); ) {
            PersonalStoreEntry pse = (PersonalStoreEntry)e.nextElement();
            if (pse.getEntryId().equals(jid)) return pse;
        }
        _entries = _groupEntries;
        // The only way users can get jid of group is by getUID() which
        //returns a decoded string. So it must be encoded here.
        jid = JIDUtil.encodedJID(jid).toString();
        for (java.util.Enumeration e = _entries.elements(); 
                                                    e.hasMoreElements(); ) {
            PersonalStoreEntry pse = (PersonalStoreEntry)e.nextElement();
            if (pse.getEntryId().equals(jid)) return pse;
        }
	return null;
    }
    
    public boolean hasEntry(String jid) {
	PersonalStoreEntry entry = null;
        try {
            entry = getEntry(jid);
        } catch (Exception e) {
        }
        return (entry != null);
    }

    protected void addEntry(XMPPPersonalContact entry) {
        if (!_contactEntries.contains((PersonalStoreEntry)entry)) {
            _contactEntries.addElement((PersonalStoreEntry)entry);
        }
    }

    protected void addEntry(XMPPPersonalGroup entry) {
        if (!_groupEntries.contains((PersonalStoreEntry)entry)) {
            _groupEntries.addElement((PersonalStoreEntry)entry);
        }
    }
    
    protected void addEntry(XMPPPersonalGateway entry) {
        if (!_gatewayEntries.contains((PersonalStoreEntry)entry)) {
            _gatewayEntries.addElement((PersonalStoreEntry)entry);
        }
    }

    protected void removeEntry(XMPPPersonalContact entry) {
        _contactEntries.remove((PersonalStoreEntry)entry);
    }

    protected void removeEntry(XMPPPersonalGroup entry) {
        _groupEntries.remove((PersonalStoreEntry)entry);
    }
    
    protected void removeEntry(XMPPPersonalGateway entry) {
        _gatewayEntries.remove((PersonalStoreEntry)entry);
    }
    
    public void rename(String name) {
	_personalStoreService.renameFolder(this, name);
    }
    
    public int size() {
	return _contactEntries.size() + _groupEntries.size() + _gatewayEntries.size();
    }
    
}
