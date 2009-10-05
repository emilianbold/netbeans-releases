/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.sql.framework.ui.graph;

import java.util.List;

import com.sun.sql.framework.exception.BaseException;

/**
 * @author Ritesh Adval
 * @version $Revision$
 */
public interface IOperatorField {

    /**
     * Gets the name of the field.
     */
    public String getName();

    /**
     * Sets the name of the field.
     * 
     * @param name
     */
    public void setName(String name);

    /**
     * Gets display name of the field.
     * 
     * @return display name
     */
    public String getDisplayName();

    /**
     * Sets display name of the field.
     * 
     * @param displayName
     */
    public void setDisplayName(String displayName);

    /**
     * Gets the value of the attribute.
     * 
     * @param attrName name of the attribute
     * @return value of the attribute
     */
    public Object getAttributeValue(String attrName);

    /**
     * Sets an attributes value.
     * 
     * @param attrName attribute name
     * @param val attribute value
     */
    public void setAttributeValue(String attrName, Object val);

    /**
     * Sets the tool tip.
     * 
     * @param toolTip toolTip
     */
    public void setToolTip(String toolTip);

    /**
     * Gets the tool tip.
     * 
     * @return toolTip
     */
    public String getToolTip();

    /**
     * Sets whether this is editable.
     * 
     * @param editable if field is editable
     */
    public void setEditable(boolean editable);

    /**
     * Indicates whether field is editable.
     * 
     * @return true if this field is editable, false if field can only have columns or
     *         expressions linked to it
     */
    public boolean isEditable();

    /**
     * set the field data object
     */
    public void setFieldDataObject(Object dObj);

    /**
     * get the field data object
     * 
     * @return
     */
    public Object getFieldDataObject();

    /**
     * Sets whether this field is static, i.e., it is not linkable and accepts only from a
     * finite range or set of values.
     * 
     * @param editable if field is editable
     */
    public void setStatic(boolean staticFlag);

    /**
     * Indicates whether field is static (not linkable and accepting only from a finite
     * range or set of values).
     * 
     * @return true if this field is static, false otherwise
     */
    public boolean isStatic();

    /**
     * If this field is static, gets List representing acceptable String values, or
     * Collections.EMPTY_LIST if it is not.
     * 
     * @return List of acceptable values, possibly Collections.EMPTY_LIST if any value is
     *         valid, or if this field is not editable
     */
    public List getAcceptableValues();

    /**
     * Sets List representing acceptable String values for this field. If acceptableValues
     * is null, both getAcceptableValues() and getAcceptableDisplayValues() will return
     * Collections.EMPTY_LIST.
     * 
     * @param acceptableValues List of new acceptable String values.
     */
    public void setAcceptableValues(List acceptableValues);

    /**
     * If this field is static, gets List representing human-readable versions of
     * acceptable String values, in the same order as the elements returned by
     * getAcceptableValues().
     * 
     * @return List of acceptables display values; may be identical to
     *         getAcceptableValues() if no values were set via
     *         setAcceptableDisplayValues();
     */
    public List getAcceptableDisplayValues();

    /**
     * Sets List representing human-readable versions of acceptable String values for this
     * field, in the same order as elements returned by getAcceptableValues(). It is the
     * caller's responsibility to ensure that display values are ordered in the same
     * sequence as their corresponding acceptable values. If <code>displayValues</code>
     * is null or empty, getAcceptableDisplayValues() will return
     * 
     * @param acceptableValues List of new acceptable String values. May be null or empty
     *        to indicate that acceptable values are also displayable, otherwise its size
     *        must match the current size of the list of acceptable values.
     */
    public void setAcceptableDisplayValues(List displayValues);

    /**
     * Gets default acceptable value for this field. This method only yields meaningful
     * values if this operator field is a static parameter, i.e., isStatic() returns true.
     * 
     * @return default acceptable value, if any, for this field.
     */
    public String getDefaultValue();

    /**
     * Sets default acceptable value for this field. This method is useful only if this
     * operator operator field is a static parameter, i.e., isStatic() returns true.
     * 
     * @param newDefault new default value
     * @throws BaseException if given default is not one of the acceptable values given by
     *         {@link getAcceptableValues()};
     */
    public void setDefaultValue(String newDefault) throws BaseException;
}
