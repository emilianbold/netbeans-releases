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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.search;

import java.util.List;
import javax.swing.ComboBoxModel;
import javax.swing.event.ListDataListener;

/**
 * Unmodifiable {@code ComboBoxModel} built on a {@link java.util.List}
 * of elements.
 * This implementation is very simple and assumes that the passed list
 * of elements is not empty and is not modified during this model's lifetime.
 *
 * @author  Marian Petras
 */
final class ListComboBoxModel implements ComboBoxModel {

    private final List<? extends Object> elements;
    private final int maxIndex;
    private Object selectedItem;

    public ListComboBoxModel(List<? extends Object> elements) {
        if (elements == null) {
            throw new IllegalArgumentException(
                    "the list of elements must not be null");           //NOI18N
        }
        if (elements.isEmpty()) {
            throw new IllegalArgumentException(
                    "empty list of elements is not allowed");           //NOI18N
        }
        this.elements = elements;
        this.maxIndex = elements.size() - 1;
    }

    public void setSelectedItem(Object item) {
        this.selectedItem = item;
    }

    public Object getSelectedItem() {
        return selectedItem;
    }

    public int getSize() {
        return maxIndex + 1;
    }

    public Object getElementAt(int index) {
        return elements.get(maxIndex - index);
    }

    public void addListDataListener(ListDataListener l) {
        /*
         * Does nothing as the data listeners would never be notified
         * of any change.
         */
    }

    public void removeListDataListener(ListDataListener l) {
        /*
         * Does nothing as the data listeners would never be notified
         * of any change.
         */
    }

}
