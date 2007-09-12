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

import org.netbeans.modules.bpel.debuggerbdi.rmi.api.BPELVariable;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.DebugFrame;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.DebuggableEngine;
import org.netbeans.modules.bpel.debugger.BreakPosition;

/**
 * A DebugFrame represents one thread within a running BPEL process.
 * If a BPEL process has branched off multiple threads, there will be
 * one DebugFrame per thread.
 * 
 * This is a remote object, the BPEL service engine makes callbacks into
 * this. The methods called are defined in the DebugFrame interface.
 * We receive onLineChange events for each activity executed.
 * 
 * @author Sun Microsystems
 */
public class BDIDebugFrame implements DebugFrame {
    
    private ProcessInstanceImpl mProcessInstance;
    private String mId;
    
    private DebuggableEngine mEngine;

    public BDIDebugFrame(ProcessInstanceImpl processInstance, String id) {
        mProcessInstance = processInstance;
        mId = id;
    }

    public String getId() {
        return mId;
    }

    //TODO:avoid storing DebuggableEngine in DebugFrame.
    //Store it in BreakPosition instead
    public DebuggableEngine getDebuggableEngine() {
        return mEngine;
    }
    
    public ProcessInstanceImpl getProcessInstance() {
        return mProcessInstance;
    }
    
//  DebugFrame interface methods
//  DebugFrame interface methods
//  DebugFrame interface methods

    public void onLineChange(String bpelFile, String uri, int lineNumber, String xpath, DebuggableEngine engine) {
        if (ignoreEvent(xpath)) {
            return;
        }
        //System.out.println("   Activity started in frame " + mId + ": " + xpath);
        mEngine = engine;
        BreakPosition breakPosition = new BreakPosition(this, xpath);
        mProcessInstance.onActivityStarted(breakPosition);
    }
    
    public void onFault(String bpelFile, String uri, int lineNumber, String xpath) {
        //mProcessInstance.onFault(this);
    }

    public void onXPathException(String bpelFile, String uri, int lineNumber, String message, String xpath) {
        mProcessInstance.onXpathException(this);
    }

    public void onTerminate(String bpelFile, String uri, int lineNumber, String xpath) {
        if (ignoreEvent(xpath)) {
            return;
        }
        //System.out.println("   Activity terminated in frame " + mId + ": " + xpath);
        mProcessInstance.onTerminate(this);
    }

    public void onExit(String bpelFile, String uri) {
        //System.out.println("Exit frame: " + mId);
        mProcessInstance.onExit(this);
    }
    
    public void onFault(String bpelFile, String uri, int lineNumber, String xpath, String faultQName, BPELVariable faultData, DebuggableEngine engine) {
        if (ignoreEvent(xpath)) {
            return;
        }
        //System.out.println("   !!!Fault in frame " + mId + ": " + xpath);
        mEngine = engine;
        BreakPosition breakPosition = new BreakPosition(this, xpath);
        mProcessInstance.onFault(breakPosition, faultQName, faultData);
    }
    
    public void onActivityComplete(String bpelFile, String uri, int lineNumber, String xpath) {
        if (ignoreEvent(xpath)) {
            return;
        }
        //System.out.println("   Activity completed in frame " + mId + ": " + xpath);
        BreakPosition breakPosition = new BreakPosition(this, xpath);
        mProcessInstance.onActivityCompleted(breakPosition);
    }
    
    private boolean ignoreEvent(String xpath) {
        return  xpath.endsWith("onAlarm") ||
                xpath.endsWith("onAlarm[1]") ||
                xpath.endsWith("onEvent") ||
                xpath.endsWith("onEvent[1]");
    }
}
