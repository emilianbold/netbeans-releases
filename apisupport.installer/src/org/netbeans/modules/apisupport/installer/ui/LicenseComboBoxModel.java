/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.apisupport.installer.ui;

import java.util.LinkedList;
import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;

/**
 *
 * @author Dmitry Lipin
 */
public class LicenseComboBoxModel implements ComboBoxModel {

    private List<ListDataListener> listeners;
    private List<String> names;
    private List<String> types;
    private String selectedItem;
    private boolean selectedItemFromList;

    public LicenseComboBoxModel(List<String> names, List<String> types) {
        this.names = new LinkedList<String>();
        this.names.addAll(names);

        this.types = new LinkedList<String>();
        this.types.addAll(types);

        this.listeners = new LinkedList<ListDataListener>();

        if (names.size() > 0) {
            this.selectedItem = names.get(0);
            this.selectedItemFromList = true;
        } else {
            this.selectedItem = "";
            this.selectedItemFromList = false;
        }
    }

    public List<String> getTypes() {
        return types;
    }

    public List<String> getNames() {
        return names;
    }

    public String getName() {
        if (selectedItemFromList) {
            return names.get(names.indexOf(selectedItem));
        } else {
            return selectedItem;
        }
    }

    // comboboxmodel ////////////////////////////////////////////////////////////
    public void setSelectedItem(Object item) {
        selectedItem = (String) item;

        if (names.indexOf(item) != -1) {
            selectedItemFromList = true;
        } else {
            selectedItemFromList = false;
        }

        fireContentsChanged(-1);
    }

    public Object getSelectedItem() {
        return selectedItem;
    }

    public int getSize() {
        return names.size();
    }

    public Object getElementAt(int index) {
        return names.get(index);
    }

    public void addListDataListener(ListDataListener listener) {
        synchronized (listeners) {
            listeners.add(listener);
        }
    }

    public void removeListDataListener(ListDataListener listener) {
        synchronized (listeners) {
            listeners.remove(listener);
        }
    }

    // private //////////////////////////////////////////////////////////////////
    private void fireContentsChanged(int index) {
        final ListDataListener[] clone;
        synchronized (listeners) {
            clone = listeners.toArray(new ListDataListener[listeners.size()]);
        }

        final ListDataEvent event = new ListDataEvent(
                this,
                ListDataEvent.CONTENTS_CHANGED,
                index,
                index);

        for (ListDataListener listener : clone) {
            listener.contentsChanged(event);
        }
    }
}

