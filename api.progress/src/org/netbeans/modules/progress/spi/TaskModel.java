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


package org.netbeans.modules.progress.spi;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Milos Kleint (mkleint@netbeans.org)
 * @since org.netbeans.api.progress/1 1.18
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
        updateSelection();
    }
    
    public void removeHandle(InternalHandle handle) {
        if (explicit == handle) {
            explicit = null;
        }
        model.removeElement(handle);
        updateSelection();
    }
    
    /** Impl of selection policy. Tasks which are not in sleep mode and
     * user initiated are preferred for selection.
     */
    void updateSelection () {
        // don't touch selection if explicit
        if (explicit != null) {
            return;
        }
        InternalHandle oldSelected = getSelectedHandle();
        // keep selection if possible
        if (oldSelected != null && !oldSelected.isInSleepMode()) {
            return;
        }

        // select last added that is not in sleep mode and preferrably userInitiated
        InternalHandle toSelect = null;
        for (int i = 0; i < model.size(); i++) {
            InternalHandle curHandle = (InternalHandle)model.getElementAt(i);
            if (getSelectionRating(curHandle) >= getSelectionRating(toSelect)) {
                toSelect = curHandle;
            }
        }
        if (toSelect != null) {
            selectionModel.setSelectionInterval(model.indexOf(toSelect), model.indexOf(toSelect));
        } else {
            selectionModel.clearSelection();
        }
    }

    private int getSelectionRating (InternalHandle handle) {
        int result = 0;
        if (handle != null) {
            if (!handle.isInSleepMode()) {
                result += 4;
            }
            if (handle.isUserInitialized()) {
                result += 2;
            }
            result += 1;
        }
        return result;
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
