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
package org.netbeans.modules.sql.framework.model.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.netbeans.modules.sql.framework.model.ColumnRef;
import org.netbeans.modules.sql.framework.model.SQLConnectableObject;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.visitors.SQLVisitor;
import net.java.hulp.i18n.Logger;
import com.sun.sql.framework.exception.BaseException;
import org.netbeans.modules.etl.logger.Localizer;


/**
 * Base class implementation of SQLConnectableObject; inherits behavior from
 * AbstractSQLObject. Subclasses should override addInput and other methods as required for
 * specialization.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public abstract class SQLConnectableObjectImpl extends AbstractSQLObject implements SQLConnectableObject {

    /* Log4J category string */
    private static final String LOG_CATEGORY = SQLConnectableObjectImpl.class.getName();
    private static transient final Logger mLogger = Logger.getLogger(SQLConnectableObjectImpl.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    /** Map of SQLInputObjects */
    protected Map inputMap;

    /** Creates a new instance of AbstractSQLExpressionObject */
    public SQLConnectableObjectImpl() {
        super();
        inputMap = new LinkedHashMap(10);
    }

    /**
     * @throws com.sun.sql.framework.exception.BaseException 
     * @see SQLConnectableObject#addInput
     */
    public void addInput(String argName, SQLObject newInput) throws BaseException {
        if (argName == null || newInput == null) {
            throw new BaseException("Input arguments not specified");
        }

        int newType = newInput.getObjectType();
        String objType = TagParserUtility.getDisplayStringFor(newType);

        if (isInputCompatible(argName, newInput) == SQLConstants.TYPE_CHECK_INCOMPATIBLE) {
            throw new BaseException("Input type " + objType + " is incompatible with input argument '" + argName + "' of " + TagParserUtility.getDisplayStringFor(this.type) + " '" + this.getDisplayName() + "'.");
        }

        if (!isInputValid(argName, newInput)) {
            throw new BaseException("Cannot link " + objType + " '" + newInput.getDisplayName() + "' as input to '" + argName + "' in " + TagParserUtility.getDisplayStringFor(this.type) + " '" + this.getDisplayName() + "'");
        }

        SQLInputObject inputObject = (SQLInputObject) inputMap.get(argName);
        if (inputObject != null) {
            inputObject.setSQLObject(newInput);
        } else {
            throw new BaseException("Input with argName '" + argName + "' does not exist.");
        }
    }

    /**
     * Overrides default implementation to compute hashcode based on any associated
     * SQLInputObjects as well as values of non-transient member variables.
     * 
     * @param o Object to compare for equality
     * @return hashcode for this instance
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        }

        boolean response = super.equals(o);

        if (o instanceof SQLConnectableObjectImpl) {
            SQLConnectableObjectImpl impl = (SQLConnectableObjectImpl) o;

            // This convoluted logic is here because inputMap.equals(impl.inputMap)
            // apparently delegates to the Object.equals() method. Go figure.
            response &= (inputMap != null) && (impl.inputMap != null) ? (inputMap.entrySet().equals(impl.inputMap.entrySet())) : (inputMap == null) && (impl.inputMap == null);
        } else {
            response = false;
        }

        return response;
    }

    /**
     * Gets list of child SQLObjects
     * 
     * @return List of child SQLObject instances
     */
    public List getChildSQLObjects() {
        return new ArrayList();
    }

    /**
     * @see SQLConnectableObject#getInput
     */
    public SQLInputObject getInput(String argName) {
        return (SQLInputObject) inputMap.get(argName);
    }

    /**
     * @see SQLConnectableObject#getInputObjectMap
     */
    public Map getInputObjectMap() {
        return inputMap;
    }

    /**
     * Provides default implementation of interface method signature. Returns an empty
     * array list. Override this method in subclasses.
     * 
     * @return ArrayList of SQLTypeObjects
     * @throws com.sun.sql.framework.exception.BaseException 
     * @see SQLConnectableObject#getPossibleTypes
     */
    public ArrayList getPossibleTypes() throws BaseException {
        ArrayList list = new ArrayList();
        return (list);
    }

    public List getSourceColumnsUsed() {
        List list = new ArrayList();
        return getColumnsUsed(this, SQLConstants.SOURCE_COLUMN, list);
    }

    /**
     * @see SQLConnectableObject#getSQLObject
     */
    public SQLObject getSQLObject(String argName) {
        SQLInputObject input = getInput(argName);
        return (input != null) ? input.getSQLObject() : null;
    }

    /**
     * @see SQLConnectableObject#getSQLObjectMap
     */
    public Map getSQLObjectMap() {
        Map objMap = new LinkedHashMap(10);
        Iterator iter = inputMap.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            String argName = (String) entry.getKey();
            SQLInputObject obj = (SQLInputObject) entry.getValue();

            if (argName != null && obj != null && obj.getSQLObject() != null) {
                objMap.put(argName, obj.getSQLObject());
            }
        }

        return objMap;
    }

    public List getTargetColumnsUsed() {
        List list = new ArrayList();
        return getColumnsUsed(this, SQLConstants.TARGET_COLUMN, list);
    }

    /**
     * Overrides default implementation to compute hashcode based on any associated
     * attributes as well as values of non-transient member variables.
     * 
     * @return hashcode for this instance
     */
    public int hashCode() {
        int hashCode = super.hashCode();
        hashCode += (inputMap != null) ? inputMap.hashCode() : 0;

        return hashCode;
    }

    public boolean hasSourceColumn() {
        return hasColumnOfType(this, SQLConstants.SOURCE_COLUMN);
    }

    public boolean hasTargetColumn() {
        return hasColumnOfType(this, SQLConstants.TARGET_COLUMN);
    }

    /**
     * @see SQLConnectableObject
     */
    public int isInputCompatible(String argName, SQLObject input) {
        return SQLConstants.TYPE_CHECK_COMPATIBLE;
    }

    public boolean isInputStatic(String argName) {
        return false;
    }

    /**
     * @see SQLConnectableObject#isInputValid
     */
    public boolean isInputValid(String argName, SQLObject input) {
        return true;
    }

    /**
     * @param sqlObj 
     * @throws com.sun.sql.framework.exception.BaseException 
     * @see SQLConnectableObject#removeInputByArgName
     */
    public SQLObject removeInputByArgName(String argName, SQLObject sqlObj) throws BaseException {
        if (argName == null) {
            throw new BaseException("Cannot delete input for null argument");
        }

        SQLInputObject inputObject = (SQLInputObject) inputMap.get(argName);
        if (inputObject == null) {
            throw new BaseException("Cannot delete non-existent input for argument " + argName);
        }

        SQLObject victim = inputObject.getSQLObject();
        inputObject.setSQLObject(null);
        return victim;
    }

    public void reset() {
        super.reset();
        Iterator it = inputMap.keySet().iterator();
        while (it.hasNext()) {
            String name = (String) it.next();
            SQLInputObject inputObj = (SQLInputObject) inputMap.get(name);
            //inputObj.setSQLObject(null);
            inputObj.getSQLObject().reset();
        }
    }

    /**
     * Overrides parent implementation to append XML elements for any input objects
     * associated with this expression.
     * 
     * @param prefix String to append to each new line of the XML representation
     * @return XML representation of this SQLObject instance
     */
    public String toXMLString(String prefix) throws BaseException {
        StringBuilder buf = new StringBuilder();
        if (prefix == null) {
            prefix = "";
        }

        buf.append(prefix).append(getHeader());
        buf.append(super.toXMLAttributeTags(prefix));
        buf.append(TagParserUtility.toXMLInputTag(prefix + "\t", inputMap));
        buf.append(prefix).append(getFooter());

        return buf.toString();
    }

    public abstract void visit(SQLVisitor visitor);

    public void copyFromSource(SQLObject source) {
        super.copyFromSource(source);
        SQLConnectableObject expObj = (SQLConnectableObject) source;

        Map inputObjMap = expObj.getInputObjectMap();
        Iterator it = inputObjMap.keySet().iterator();

        while (it.hasNext()) {
            String name = (String) it.next();
            SQLInputObject inputObj = (SQLInputObject) inputObjMap.get(name);
            SQLObject obj = inputObj.getSQLObject();
            if (obj != null) {
                try {
                    SQLObject copiedObj = (SQLObject) obj.cloneSQLObject();
                    SQLInputObject inputObject = (SQLInputObject) inputMap.get(name);
                    if (inputObject != null) {
                        inputObject.setSQLObject(copiedObj);
                    }
                } catch (CloneNotSupportedException ex) {
                    mLogger.errorNoloc(mLoc.t("EDIT111: Failed to cloned input map{0}", LOG_CATEGORY), ex);
                }
            }
        }

    }

    protected List getColumnsUsed(SQLConnectableObject exp, int colType, List list) {
        if (exp != null) {
            Map inputObjMap = exp.getInputObjectMap();
            Iterator it = inputObjMap.keySet().iterator();

            while (it.hasNext()) {
                String name = (String) it.next();
                SQLInputObject inputObj = (SQLInputObject) inputObjMap.get(name);
                SQLObject obj = inputObj.getSQLObject();
                if (obj != null) {
                    try {
                        if (obj.getObjectType() == SQLConstants.COLUMN_REF) {
                            obj = ((ColumnRef) obj).getColumn();
                            if (obj.getObjectType() == colType) {
                                list.add(obj);
                            }
                        } else {
                            if (obj instanceof SQLConnectableObject) {
                                getColumnsUsed((SQLConnectableObject) obj, colType, list);
                            }
                        }

                    } catch (Exception ex) {
                        mLogger.errorNoloc(mLoc.t("EDIT112: Finding expression contains column refs{0}", LOG_CATEGORY), ex);
                    }
                }
            }
        }
        return list;
    }

    protected boolean hasColumnOfType(SQLConnectableObject exp, int colType) {
        boolean ret = false;

        if (exp != null) {
            Map inputObjMap = exp.getInputObjectMap();
            Iterator it = inputObjMap.keySet().iterator();

            while (it.hasNext()) {
                String name = (String) it.next();
                SQLInputObject inputObj = (SQLInputObject) inputObjMap.get(name);
                SQLObject obj = inputObj.getSQLObject();
                if (obj != null) {
                    try {
                        if (obj.getObjectType() == SQLConstants.COLUMN_REF) {
                            obj = ((ColumnRef) obj).getColumn();
                            if (obj.getObjectType() == colType) {
                                ret = true;
                                break;
                            }
                        } else {
                            if (obj instanceof SQLConnectableObject) {
                                if (hasColumnOfType((SQLConnectableObject) obj, colType)) {
                                    ret = true;
                                    break;
                                }
                            }
                        }

                    } catch (Exception ex) {
                        mLogger.errorNoloc(mLoc.t("EDIT112: Finding expression contains column refs{0}", LOG_CATEGORY), ex);
                    }
                }
            }
        }
        return ret;
    }
}

