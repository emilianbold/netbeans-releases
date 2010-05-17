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

package org.netbeans.modules.sql.framework.ui.graph.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.netbeans.modules.sql.framework.ui.graph.IOperatorField;

import com.sun.etl.exception.BaseException;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public class OperatorFieldNode implements IOperatorField {

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

    /** Creates a new instance of OperatorFieldNode */
    public OperatorFieldNode(String name, String displayName) {
        this.fieldName = name;
        this.fieldDisplayName = displayName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.sql.framework.ui.graph.IOperatorField#getName()
     */
    public String getName() {
        return fieldName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.sql.framework.ui.graph.IOperatorField#setName(java.lang.String)
     */
    public void setName(String name) {
        this.fieldName = name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.sql.framework.ui.graph.IOperatorField#getDisplayName()
     */
    public String getDisplayName() {
        return this.fieldDisplayName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.sql.framework.ui.graph.IOperatorField#setDisplayName(java.lang.String)
     */
    public void setDisplayName(String displayName) {
        this.fieldDisplayName = displayName;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.sql.framework.ui.graph.IOperatorField#getAttributeValue(java.lang.String)
     */
    public Object getAttributeValue(String attrName) {
        return map.get(attrName);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.sql.framework.ui.graph.IOperatorField#setAttributeValue(java.lang.String,
     *      java.lang.Object)
     */
    public void setAttributeValue(String attrName, Object val) {
        map.put(attrName, val);
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.sql.framework.ui.graph.IOperatorField#setToolTip(java.lang.String)
     */
    public void setToolTip(String toolTip) {
        this.tTip = toolTip;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.sql.framework.ui.graph.IOperatorField#getToolTip()
     */
    public String getToolTip() {
        return tTip;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.sql.framework.ui.graph.IOperatorField#setEditable(boolean)
     */
    public void setEditable(boolean editable) {
        this.edit = editable;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.sql.framework.ui.graph.IOperatorField#isEditable()
     */
    public boolean isEditable() {
        return edit;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.sql.framework.ui.graph.IOperatorField#setStatic(boolean)
     */
    public void setStatic(boolean staticFlag) {
        isStatic = staticFlag;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.sql.framework.ui.graph.IOperatorField#isStatic()
     */
    public boolean isStatic() {
        return isStatic;
    }

    public String getDefaultValue() {
        return defaultAcceptable;
    }

    public void setDefaultValue(String newValue) throws BaseException {
        if (acceptables.contains(newValue)) {
            defaultAcceptable = newValue;
        } else {
            throw new BaseException("Given value does not exist in acceptables list: " + newValue);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.sql.framework.ui.graph.IOperatorField#getAcceptableValues()
     */
    public List getAcceptableValues() {
        return (isStatic) ? Collections.unmodifiableList(acceptables) : Collections.EMPTY_LIST;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.sql.framework.ui.graph.IOperatorField#setAcceptableValues(java.util.List)
     */
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

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.sql.framework.ui.graph.IOperatorField#getAcceptableDisplayValues()
     */
    public List getAcceptableDisplayValues() {
        return (isStatic) ? displayAcceptables.isEmpty() ? getAcceptableValues() : Collections.unmodifiableList(displayAcceptables) : Collections.EMPTY_LIST;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.netbeans.modules.sql.framework.ui.graph.IOperatorField#setAcceptableDisplayValues(java.util.List)
     */
    public void setAcceptableDisplayValues(List displayValues) {
        if (isStatic) {
            displayAcceptables.clear();
            if (displayValues != null) {
                if (displayValues.isEmpty()) {
                    return;
                } else if (displayValues.size() != acceptables.size()) {
                    throw new IllegalArgumentException("Count of display values must match acceptable values.");
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
