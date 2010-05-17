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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
