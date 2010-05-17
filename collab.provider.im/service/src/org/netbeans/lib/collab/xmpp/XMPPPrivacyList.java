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

/**
 *
 *
 * @author Rahul Shah
 *
 */
public class XMPPPrivacyList implements org.netbeans.lib.collab.PrivacyList {

    private String _name;
    private LinkedList _items = new LinkedList();

    /** Creates a new instance of XMPPPrivacyList */
    public XMPPPrivacyList(String name) {
        _name = name;
    }

    /**
     * return The name of this privacy list
     */
    public String getName() {
        return _name;
    }

    /**
     * @param name The name of this privacy list
     */
    public void setName(String name) {
        _name = name;
    }
        
    /**
     * return enumeration of PrivacyItem objects
     */
    public Collection getPrivacyItems() throws CollaborationException {
	return (Collection)_items;
    }
    
    /*
     * gets the access for the id
     * @param type Type as specified in PrivacyItem
     * @param id 
     */
    /*public int getAccess(String type, String id) throws CollaborationException 
    {
	PrivacyItem[] items = getPrivacyItems();
	for (int i = 0; i < items.length; i++) {
	    if (items[i].getType().equals(type)) {
		if (items[i].hasSubject(id)) return items[i].getAccess();
	    }
	}
	throw new CollaborationException("Item not found");
    }*/
    
    /** Creates a new PrivacyItem for this privacy list
     * @param type The Type of the Privacy Item.
     * @param access The access level for this privacy item
     */    
    public PrivacyItem createPrivacyItem(String type, int access) throws CollaborationException {
        return new XMPPPrivacyItem(type,access);
    }
    
    /** Add PrivacyItem to the PrivacyList
     * @param item Item to be added to the PrivacyList
     */    
    public void addPrivacyItem(PrivacyItem item) throws CollaborationException {
        _items.add(item);
    }
    
    /** Remove PrivacyItem from the PrivacyList
     * @param item Item to be added to the PrivacyList
     */    
    public void removePrivacyItem(PrivacyItem item) throws CollaborationException {
        _items.remove(item);
    }
    
    /** 
     * Removes all PrivacyItem from the PrivacyList
     */    
    public void reset() throws CollaborationException {
        _items = new LinkedList();
    }
}
