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

package org.netbeans.modules.sql.framework.model;

import java.util.Collection;
import java.util.List;

import org.netbeans.modules.sql.framework.model.visitors.SQLVisitedObject;

import com.sun.etl.exception.BaseException;
import com.sun.etl.utils.Attribute;

/**
 * This defines condition container for sql, which are defined at source table target
 * table
 * 
 * @author Ritesh Adval
 */
public interface SQLCondition extends SQLContainerObject, SQLVisitedObject {

    public static final String ATTR_CONTAINS_JAVA_OPERATORS = "containsJavaOperators";

    /** XML attribute name for display name */
    public static final String DISPLAY_NAME = "displayName";

    /**
     * GUI Mode Type II : Graphical
     */
    public static final int GUIMODE_GRAPHICAL = 2;

    /**
     * GUI Mode Type I : Text Mode
     */
    public static final int GUIMODE_SQLCODE = 1;

    /**
     * XML tag name for SQL Condition
     */
    public static final String TAG_CONDITION = "sqlCondition";

    public static final String TAG_SQLCODE = "sqlcode";

    /**
     * Adds equality predicate to the condition. If a different predicate already exists
     * then it is linked with "AND" predicator. Ex: 1) col = input 2) (T1.EMP_ID =
     * S1.EMP_ID) AND (T1.LAST_NAME = S2.LAST_NAME)
     * 
     * @param newInput 
     * @param col
     * @throws com.sun.etl.exception.BaseException 
     */
    public void addEqualityPredicate(SQLObject newInput, SQLDBColumn col) throws BaseException;

    /**
     * Gets an Attribute based on its name
     * 
     * @param attrName attribute Name
     * @return Attribute instance associated with attrName, or null if none exists
     */
    public Attribute getAttribute(String attrName);

    /**
     * Gets Collection of active attribute names.
     * 
     * @return Collection of attribute names
     */
    public Collection getAttributeNames();

    /**
     * Gets the object referenced by a named Attribute, if it exists.
     * 
     * @param attrName attribute Name
     * @return Object referenced by Attributed with name attrName, or null if none exists
     */
    public Object getAttributeValue(String attrName);

    /**
     * get the condition sql text
     * 
     * @return condition sql text
     */
    public String getConditionText();

    /**
     * Get the condition sql text. Tries to construct Sql text from graphic model if the
     * sql text is null or empty.
     * 
     * @param constructIfEmpty 
     * @return condition sql text
     */
    public String getConditionText(boolean constructIfEmpty);

    /**
     * Gets display name.
     * 
     * @return current display name
     */
    public String getDisplayName();

    public GUIInfo getGUIInfo();

    /**
     * get the gui mode
     * 
     * @return gui mode
     */
    public int getGuiMode();

    public Object getParentObject();

    /**
     * Returns list of Source or Target table columns used in the Condition expression.
     * 
     * @return
     */
    public List getParticipatingColumns();

    public SQLPredicate getRootPredicate();

    /**
     * check if there is some condition is defined, it may be invalid condition
     * 
     * @return true if some form of condition exist
     */
    public boolean isConditionDefined();

    /**
     * Check if a java operator is used in the model.
     * 
     * @return true if a java operator is used.
     */
    public boolean isContainsJavaOperators();

    /**
     * check if the object already exist
     * 
     * @param obj 
     * @return true if object already exist
     */
    public SQLObject isObjectExist(SQLObject obj);

    /**
     * is this condition a valid condition
     * 
     * @return true if condition is valid
     */
    public boolean isValid();

    /**
     * when a table is removed whose column are refered in this condition rhen we need to
     * remove the column references
     * 
     * @param column AbstractDBColumn
     * @throws com.sun.etl.exception.BaseException 
     */
    public void removeDanglingColumnRef(SQLObject column) throws BaseException;

    /**
     * Removes equality operator "col = value" from the condition and any reference to it
     * using "AND" operator.
     * 
     * @param col
     * @param victim 
     * @throws BaseException
     */
    public void removeEqualsPredicate(SQLDBColumn col, SQLObject victim) throws BaseException;

    /**
     * Removes the "targetColumn IS NULL" predicate from the target condition. Used when
     * target table is outer joined with source/table view and SQL being generated for
     * ANSI satndard FROM clause.
     * 
     * @throws BaseException
     */
    public void replaceTargetColumnIsNullPredicate() throws BaseException;

    /**
     * Sets an attribute name-value pair. The name of the Attribute should be one of the
     * String constants defined in this class.
     * 
     * @param attrName attribute Name
     * @param val value of the attribute
     */
    public void setAttribute(String attrName, Object val);

    /**
     * set the condition text
     * 
     * @param text condition text
     */
    public void setConditionText(String text);

    /**
     * set it to true if a java operator is used in the model
     * 
     * @param javaOp true if there is a java operator
     */
    public void setContainsJavaOperators(boolean javaOp);

    /**
     * Sets display name to given value.
     * 
     * @param newName new display name
     */
    public void setDisplayName(String newName);

    /**
     * set the gui mode
     * 
     * @param mode gui mode
     */
    public void setGuiMode(int mode);

    /**
     * Validate SQL Condition and retuns list of Invalid Input Object
     * 
     * @return List
     */
    public List validate();
}

