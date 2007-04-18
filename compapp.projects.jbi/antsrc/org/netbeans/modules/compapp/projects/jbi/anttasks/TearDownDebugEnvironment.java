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
package org.netbeans.modules.compapp.projects.jbi.anttasks;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.Task;
import org.netbeans.api.debugger.DebuggerManager;


/**
 * Ant task to tear down debug environment for CompApp project test case run.
 *
 * @author jqian
 */
public class TearDownDebugEnvironment extends Task {
    
    /**
     * DOCUMENT ME!
     *
     * @throws BuildException DOCUMENT ME!
     */
    public void execute() throws BuildException {
        System.out.println("TearDownDebugEnvironment");
        DebuggerManager.getDebuggerManager().finishAllSessions();
        
        // TODO: restore SE debug property
        // TODO: restore test case timeout property
    }    
}
