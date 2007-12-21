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

package org.netbeans.modules.bpel.debugger.ui.breakpoint;

import org.netbeans.modules.bpel.debugger.api.breakpoints.BpelFaultBreakpoint;
import org.netbeans.modules.bpel.debugger.ui.util.ModelUtil;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.modules.bpel.debugger.api.breakpoints.BpelBreakpoint;
import org.netbeans.modules.bpel.debugger.api.breakpoints.LineBreakpoint;
import org.netbeans.modules.bpel.debugger.ui.util.EditorUtil;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 * @author Alexander Zgursky
 */
public class BpelFaultBreakpointView extends BpelBreakpointView {

    protected String getName(BpelBreakpoint breakpoint) throws UnknownTypeException {
        if ( !(breakpoint instanceof BpelFaultBreakpoint)) {
            throw new UnknownTypeException(breakpoint);
        }
        
        BpelFaultBreakpoint faultBp = (BpelFaultBreakpoint)breakpoint;
        if (faultBp.getProcessQName() == null) {
            return "(broken)";
        }
        String processName = faultBp.getProcessQName().getLocalPart();
        String faultName = "";
        if (faultBp.getFaultQName() != null) {
            faultName = faultBp.getFaultQName().getLocalPart();
            return NbBundle.getMessage(
                BpelFaultBreakpointView.class,
                "CTL_Fault_Breakpoint_Name", faultName, processName); // NOI18N
        } else {
            return NbBundle.getMessage(
                BpelFaultBreakpointView.class,
                "CTL_Fault_Breakpoint_All_Faults_Name", processName); // NOI18N
        }

   }

    public String getIconBase(Object object) throws UnknownTypeException {
        if ( !(object instanceof BpelFaultBreakpoint)) {
            throw new UnknownTypeException(object);
        }
        
        return BREAKPOINT;
    }
}
