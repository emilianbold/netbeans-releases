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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package org.netbeans.progress.spi;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionListener;
import org.netbeans.progress.module.*;

/**
 *
 * @author Milos Kleint (mkleint@netbeans.org)
 */
public final class TaskModel {
    private DefaultListSelectionModel selectionModel;
    private DefaultListModel model;
    private InternalHandle explicit;
    /** Creates a new instance of TaskModel */
    public TaskModel() {
        selectionModel = new DefaultListSelectionModel();
        model = new DefaultListModel();
    }
    
    
    
    public void addHandle(InternalHandle handle) {
        model.addElement(handle);
        if (handle.isUserInitialized() && explicit == null) {
            selectionModel.setSelectionInterval(model.size() - 1, model.size() - 1);
        }
    }
    
    public void removeHandle(InternalHandle handle) {
        if (explicit == handle) {
            explicit = null;
        }
        int index = model.indexOf(handle);
        if (selectionModel.getMinSelectionIndex() == index) {
            // if we are removing the handle that is selected, do tricks with selection
            // too figure out which one should be sleected now
            changeSelection(index);
        }
        InternalHandle selectedHandle = getSelectedHandle();
        model.removeElement(handle);
        if (selectedHandle != null) {
            selectionModel.setSelectionInterval(model.indexOf(selectedHandle), model.indexOf(selectedHandle));
        } else {
           //TODO what to do here? 
            selectionModel.clearSelection();
        }
        
    }
    
    /**
     * if we are removing the handle that is selected, do tricks with selection
     * too figure out which one should be sleected no     
     */
    private void changeSelection(int current) {
        InternalHandle last = null;
        for (int i = 0; i < model.size(); i++) {
            if (current != i) {
                InternalHandle handle = (InternalHandle)model.getElementAt(i);
                if (handle.isUserInitialized()) {
                    last = handle;
                } else if (last == null) {
                    last = handle;
                }
            }
        }
        if (last != null) {
            selectionModel.setSelectionInterval(model.indexOf(last), model.indexOf(last));
        } else {
            selectionModel.clearSelection();
        }
        
    }
    
    public void explicitlySelect(InternalHandle handle) {
        explicit = handle;
        int index = model.indexOf(explicit);
        if (index == -1) {
            //TODO what?
        }
        selectionModel.setSelectionInterval(index, index);
    }
    
    public InternalHandle getExplicitSelection() {
        return explicit;
    }
    
    public int getSize() {
        return model.size();
    }
           
    
    public InternalHandle[] getHandles() {
        InternalHandle[] handles = new InternalHandle[model.size()];
        model.copyInto(handles);
        return handles;
    }
    
    public InternalHandle getSelectedHandle() {
        int select = selectionModel.getMinSelectionIndex();
        if (select != -1) {
            if (select >= 0 && select < model.size()) {
                return (InternalHandle)model.getElementAt(selectionModel.getMinSelectionIndex());
            }
        }
        return null;
    }
    
    public void addListSelectionListener(ListSelectionListener listener) {
        selectionModel.addListSelectionListener(listener);
    }
    
    public void removeListSelectionListener(ListSelectionListener listener) {
        selectionModel.removeListSelectionListener(listener);
    }
    
    public void addListDataListener(ListDataListener listener) {
        model.addListDataListener(listener);
    }
    
    public void removeListDataListener(ListDataListener listener) {
        model.removeListDataListener(listener);
    }
}
