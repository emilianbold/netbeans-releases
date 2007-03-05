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
package com.sun.rave.web.ui.model;

import com.sun.rave.web.ui.util.MessageUtil;
import java.util.ArrayList;
import java.io.Serializable;

/**
 * Represents a list of selectable options, which can be used to
 * initialize the <code>items</code> property of all selector-based components
 * (<code>Listbox</code>, <code>Dropdown</code>, <code>JumpDropdown</code>,
 * <code>CheckboxGroup</code>, <code>RadioButtonGroup</code>), and
 * <code>AddRemove</code>.
 *
 * @author gjmurphy
 */
public class OptionsList implements Serializable {

    private ArrayList options;
    private ArrayList selectedValues;
    private boolean isMultiple;

    public boolean isMultiple() {
        return isMultiple;
    }

    public void setMultiple(boolean isMultiple) {
        this.isMultiple = isMultiple;
    }

    public OptionsList() {
        options = new ArrayList();
        selectedValues = new ArrayList();
        isMultiple = false;
    }
    
    public void setOptions(Option[] options) {
        this.options.clear();
        if (options == null)
            return;
        for (int i = 0; i < options.length; i++)
            this.options.add(options[i]);
    }
    
    public Option[] getOptions() {
        Option[] options = new Option[this.options.size()];
        this.options.toArray(options);
        return options;
    }
    
    /**
     * If this options list is in "multiple" mode, value specified may be
     * an array of objects or a singleton. Otherwise, the value is treated as
     * a singleton.
     */
    public void setSelectedValue(Object value) {
        selectedValues.clear();
        if (value == null)
            return;
        if (value instanceof Object[]) {
            Object[] values = (Object[]) value;
            for (int i = 0; i < values.length; i++)
                selectedValues.add(values[i]);
        } else {
            selectedValues.add(value);
        }
    }
    
    /**
     * If this options list is in "multiple" mode, returns an array of objects;
     * otherwise, returns a singleton. By default, in "multiple" mode, returns
     * an empty object array, otherwise, returns a null object.
     */
    public Object getSelectedValue() {
        if (isMultiple) {
            if (selectedValues.size() == 0)
                return new Object[0];
            return selectedValues.toArray();
        } else {
            if (selectedValues.size() == 0) 
                return null;
            return selectedValues.get(0);
        }
    }
    
}
