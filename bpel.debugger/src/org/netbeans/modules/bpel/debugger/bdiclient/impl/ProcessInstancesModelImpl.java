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

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;
import javax.xml.namespace.QName;
import org.netbeans.modules.bpel.debugger.api.ProcessInstance;
import org.netbeans.modules.bpel.debugger.api.ProcessInstancesModel;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.BPELProcessInstanceRef;
import org.netbeans.modules.bpel.debugger.BpelDebuggerImpl;
import org.netbeans.modules.bpel.debuggerbdi.rmi.api.BPELProcessRef;

/**
 * Model for maintaining the list of process instances.
 * Supports notification of process state changes.
 *
 * @author Sun Microsystems
 */
public class ProcessInstancesModelImpl implements ProcessInstancesModel {

    private static Logger LOGGER = Logger.getLogger(ProcessInstancesModelImpl.class.getName());
    
    private final Vector mListeners = new Vector();
    private final BpelDebuggerImpl mDebugger;
    
    private final Map<String, ProcessInstanceImpl> mProcessInstances = Collections.synchronizedMap(
            new LinkedHashMap<String, ProcessInstanceImpl>());
    
    private final Map<QName, BpelProcessImpl> mProcesses =
            new HashMap<QName, BpelProcessImpl>();
    
    public ProcessInstancesModelImpl(BpelDebuggerImpl debugger) {
        mDebugger = debugger;
    }
    
    public BpelDebuggerImpl getDebugger() {
        return mDebugger;
    }
    
    public void clear() {
        mProcessInstances.clear();
        mListeners.clear();
    }
    
    public ProcessInstanceImpl[] getProcessInstances() {
        return mProcessInstances.values().toArray(new ProcessInstanceImpl[mProcessInstances.size()]);
    }
    
    public BpelProcessImpl getProcess(BPELProcessRef processRef) {
        QName processQName = makeProcessQName(processRef.uri());
        BpelProcessImpl process = mProcesses.get(processQName);
        if (process == null) {
            process = new BpelProcessImpl(processRef, mDebugger);
            mProcesses.put(processQName, process);
        }
        return process;
    }
    
    public void addListener(ProcessInstancesModel.Listener listener) {
        mListeners.add(listener);
    }

    public void removeListener(ProcessInstancesModel.Listener listener) {
        mListeners.remove(listener);
    }
    
    protected void processUndeployed(String uri) {
        QName processQName = makeProcessQName(uri);
        for (ProcessInstanceImpl processInstance : getProcessInstances()) {
            if (processInstance.getProcessQName().equals(processQName)) {
                processInstance.setUndeployed(true);
                if (processInstance.getState() == ProcessInstance.STATE_SUSPENDED) {
                    processInstance.resume();
                }
                mProcessInstances.remove(processInstance.getId());
                fireProcessInstanceRemoved(processInstance);
            }
        }
        mProcesses.remove(processQName);
    }

    protected BDIDebugFrame frameCreated(String id, String processInstanceId, String parentFrameId, String bpelFile, String uri) {
        ProcessInstanceImpl processInstance = mProcessInstances.get(processInstanceId);
        BDIDebugFrame frame = processInstance.addFrame(id, parentFrameId);
        return frame;
    }
    
    protected void processInstanceStarted(BPELProcessInstanceRef processInstanceRef) {
        ProcessInstanceImpl processInstance = new ProcessInstanceImpl(this, processInstanceRef);
        mProcessInstances.put(processInstance.getId(), processInstance);
        processInstance.onProcessInstanceStarted();
        fireProcessInstanceAdded(processInstance);
    }
    
    protected void processInstanceDied(BPELProcessInstanceRef processInstanceRef) {
        String processInstanceId = processInstanceRef.globalID();
        ProcessInstanceImpl processInstance = mProcessInstances.get(processInstanceId);
        processInstance.onProcessInstanceDied();
    }
    
    protected void processInstanceCompleted(ProcessInstanceImpl processInstance) {
        mProcessInstances.remove(processInstance.getId());
        fireProcessInstanceRemoved(processInstance);
    }
    
    protected void processInstanceStateChanged(ProcessInstanceImpl processInstance, int oldState, int newState) {
        fireProcessInstanceStateChanged(processInstance, oldState, newState);
    }
    
    private void fireProcessInstanceAdded(ProcessInstanceImpl processInstance) {
        ProcessInstancesModel.Listener[] listeners = new ProcessInstancesModel.Listener[mListeners.size()];
        mListeners.copyInto(listeners);
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].processInstanceAdded(processInstance);
        }
    }
    
    private void fireProcessInstanceRemoved(ProcessInstanceImpl processInstance) {
        ProcessInstancesModel.Listener[] listeners = new ProcessInstancesModel.Listener[mListeners.size()];
        mListeners.copyInto(listeners);
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].processInstanceRemoved(processInstance);
        }
    }
    
    private void fireProcessInstanceStateChanged(ProcessInstanceImpl processInstance, int oldState, int newState) {
        ProcessInstancesModel.Listener[] listeners = new ProcessInstancesModel.Listener[mListeners.size()];
        mListeners.copyInto(listeners);
        for (int i = 0; i < listeners.length; i++) {
            listeners[i].processInstanceStateChanged(processInstance, oldState, newState);
        }
    }
    
    public static QName makeProcessQName(String uri) {
        int pos = uri.lastIndexOf('/');
        return new QName(uri.substring(0, pos), uri.substring(pos + 1));
    }
    
}
