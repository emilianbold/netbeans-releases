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
package org.netbeans.modules.sql.framework.ui.graph;

/**
 * @author Wei
 * @version 1.0
 */

public interface ICommand {
    /** Show SQL command */
    public static final String SHOW_SQL_CMD = "showSQL";

    /** Show Data command */
    public static final String SHOW_DATA_CMD = "showData";

    /** Show Rejection Data command */
    public static final String SHOW_REJECTION_DATA_CMD = "showRejectionData";

    /** Show Property command */
    public static final String SHOW_PROPERTY_CMD = "showProperty";

    /** Show Property command */
    public static final String CONFIG_CMD = "config";

    /** Add Runtime command */
    public static final String ADD_RUNTIME_CMD = "addRuntime";

    /** Delete Node command */
    public static final String DELETE_NODE_CMD = "deleteNode";

    public static final String EDIT_JOINVIEW = "editJoinView";

    public static final String SHOW_TARGET_SQL = "showTargetSQL";

    public static final String DATA_VALIDATION = "dataValidation";

    public static final String DATA_EXTRACTION = "dataExtraction";

    public static final String SHOW_TARGET_JOIN_CONDITION_CMD = "tagetJoinCondition" ;
    
    public static final String SHOW_TARGET_FILTER_CONDITION_CMD = "tagetFilterCondition" ;
}
