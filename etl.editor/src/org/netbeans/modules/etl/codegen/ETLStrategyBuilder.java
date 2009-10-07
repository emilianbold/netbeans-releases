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
package org.netbeans.modules.etl.codegen;

import com.sun.sql.framework.exception.BaseException;

/**
 * This allows us to plug-in different strategy as appropriate to optimize process flow;
 * that will best fit for a given scenario.
 *
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public interface ETLStrategyBuilder {

    public static final int EXEC_MODE_SIMPLE = 1; // All source and target in same Database
    public static final int EXEC_MODE_STAGING = 2; // Sources are copied to temp table on target DB
    public static final int EXEC_MODE_ONE_PASS = 3; // All src are in same DB but different than Tgt
    public static final int EXEC_MODE_PIPELINE = 4; // All src are in same DB but different than Tgt

    public void setETLScriptBuilderModel(ETLScriptBuilderModel model);

    public void generateScriptForTable(ETLStrategyBuilderContext context) throws BaseException;

    public String getScriptToDisplay(ETLStrategyBuilderContext context) throws BaseException;
}
