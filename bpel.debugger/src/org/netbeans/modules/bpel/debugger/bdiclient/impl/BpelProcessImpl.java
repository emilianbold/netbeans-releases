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

package org.netbeans.modules.bpel.debugger.bdiclient.impl;

import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.debugger.BpelDebuggerImpl;
import org.netbeans.modules.bpel.debugger.api.BpelProcess;
import org.netbeans.modules.bpel.debugger.api.psm.ProcessStaticModel;
import org.netbeans.modules.bpel.debugger.psm.ProcessStaticModelImpl;
import org.netbeans.modules.bpel.debugger.spi.ProcessStaticModelProvider;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.BPELProcessRef;
import org.netbeans.spi.debugger.ContextProvider;

/**
 *
 * @author Alexander Zgursky
 */
public class BpelProcessImpl implements BpelProcess {
    private final QName myQName;
    private final BPELProcessRef myProcessRef;
    private final BpelDebuggerImpl myDebugger;
    private ProcessStaticModelImpl myPsm;
    
    /** Creates a new instance of BpelProcessImpl */
    protected BpelProcessImpl(BPELProcessRef processRef, BpelDebuggerImpl debugger) {
        myProcessRef = processRef;
        myQName = ProcessInstancesModelImpl.makeProcessQName(myProcessRef.uri());
        myDebugger = debugger;
    }
    
    public String getName() {
        return myQName.getLocalPart();
    }
    
    public String getTargetNamespace() {
        return myQName.getNamespaceURI();
    }
    
    public QName getQName() {
        return myQName;
    }
    
    public ProcessStaticModel getProcessStaticModel() {
        if (myPsm == null) {
//            ContextProvider contextProvider = myDebugger.getLookupProvider();
//            ProcessStaticModelProvider modelProvider =
//                    (ProcessStaticModelProvider)contextProvider.lookupFirst(
//                    null, ProcessStaticModelProvider.class);
//            myPsm = modelProvider.getModel(myQName);
            myPsm = ProcessStaticModelImpl.build(this);
        }
        return myPsm;
    }
    
    public BPELProcessRef getProcessRef() {
        return myProcessRef;
    }
}
