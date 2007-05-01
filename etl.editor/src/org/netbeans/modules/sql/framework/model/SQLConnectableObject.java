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
package org.netbeans.modules.sql.framework.model;

import java.util.List;
import java.util.Map;

import org.netbeans.modules.sql.framework.model.visitors.SQLVisitedObject;

import com.sun.sql.framework.exception.BaseException;

/**
 * Interface for all objects in the UI Object Model which accept inputs in the form of
 * SQLObject implementation instances.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public interface SQLConnectableObject extends SQLObject, SQLVisitedObject {

    /**
     * Adds the given SQLInputObject as an input.
     * 
     * @param argName name of argument whose associated SQLObject input is requested
     * @param newInput SQLInputObject serving as input
     * @throws BaseException if newInput cannot be added to this instance
     */
    public void addInput(String argName, SQLObject newInput) throws BaseException;

    /**
     * Gets SQLInputObject instance, if any, associated as an input with the given
     * argument name.
     * 
     * @param argName name of argument whose associated SQLInputObject input is requested
     * @return SQLInputObject associated with argName, or null if no such instance exists
     */
    public SQLInputObject getInput(String argName);

    /**
     * Gets a Map of argument names to corresponding SQLInputObject instances.
     * 
     * @return Map of arguments to SQLInputObject inputs.
     */
    public Map getInputObjectMap();

    /**
     * Returns List of Source table columns used in the expression.
     * 
     * @return List of Source table columns used in the expression.
     */
    public List getSourceColumnsUsed();

    /**
     * Gets SQLObject instance, if any, referenced as an input with the given argument
     * name.
     * 
     * @param argName name of argument whose associated SQLInputObject input is requested
     * @return SQLObject associated with argName, or null if no such instance exists
     */
    public SQLObject getSQLObject(String argName);

    /**
     * Gets Map of argument names to SQLObject instances, if any, referenced as inputs.
     * 
     * @return List of SQLObject instances referenced as inputs for this instance; empty
     *         if no SQLObjects are currently referenced.
     */
    public Map getSQLObjectMap();

    /**
     * Returns List of Target table columns used in the expression.
     * 
     * @return List of Target table columns used in the expression.
     */
    public List getTargetColumnsUsed();

    /**
     * @return true if expression contains source column.
     */
    public boolean hasSourceColumn();

    /**
     * @return true if expression contains target column.
     */
    public boolean hasTargetColumn();

    /**
     * Indicates whether the given object is compatible as an input for this instance.
     * 
     * @param argName name of argument field whose type will be checked against input for
     *        compatibility
     * @param input SQLObject to test for compatibility
     * @return SQLConstants.TYPE_CHECK_SAME if input and argument field are of identical
     *         type, SQLConstants.TYPE_CHECK_COMPATIBLE if input can be can be cast to
     *         type that is compatible with the argument field,
     *         SQLConstants.TYPE_CHECK_INCOMPATIBLE if input and argument field are
     *         incompatible
     */
    public int isInputCompatible(String argName, SQLObject input);

    /**
     * Indicates whether the given object is a static input for this instance.
     * 
     * @param argName name of argument whose type will be checked against input for
     *        validity
     * @return true if input is static, false otherwise
     */
    public boolean isInputStatic(String argName);

    /**
     * Indicates whether the given object is a valid input for this instance.
     * 
     * @param argName name of argument whose type will be checked against input for
     *        validity
     * @param input SQLObject to test for validity
     * @return true if input is valid, false otherwise
     */
    public boolean isInputValid(String argName, SQLObject input);

    /**
     * Removes the SQLObject, if any, associated with the given argument name.
     * 
     * @param argName name of argument whose associated SQLObject input, if any, should be
     *        removed
     * @return SQLObject formerly associated with argName, or null if no SQLObject was
     *         associated with argName.
     * @throws BaseException if error occurs during removal
     */
    public SQLObject removeInputByArgName(String argName, SQLObject sqlObj) throws BaseException;
}
