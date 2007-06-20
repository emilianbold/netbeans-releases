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

package org.netbeans.modules.cnd.ui.options;

import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import org.netbeans.modules.cnd.api.compilers.Tool;

/**
 * Manage a JList with both add and remove lists.
 *
 * @author gordon
 */
public class AddRemoveListModel extends DefaultListModel {
    
    private ArrayList<String> addList;
    private ArrayList removeList;
    
    /** Creates a new instance of AddRemoveListModel */
    public AddRemoveListModel() {
        addList = new ArrayList();
        removeList = new ArrayList();
    }
    
    public void addAddElement(String s) {
        if (!contains(s)) {
            for (int i = 0; i < getSize(); i++) {
                Object o = elementAt(i);
                if (o instanceof Tool) {
                    Tool tool = (Tool) o;
                    if (tool.getName().equals(s)) {
                        return;
                    }
                }
            }
            addList.add(s);
            addElement(s);
        }
    }
    
    public Object remove(int idx) {
        Object o = super.remove(idx);
        if (addList.contains(o)) {
            addList.remove(o);
        } else {
            removeList.add(o);
        }
        return o;
    }
    
    public List<String> getAddList() {
        return addList;
    }
    
    public List getRemoveList() {
        return removeList;
    }
}
