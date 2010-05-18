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

/**
 *
 *
 * @author Vijayakumar Palaniappan
 *
 */
public class XMPPPersonalStoreEntry implements PersonalStoreEntry {

    XMPPSession _session;
    XMPPPersonalStoreService _personalStoreService;
    String _displayName;
    String _type;
    String _jid;
    java.util.ArrayList _folders;

    HashMap _props = new HashMap();


    /** Creates a new instance of XMPPPersonalStoreEntry */
    public XMPPPersonalStoreEntry(XMPPSession s, String name, String type, String jid) 
    {
        _session = s;
        try {
          _personalStoreService = 
              (XMPPPersonalStoreService)_session.getPersonalStoreService();
        } catch(CollaborationException e) {
            e.printStackTrace();
        }
        _displayName = name;
        _type = type;
        _jid = jid;
        _folders = new java.util.ArrayList();
    }
    
    public void addToFolder(PersonalStoreFolder folder) 
                            throws CollaborationException 
    {
        if (PersonalStoreEntry.CONTACT.equals(_type)) {
            if (!_folders.contains(folder)) _folders.add(folder);
            ((XMPPPersonalFolder)folder).addEntry((XMPPPersonalContact)this);
        } else if (PersonalStoreEntry.GROUP.equals(_type)) {
            if (!_folders.contains(folder)) _folders.add(folder);
            ((XMPPPersonalFolder)folder).addEntry((XMPPPersonalGroup)this);         
        } else if (PersonalStoreEntry.GATEWAY.equals(_type)) {
            if (!_folders.contains(folder)) _folders.add(folder);
            ((XMPPPersonalFolder)folder).addEntry((XMPPPersonalGateway)this);
        } else if (PersonalStoreEntry.PROFILE.equals(_type)) {
            throw new CollaborationException(PersonalStoreEntry.PROFILE + " cannot be added to the folder");                        
        }
    }
    
    public String getDisplayName() {
	return _displayName;
    }
    
    public String getType() {
	return _type;
    }
    
    public String getEntryId() {
	return _jid;
    }

    public java.util.Collection getFolders() {
        return _folders;
    }
    
    public void setDisplayName(String name) {
	_displayName = name;
    }

    public void setEntryId(String id) {
        _jid = id;
    }

    public void remove() throws CollaborationException 
    {
        if ((PersonalStoreEntry.CONTACT.equals(_type)) ||
            (PersonalStoreEntry.GATEWAY.equals(_type))) {
            for (int i = 0; i < _folders.size(); i++) {
                removeFromFolder((PersonalStoreFolder)_folders.get(i));
            }
            _personalStoreService.removeEntry(this);
        } else if (PersonalStoreEntry.CONTACT_FOLDER.equals(_type)) {
            _personalStoreService.removeFolder((PersonalStoreFolder)this);
        } else if (PersonalStoreEntry.GROUP.equals(_type)) {
            for (int i = 0; i < _folders.size(); i++) {
                removeFromFolder((PersonalStoreFolder)_folders.get(i));
            }
            _personalStoreService.removePrivateStorage(this);
        } else if (PersonalStoreEntry.PROFILE.equals(_type)) {
            _personalStoreService.removeProfile();
        } else if (PersonalStoreEntry.CONFERENCE.equals(_type)) {
            _personalStoreService.removePrivateStorage(this);
        }
    }
    
    public void removeFromFolder(PersonalStoreFolder folder) 
                                 throws CollaborationException 
    {
        if (_type.equals(PersonalStoreEntry.CONTACT)) {
            ((XMPPPersonalFolder)folder).removeEntry((XMPPPersonalContact)this);
            _folders.remove(folder);
        } else if (_type.equals(PersonalStoreEntry.GROUP)) {
            ((XMPPPersonalFolder)folder).removeEntry((XMPPPersonalGroup)this);
            _folders.remove(folder);        
        } else if (_type.equals(PersonalStoreEntry.GATEWAY)) {
            ((XMPPPersonalFolder)folder).removeEntry((XMPPPersonalGateway)this);
            _folders.remove(folder);
        } 
    }
    
    public void save() throws CollaborationException 
    {
        if (_type.equals(PersonalStoreEntry.CONTACT) ||
            _type.equals(PersonalStoreEntry.GATEWAY)) {
            if (_folders.size() > 0) {
                _personalStoreService.saveEntry(this);
            } else {
                _personalStoreService.removeEntry(this);
            }
        } else if (_type.equals(PersonalStoreEntry.CONTACT_FOLDER)) {
            if (((XMPPPersonalFolder)this).size() > 0) {
                XMPPPersonalStoreService.saveFolder((PersonalStoreFolder)this);
            }
        } else if (_type.equals(PersonalStoreEntry.GROUP)) {
            if (_folders.size() > 0) {
                _personalStoreService.setPrivateStorage(this);
            } else {
                _personalStoreService.removePrivateStorage(this);
            }
        } else if (_type.equals(PersonalStoreEntry.CONFERENCE)) {
            _personalStoreService.setPrivateStorage(this);
        } 
    }
    
    public CollaborationPrincipal getPrincipal() {
        return new XMPPPrincipal(new org.jabberstudio.jso.JID(_jid), 
                                 _displayName);
    }

    public boolean equals(Object o) {
        if (o instanceof XMPPPersonalFolder) {
            XMPPPersonalFolder f = (XMPPPersonalFolder)o;
            String displayName = f.getDisplayName();
            if (displayName != null && displayName.equalsIgnoreCase(_displayName)) {
                return true;
            }
        } else if (o instanceof XMPPPersonalStoreEntry) {
            XMPPPersonalStoreEntry e = (XMPPPersonalStoreEntry)o;
            String entryId = e.getEntryId();
            if (entryId != null && entryId.equalsIgnoreCase(_jid)) {
                return true;
            }
        }
        return false;
    }
    
    public void clearFolders() {
        _folders.clear();
    }
    
    public String toString() {
        return "JID: " + _jid + 
                " DisplayName: " + _displayName + 
                " type: " + _type +
                " folders " + _folders;
    }

}
