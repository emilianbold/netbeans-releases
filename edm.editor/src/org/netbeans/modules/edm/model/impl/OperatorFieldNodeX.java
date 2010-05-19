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

package org.netbeans.modules.edm.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.netbeans.modules.edm.model.EDMException;
import org.openide.util.NbBundle;

public class OperatorFieldNodeX implements org.netbeans.modules.edm.editor.graph.jgo.IOperatorField {

    private String fieldName;

    private String fieldDisplayName;

    private HashMap map = new HashMap();

    private String tTip;

    private boolean edit;

    private Object fieldData;

    private boolean isStatic;

    private List acceptables = Collections.synchronizedList(new ArrayList());

    private List displayAcceptables = Collections.synchronizedList(new ArrayList());

    private String defaultAcceptable;

    /** Creates a new instance of OperatorFieldNodeX */
    public OperatorFieldNodeX(String name, String displayName) {
        this.fieldName = name;
        this.fieldDisplayName = displayName;
    }

    public String getName() {
        return fieldName;
    }

    public void setName(String name) {
        this.fieldName = name;
    }

    public String getDisplayName() {
        return this.fieldDisplayName;
    }

    public void setDisplayName(String displayName) {
        this.fieldDisplayName = displayName;
    }

    public Object getAttributeValue(String attrName) {
        return map.get(attrName);
    }

    public void setAttributeValue(String attrName, Object val) {
        map.put(attrName, val);
    }

    public void setToolTip(String toolTip) {
        this.tTip = toolTip;
    }

    public String getToolTip() {
        return tTip;
    }

    public void setEditable(boolean editable) {
        this.edit = editable;
    }

    public boolean isEditable() {
        return edit;
    }

    public void setStatic(boolean staticFlag) {
        isStatic = staticFlag;
    }

    public boolean isStatic() {
        return isStatic;
    }

    public String getDefaultValue() {
        return defaultAcceptable;
    }

    public void setDefaultValue(String newValue) throws EDMException {
        if (acceptables.contains(newValue)) {
            defaultAcceptable = newValue;
        } else {
            throw new EDMException(NbBundle.getMessage(OperatorFieldNodeX.class, "ERROR_acceptables_list") + newValue);
        }
    }

    public List getAcceptableValues() {
        return (isStatic) ? Collections.unmodifiableList(acceptables) : Collections.EMPTY_LIST;
    }

    public void setAcceptableValues(List acceptableValues) {
        if (isStatic) {
            acceptables.clear();
            if (acceptableValues != null) {
                acceptables.addAll(acceptableValues);
            } else {
                // Clear display values as well.
                displayAcceptables.clear();
            }
        }
    }

    public List getAcceptableDisplayValues() {
        return (isStatic) ? displayAcceptables.isEmpty() ? getAcceptableValues() : Collections.unmodifiableList(displayAcceptables) : Collections.EMPTY_LIST;
    }


    public void setAcceptableDisplayValues(List displayValues) {
        if (isStatic) {
            displayAcceptables.clear();
            if (displayValues != null) {
                if (displayValues.isEmpty()) {
                    return;
                } else if (displayValues.size() != acceptables.size()) {
                    throw new IllegalArgumentException(NbBundle.getMessage(OperatorFieldNodeX.class, "ERROR_acceptable_values"));
                }

                displayAcceptables.addAll(displayValues);
            }
        }
    }

    public Object getFieldDataObject() {
        return fieldData;
    }

    public void setFieldDataObject(Object dObj) {
        this.fieldData = dObj;
    }
}