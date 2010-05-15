/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.wsdlextensions.ldap;

import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractListModel;

/**
 *
 * @author Gary
 */
public class SortedListModel extends AbstractListModel {

    private List items = null;

    public SortedListModel() {
        items = new ArrayList();
    }

    public int getSize() {
        return items.size();
    }

    public String getElementAt(int index) {
        return (String) items.get(index);
    }

    @SuppressWarnings("unchecked")
    public void addElement(String item) {
        if (items.size() == 0) {
            items.add(item);
        } else if (items.size() > 0) {
            boolean flag = true;
            for (int i = 0; i < items.size(); i++) {
                String str = (String) items.get(i);
                if (str.compareTo(item) > 0) {
                    items.add(i, item);
                    flag = false;
                    break;
                }
            }
            if (flag) {
                items.add(item);
            }
        }        
    }

    public void removeElement(String item) {
        if (items.size() > 0) {
            for (int i = 0; i < items.size(); i++) {
                String str = (String) items.get(i);
                if (str.compareTo(item) == 0) {
                    items.remove(i);
                }
            }
        }
    }

    public void removeElements() {
        items.clear();
    }

    public List getElements() {
        return items;
    }
}
