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

package org.netbeans.modules.bpel.debugger.bdiclient.impl;

import org.netbeans.modules.bpel.debugger.api.DebugException;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.BPELDebugger;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.DebugFrame;
import org.netbeans.modules.bpel.debugger.BpelDebuggerImpl;

/**
 * This is a remote object, the BPEL service engine makes callbacks
 * into this. The methods called are defined in the BPELDebugger interface.
 * We receive enterFrame events for each thread entered.
 *
 * @author Alexander Zgursky
 */
public class BDIDebugger implements BPELDebugger {
    private BpelDebuggerImpl mDebugger;
    
    /** Creates a new instance of BDIDebugger */
    public BDIDebugger(BpelDebuggerImpl debugger) {
        mDebugger = debugger;
    }

    public DebugFrame enterFrame(String rootFrameId, String bpelFile, String uri) {
        //doesn't seem to be used anymore
        return null;
    }

    public DebugFrame enterFrame(String rootFrameId, String id, String bpelFile, String uri) {
        BDIDebugFrame debugFrame = getModel().frameCreated(rootFrameId, id, bpelFile, uri);
        return debugFrame;
    }

    public boolean detach() {
        //TODO:change to NLS
        mDebugger.setException(new DebugException("Target disconnected"));
        return true;
    }

    public boolean processRemoved(String targetNamespace) {
        getModel().processUndeployed(targetNamespace);
        return true;
    }
    
    private ProcessInstancesModelImpl getModel() {
        return mDebugger.getProcessInstancesModel();
    }
    
}
