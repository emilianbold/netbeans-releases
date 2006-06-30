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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.debugger.jpda.ui.breakpoints;

import javax.swing.JComponent;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.spi.debugger.ui.BreakpointType;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.modules.debugger.jpda.ui.EditorContextBridge;

import org.openide.util.NbBundle;


/**
* Implementation of breakpoint on variable accecc / modification.
*
* @author   Jan Jancura
*/
public class FieldBreakpointType extends BreakpointType {

    public String getCategoryDisplayName () {
        return NbBundle.getMessage (
            FieldBreakpointType.class,
            "CTL_Java_breakpoint_events_cathegory_name"
        );
    }
    
    public JComponent getCustomizer () {
        return new FieldBreakpointPanel ();
    }
    
    public String getTypeDisplayName () {
        return NbBundle.getMessage (
            FieldBreakpointType.class, 
            "CTL_Variable_event_type_name"
        );
    }
    
    public boolean isDefault () {
        return EditorContextBridge.getDefaultType () == EditorContextBridge.FIELD;
    }
}
