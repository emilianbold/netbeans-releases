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
package org.netbeans.modules.edm.model.impl;

import org.netbeans.modules.edm.model.DBColumn;
import org.netbeans.modules.edm.editor.utils.TagParserUtility;
import org.netbeans.modules.edm.model.SQLCanvasObject;
import org.netbeans.modules.edm.model.SQLInputObject;
import org.netbeans.modules.edm.model.SQLObject;

import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.editor.utils.StringUtil;
import org.openide.util.NbBundle;

/**
 * UI wrapper class for SQLObjects which serve as inputs to SQLConnectableObjects.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class SQLInputObjectImpl implements SQLInputObject {

    /* Argument name */
    private String argName;

    /* Display name */
    private String dispName;

    /* SQLObject representing input value */
    private SQLObject input;

    /**
     * Creates a new instance of SQLInputObject with the given argument name and input
     * object.
     * 
     * @param argumentName argument name to associate with the given SQLObject
     * @param displayName display name for this instance
     * @param inputObject SQLObject providing input value for the given argument name
     */
    public SQLInputObjectImpl(String argumentName, String displayName, SQLObject inputObject) {
        if (StringUtil.isNullString(argumentName)) {
            throw new IllegalArgumentException(NbBundle.getMessage(SQLInputObjectImpl.class, "ERROR_null_argumentName"));
        }

        argName = argumentName;
        dispName = displayName;
        input = inputObject;
    }

    /**
     * Overrides default implementation to compute hashcode based on any associated
     * SQLInputObjects as well as values of non-transient member variables.
     * 
     * @param o Object to compare against this
     * @return hashcode for this instance
     */
    public boolean equals(Object o) {
        if (o == null) {
            return false;
        } else if (o == this) {
            return true;
        } else if (!(o instanceof SQLInputObjectImpl)) {
            return false;
        }

        SQLInputObjectImpl impl = (SQLInputObjectImpl) o;
        boolean response = (argName != null) ? argName.equals(impl.argName) : (impl.argName == null);
        response &= (input != null) ? (input.equals(impl.input)) : (impl.input == null);

        return response;
    }

    /**
     * Gets argument name associated with this input.
     * 
     * @return argument name
     */
    public String getArgName() {
        return argName;
    }

    /**
     * Gets display name of this input.
     * 
     * @return current display name
     */
    public String getDisplayName() {
        return (dispName != null) ? dispName : argName;
    }

    /**
     * Gets reference to SQLObject holding value of this input
     * 
     * @return input object
     */
    public SQLObject getSQLObject() {
        return input;
    }

    /**
     * Overrides default implementation to compute hashcode based on any associated
     * attributes as well as values of non-transient member variables.
     * 
     * @return hashcode for this instance
     */
    public int hashCode() {
        int hashCode = (argName != null) ? argName.hashCode() : 0;
        hashCode += (input != null) ? input.hashCode() : 0;

        return hashCode;
    }

    /**
     * Sets display name of this input.
     * 
     * @param newName new display name
     */
    public void setDisplayName(String newName) {
        dispName = newName;
    }

    /**
     * Sets reference to SQLObject holding value of this input
     * 
     * @param newInput reference to new input object
     */
    public void setSQLObject(SQLObject newInput) {
        input = newInput;
    }

    /**
     * @see SQLInputObject
     */
    public String toXMLString(String prefix) {
        StringBuilder buf = new StringBuilder();

        if (prefix == null) {
            prefix = "";
        }

        buf.append(prefix).append("<" + TAG_INPUT + " ");
        buf.append(ATTR_ARGNAME + "=\"").append(argName).append("\" ");
        buf.append(ATTR_DISPLAY_NAME + "=\"").append(getDisplayName()).append("\">\n");

        try {
            // TODO: make Source and target columns as canvas objects
            // if input is a canvas object then it is refered object
            if (input instanceof SQLCanvasObject || input instanceof DBColumn) {
                buf.append(TagParserUtility.toXMLObjectRefTag(input, prefix + "\t"));
            } else {
                // if input is not canvas object then it is part of object
                buf.append(input.toXMLString(prefix + "\t"));
            }
        } catch (EDMException e) {
            // @TODO log this exception
        }

        buf.append(prefix).append("</" + TAG_INPUT + ">\n");

        return buf.toString();
    }
}

