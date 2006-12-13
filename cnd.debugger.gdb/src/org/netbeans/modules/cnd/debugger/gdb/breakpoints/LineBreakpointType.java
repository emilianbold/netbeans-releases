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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.debugger.gdb.breakpoints;

import javax.swing.JComponent;
import org.netbeans.spi.debugger.ui.BreakpointType;
import org.openide.util.NbBundle;

/**
 * Implementation of a line breakpoint.
 *
 * @author   Gordon Prieur (copied from Jan Jancura's JPDA implementation)
 */
public class LineBreakpointType extends BreakpointType {

    public String getCategoryDisplayName() {
        return NbBundle.getMessage(LineBreakpointType.class,
                    "CTL_Gdb_breakpoint_events_category_name"); // NOI18N
    }
    
    public JComponent getCustomizer() {
        return new LineBreakpointPanel ();
    }
    
    public String getTypeDisplayName() {
        return NbBundle.getMessage(LineBreakpointType.class, "CTL_Gdb_Line_Breakpoint"); // NOI18N
    }
    
    /**
     *  Tell debuggercore if this should be the default breakpoint.
     *
     *  Currently we always return false because we want to defer to FunctionBreakpointType.
     *  Eventually, this class and FunctionBreakpointType should both become smart enough
     *  that FBT is the default if the cursor is inside a function and LBT if its outside
     *  of a function (in both cases, its false if the current file in the editor isn't a
     *  C, C++, or Fortran file)
     */
    public boolean isDefault() {	
	return false;	// do false for now because FunctionBreakpointType currently
			// overrides this anyway.
    }
}

