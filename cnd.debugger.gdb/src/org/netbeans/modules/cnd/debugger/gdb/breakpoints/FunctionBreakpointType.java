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
import org.netbeans.modules.cnd.debugger.gdb.EditorContextBridge;
import org.netbeans.spi.debugger.ui.BreakpointType;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;

import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;


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
	String mime = EditorContextBridge.getContext().getMostRecentMIMEType();
	return mime.equals("text/x-c++") || mime.equals("text/x-c") || mime.equals("text/x-fortran");
    }
}

