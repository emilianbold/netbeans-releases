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

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import java.util.List;
import javax.swing.JComponent;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.debugger.gdb.EditorContextBridge;
import org.netbeans.spi.debugger.ui.BreakpointType;

import org.openide.util.NbBundle;


/**
 * Implementation of breakpoint on function.
 *
 * @author   Nik Molchanov (copied from Jan Jancura's JPDA implementation). 
 */
public class FunctionBreakpointType extends BreakpointType {

    public String getCategoryDisplayName() {
        return NbBundle.getMessage(FunctionBreakpointType.class,
                    "CTL_Gdb_breakpoint_events_category_name"); // NOI18N
    }
    
    public JComponent getCustomizer() {
        return new FunctionBreakpointPanel ();
    }
    
    public String getTypeDisplayName() {
        return NbBundle.getMessage(FunctionBreakpointType.class, "CTL_Gdb_Function_Breakpoint"); // NOI18N
    }
    
    public boolean isDefault() {
        // First, check for an open file. Is it one of ours?
	String mime = EditorContextBridge.getContext().getMostRecentMIMEType();
        if (mime.length() > 0 &&
                mime.equals("text/x-c++") || mime.equals("text/x-c") || // NOI18N
                mime.equals("text/x-fortran")) { // NOI18N
            return true;
        }
        
        // Next, check the main project. Is it one of ours?
        Project project = OpenProjects.getDefault().getMainProject();
        if (project != null) {
            NativeProject np = (NativeProject) project.getLookup().lookup(NativeProject.class);
            if (np != null) {
                return true;
            }
        }
        
        // Last, count breakpoint types. We define 2. If thats all that are returned, then
        // we're the only active debugger and should be the default.
        List breakpointTypes = DebuggerManager.getDebuggerManager().lookup(null, BreakpointType.class);
        if (breakpointTypes.size() == 2) {
            return true;
        }
        	
	return false;
    }
}

