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

import java.util.Map;
import com.sun.etl.engine.ETLEngine;
import com.sun.etl.exception.BaseException;

/**
 * @author Jonathan Giron
 */
public interface ETLProcessFlowGenerator {

    public void applyConnectionDefinitions() throws BaseException;

    public void applyConnectionDefinitions(boolean isMemoryDb) throws BaseException;
    
    public void applyConnectionDefinitions(boolean useInstancedb, boolean isMemoryDb) throws BaseException;

    public void applyConnectionDefinitions(Map name2connectionDefMap, Map connDefMap, Map intDbConfigParams) throws BaseException;

    public String getInstanceDBFolder();

    public void setInstanceDBFolder(String instanceDBFolder);

    public String getInstanceDBName();

    public void setInstanceDBName(String instanceDBName);

    public String getMonitorDBFolder();

    public void setMonitorDBFolder(String monitorDBFolder);

    public String getMonitorDBName();

    public void setMonitorDBName(String monitorDBName);

    public String getWorkingFolder();

    public void setWorkingFolder(String workingFolder);

    public ETLEngine getScript() throws BaseException;
}