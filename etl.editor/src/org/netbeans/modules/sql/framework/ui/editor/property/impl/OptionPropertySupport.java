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

package org.netbeans.modules.sql.framework.ui.editor.property.impl;

import java.util.ArrayList;
import java.util.List;

import org.netbeans.modules.sql.framework.ui.editor.property.IElement;
import org.netbeans.modules.sql.framework.ui.editor.property.IOptionProperty;
import org.netbeans.modules.sql.framework.ui.editor.property.IPropertyCustomizer;


/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class OptionPropertySupport extends BasicPropertySupport implements IOptionProperty {

    private int defaultOption = 0;

    private ArrayList optionList = new ArrayList();

    public OptionPropertySupport() {
        super();
    }

    /** Creates a new instance of OptionPropertySupport */
    public OptionPropertySupport(String valueType) {
        super(valueType);
    }

    /**
     * add a element in the node
     * 
     * @param element element to add
     */
    public void add(IElement element) {
        element.setParent(this);
        optionList.add(element);
    }

    public int getDefaultOption() {
        return defaultOption;
    }

    public BasicOption getOptionForDisplayName(String displayName) {
        for (int i = 0; i < optionList.size(); i++) {
            BasicOption option = (BasicOption) optionList.get(i);
            if (displayName != null && displayName.equals(option.getDisplayName())) {
                return option;
            }
        }
        return null;
    }

    public BasicOption getOptionForValue(String value) {
        for (int i = 0; i < optionList.size(); i++) {
            BasicOption option = (BasicOption) optionList.get(i);
            if (value != null && value.equals(option.getValue())) {
                return option;
            }
        }
        return null;
    }

    public List getOptions() {
        return optionList;
    }

    public void setDefaultOption(String defOption) {
        defaultOption = Integer.parseInt(defOption);
    }

    /**
     * set the optional property editor which can be used to edit this property
     * 
     * @return property editor
     */
    public void setEditorClass(String editorClass) {
        super.setEditorClass(editorClass);
        if (this.getPropertyEditor() instanceof OptionListEditor) {
            ((OptionListEditor) this.getPropertyEditor()).setProperty(this);
        }
    }

    /**
     * set the property customizer
     * 
     * @param customizer customizer
     */
    public void setPropertyCustomizer(IPropertyCustomizer customizer) {
        super.setPropertyCustomizer(customizer);
        if (customizer == null) {
            return;
        }

        if (this.getPropertyEditor() instanceof OptionListEditor) {
            OptionListEditor listEditor = (OptionListEditor) this.getPropertyEditor();
            listEditor.setCustomOptions(customizer.getOptions());

        }
    }

    // override set value to handle Integer options; if optionProperty type is integer we
    // want to convert passed String optiong value to integer
    public void setValue(Object obj) throws java.lang.IllegalAccessException, java.lang.IllegalArgumentException,
            java.lang.reflect.InvocationTargetException {
        if (this.getValueType() == Integer.class && obj instanceof String) {
            try {
                super.setValue(new Integer((String) obj));
            } catch (NumberFormatException ex) {
                ex.printStackTrace();
                // log me
            }
        } else {
            super.setValue(obj);
        }
    }
}

