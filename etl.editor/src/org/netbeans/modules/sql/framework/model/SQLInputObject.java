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

/**
 * UI wrapper class for SQLObjects which serve as inputs to SQLConnectableObjects.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public interface SQLInputObject {

    public static String ATTR_ARGNAME = "argName";

    public static String ATTR_DISPLAY_NAME = "displayName";

    public static String TAG_INPUT = "input";

    /**
     * Gets argument name associated with this input.
     * 
     * @return argument name
     */
    public String getArgName();

    /**
     * Gets display name of this input.
     * 
     * @return current display name
     */
    public String getDisplayName();

    /**
     * Gets reference to SQLObject holding value of this input
     * 
     * @return input object
     */
    public SQLObject getSQLObject();

    /**
     * Sets display name of this input.
     * 
     * @param newName new display name
     */
    public void setDisplayName(String newName);

    /**
     * Sets reference to SQLObject holding value of this input
     * 
     * @param newInput reference to new input object
     */
    public void setSQLObject(SQLObject newInput);

    /**
     * Writes contents of this instance to an XML element.
     * 
     * @param prefix String to prepend to the start of each new line
     * @return XML element representing the contents of this instance
     */
    public String toXMLString(String prefix);
}

