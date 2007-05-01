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

import java.beans.PropertyEditorSupport;
import java.util.List;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class OptionListEditor extends PropertyEditorSupport {
    List optionList;
    private OptionPropertySupport optionS;
    private String[] tags;

    private Object value = "ddd";

    public OptionListEditor() {
        super();
    }

    public String getAsText() {
        try {
            String optionVal = "" + optionS.getValue();
            BasicOption option = getOptionForValue(optionVal);
            if (option != null) {
                return option.getDisplayName();
            }

            // if we have a default option defined in xml
            int defOpt = optionS.getDefaultOption();
            if (defOpt < optionList.size()) {
                option = (BasicOption) optionList.get(defOpt);
                optionVal = option.getDisplayName();
            }

            return optionVal;

        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return "";
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

    public BasicOption getOptionForValue(String value1) {
        for (int i = 0; i < optionList.size(); i++) {
            BasicOption option = (BasicOption) optionList.get(i);
            if (value1 != null && value1.equals(option.getValue())) {
                return option;
            }
        }

        return null;
    }

    public String[] getTags() {
        tags = new String[optionList.size()];
        for (int i = 0; i < optionList.size(); i++) {
            BasicOption option = (BasicOption) optionList.get(i);
            tags[i] = option.getDisplayName();
        }

        return tags;
    }

    public Object getValue() {
        return value;
    }

    public void setAsText(String text) {
        BasicOption option = null;

        try {                        
            option = getOptionForDisplayName(text);
            if (option != null) {
                optionS.setValue(option.getValue());
                setValue(option.getValue());
            } else {
                optionS.setValue(text);
                setValue(text);
            }

        } catch (Exception ex) {
            // log this exception
        }
    }

    public void setCustomOptions(List customOptions) {
        this.optionList = customOptions;
    }

    public void setProperty(OptionPropertySupport optionS) {
        this.optionS = optionS;
        optionList = optionS.getOptions();
    }

    public void setValue(Object val) {
        this.value = val;
    }

}

