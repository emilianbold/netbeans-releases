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

package org.netbeans.modules.tasklist.ui.checklist;

/**
 * Default model for a CheckList
 */
public class DefaultCheckListModel extends AbstractCheckListModel {

    private static final long serialVersionUID = 1;

    private boolean state[] = new boolean[0];
    private Object[] values = new Object[0];

    /**
     * Creates a new empty model
     */
    public DefaultCheckListModel() {
    }

    /**
     * Creates a new model with the given state of checkboxes and the given
     * values
     *
     * @param state state of the checkboxes. A copy of this array will NOT be
     * created.
     * @param values values. A copy of this array will NOT be
     * created.
     */
    public DefaultCheckListModel(boolean[] state, Object[] values) {
        if (state.length != values.length)
            throw new IllegalArgumentException("state.length != values.length"); //NOI18N
        this.state = state;
        this.values = values;
    }
    
    public boolean isChecked(int index) {
        return state[index];
    }
    
    public void setChecked(int index, boolean c) {
        state[index] = c;
        fireContentsChanged(this, index, index);
    }

    public int getSize() {
        return values.length;
    }

    public Object getElementAt(int index) {
        return values[index];
    }
}
