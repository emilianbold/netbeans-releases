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

    @Override
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

    @Override
    public String[] getTags() {
        tags = new String[optionList.size()];
        for (int i = 0; i < optionList.size(); i++) {
            BasicOption option = (BasicOption) optionList.get(i);
            tags[i] = option.getDisplayName();
        }

        return tags;
    }

    @Override
    public Object getValue() {
        return value;
    }

    @Override
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
            ex.printStackTrace();
        }
    }

    public void setCustomOptions(List customOptions) {
        this.optionList = customOptions;
    }

    public void setProperty(OptionPropertySupport optionS) {
        this.optionS = optionS;
        optionList = optionS.getOptions();
    }

    @Override
    public void setValue(Object val) {
        this.value = val;
    }
}