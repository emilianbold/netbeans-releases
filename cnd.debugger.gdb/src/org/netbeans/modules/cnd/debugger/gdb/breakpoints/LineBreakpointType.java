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

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.spi.debugger.ui.BreakpointType;

import org.netbeans.modules.cnd.debugger.gdb.EditorContextBridge;

import org.openide.util.NbBundle;


/**
* Implementation of breakpoint on method.
*
* @author   Jan Jancura
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
        return NbBundle.getMessage(LineBreakpointType.class, "CTL_Gdb_event_type_name"); // NOI18N
    }
    
    public boolean isDefault () {
        return true; //EditorContextBridge.getDefaultType () == EditorContextBridge.LINE;
    }
}

