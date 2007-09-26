/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s):
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
    private final boolean reverseOrder;
    private Object selectedItem;

    public ListComboBoxModel(List<? extends Object> elements) {
        this(elements, false);
    }

    public ListComboBoxModel(List<? extends Object> elements,
                             final boolean reverseOrder) {
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
        this.reverseOrder = reverseOrder;
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
        return elements.get(reverseOrder ? maxIndex - index
                                         : index);
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
