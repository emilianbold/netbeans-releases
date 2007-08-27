/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.modelimpl.trace;

import java.io.File;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmModel;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class TestModelHelper {
    private TraceModel traceModel;
    
    /**
     * Creates a new instance of TestModelHelper
     */
    public TestModelHelper() {
        traceModel = new TraceModel();
    }
    
    /*package-local*/ TraceModel getTraceModel() {
        return traceModel;
    }
    
    public void initParsedProject(String projectRoot, 
            List<String> sysIncludes, List<String> usrIncludes) throws Exception {
        traceModel.setIncludePaths(sysIncludes, usrIncludes);
        traceModel.test(new File(projectRoot), System.out, System.err);
    } 
    
    public void initParsedProject(String projectRoot) throws Exception {
        traceModel.test(new File(projectRoot), System.out, System.err);
    }     
    
    public ProjectBase getProject(){
        return traceModel.getProject();
    }

    public CsmModel getModel(){
        return traceModel.getModel();
    }
    
    public void shutdown() {
        traceModel.shutdown();
    }
}
