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

import org.netbeans.modules.bpel.debugger.ui.util.ModelUtil;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.netbeans.modules.bpel.debugger.api.breakpoints.BpelBreakpoint;
import org.netbeans.modules.bpel.debugger.api.breakpoints.LineBreakpoint;
import org.netbeans.modules.bpel.debugger.ui.util.EditorUtil;
import org.openide.loaders.DataObject;

/**
 * @author Vladimir Yaroslavskiy
 * @author Alexander Zgursky
 */
public class BpelLineBreakpointView extends BpelBreakpointView {
    
    protected String getName(BpelBreakpoint breakpoint) throws UnknownTypeException {
        if ( !(breakpoint instanceof LineBreakpoint)) {
            throw new UnknownTypeException(breakpoint);
        }
        LineBreakpoint lbp = (LineBreakpoint) breakpoint;
        DataObject dataObject = EditorUtil.getDataObject(lbp.getURL());
        String strLine = "(broken)";
        if (dataObject != null) {
            BpelModel model = EditorUtil.getBpelModel(dataObject);
            if (model != null) {
                UniqueId bpelEntityId = ModelUtil.getBpelEntityId(model, lbp.getXpath());
                if (bpelEntityId != null) {
                    int lineNumber = ModelUtil.getLineNumber(bpelEntityId);
                    if (lineNumber > 0) {
                        strLine = "" + lineNumber;
                    }
                }
            }
        }
        return EditorUtil.getFileName(lbp.getURL()) +
                ": " + strLine; // NOI18N
    }
    
    public String getIconBase(Object object) throws UnknownTypeException {
        if ( !(object instanceof LineBreakpoint)) {
            throw new UnknownTypeException(object);
        }
        return LINE_BREAKPOINT;
    }
}
