/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.bpel.debugger.bdiclient.impl;

import org.netbeans.modules.bpel.debugger.api.DebugException;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.BPELDebugger;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.BPELProcessInstanceRef;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.BPELProcessRef;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.DebugFrame;
import org.netbeans.modules.bpel.debugger.BpelDebuggerImpl;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.VirtualBPELEngine;

/**
 * This is a remote object, the BPEL service engine makes callbacks
 * into this. The methods called are defined in the BPELDebugger interface.
 * We receive enterFrame events for each thread entered.
 *
 * @author Alexander Zgursky
 */
public class BDIDebugger implements BPELDebugger {
    private BpelDebuggerImpl mDebugger;
    private VirtualBPELEngine mVirtualBpelEngine;
    
    /** Creates a new instance of BDIDebugger */
    public BDIDebugger(BpelDebuggerImpl debugger) {
        mDebugger = debugger;
    }
    
    public DebugFrame enterFrame(String id, String processInstanceId, String parentFrameId, String bpelFile, String uri) {
        //System.out.println("Enter frame: " + id + "(" + parentFrameId + ")");
        BDIDebugFrame debugFrame = getModel().frameCreated(id, processInstanceId, parentFrameId, bpelFile, uri);
        return debugFrame;
    }
    
    public boolean detach() {
        //TODO:change to NLS
        mDebugger.setException(new DebugException("Target disconnected"));
        return true;
    }

    public boolean processRemoved(String targetNamespace) {
        //NOT USED ANYMORE
//        getModel().processUndeployed(targetNamespace);
        return true;
    }
    
    public void processRemoved(BPELProcessRef process) {
        getModel().processUndeployed(process.uri());
    }

    public void processAdded(BPELProcessRef process) {
        //Nothing to do
    }

    public void processInstanceStarted(BPELProcessInstanceRef instance) {
        getModel().processInstanceStarted(instance);
    }

    public void processInstanceDied(BPELProcessInstanceRef instance) {
        getModel().processInstanceDied(instance);
    }

    public void setVirtualBPELEngine(VirtualBPELEngine engine) {
        mVirtualBpelEngine = engine;
    }
    
    public VirtualBPELEngine getVirtualBPELEngine() {
        return mVirtualBpelEngine;
    }

    private ProcessInstancesModelImpl getModel() {
        return mDebugger.getProcessInstancesModel();
    }
}
