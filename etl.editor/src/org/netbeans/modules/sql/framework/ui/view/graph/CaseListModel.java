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
package org.netbeans.modules.sql.framework.ui.view.graph;

import java.util.Vector;

import javax.swing.AbstractListModel;

import org.openide.util.NbBundle;

import com.sun.sql.framework.exception.BaseException;

/**
 * This class represents model for SQLCaseArea
 * 
 * @author Ritesh Adval
 */
public class CaseListModel extends AbstractListModel {

    private Vector listItems = new Vector();

    /** Creates a new instance of CaseListModel */
    public CaseListModel() {
    }

    /**
     * Returns the value at the specified index.
     * 
     * @param index the requested index
     * @return the value at <code>index</code>
     */
    public Object getElementAt(int index) {
        if (index < listItems.size()) {
            return listItems.get(index);
        }

        return null;
    }

    /**
     * Returns the length of the list.
     * 
     * @return the length of the list
     */
    public int getSize() {
        return listItems.size();
    }

    /**
     * always add element not at the end but before last element
     * 
     * @param val value to add
     */
    public void add(Object val) {
        if (!listItems.contains(val)) {
            listItems.add(val);
            int idx = listItems.indexOf(val);
            fireIntervalAdded(this, idx, idx);
        }
    }

    /**
     * add element at the specified index
     * 
     * @param row row index
     * @param val object value
     */
    public void add(int row, Object val) {
        listItems.add(row, val);
        fireIntervalAdded(this, row, row);
    }

    /**
     * get index of an object
     * 
     * @param val object whose index needs to be found
     * @param return index of object
     */
    public int indexOf(Object val) {
        return listItems.indexOf(val);
    }

    /**
     * remove an object for the model
     * 
     * @param val object to be removed
     */
    public boolean remove(Object val) throws BaseException {
        if (listItems.size() == 1) {
            return false;
        }
        
        if (!listItems.contains(val)) {
            throw new BaseException(NbBundle.getMessage(CaseListModel.class, "ERROR_casewhen_cannotremove"));            
        }

        int idx = listItems.indexOf(val);
        listItems.remove(idx);
        fireIntervalRemoved(this, idx, idx);
        
        return true;
    }
}

