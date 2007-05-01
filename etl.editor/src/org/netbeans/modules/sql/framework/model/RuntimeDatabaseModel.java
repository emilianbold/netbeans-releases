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
 * This extends database model (may change) and provides two object input and output
 * 
 * @author Ritesh Adval
 */
public interface RuntimeDatabaseModel extends SQLDBModel {

    /** Constant for DatabaseModel metadata name tag. */
    public static final String RUNTIME_MODEL_TAG = "runtimeModel";

    /** String constant indicating runtime type */
    public static final String STRTYPE_RUNTIME = "runtime";

    /**
     * get runtime input object
     * 
     * @return runtime input
     */
    public RuntimeInput getRuntimeInput();

    /**
     * get runtime output object
     * 
     * @return runtime output
     */
    public RuntimeOutput getRuntimeOutput();

}

