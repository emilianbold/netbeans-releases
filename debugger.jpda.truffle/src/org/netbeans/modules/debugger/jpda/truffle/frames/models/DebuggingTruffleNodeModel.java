/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.debugger.jpda.truffle.frames.models;

import com.sun.jdi.AbsentInformationException;
import java.awt.Color;
import java.awt.datatransfer.Transferable;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.netbeans.api.debugger.jpda.CallStackFrame;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.api.debugger.jpda.MonitorInfo;
import org.netbeans.api.debugger.jpda.This;
import org.netbeans.modules.debugger.jpda.truffle.Utils;
import org.netbeans.modules.debugger.jpda.truffle.access.CurrentPCInfo;
import org.netbeans.modules.debugger.jpda.truffle.access.TruffleAccess;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.spi.debugger.ContextProvider;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.viewmodel.ExtendedNodeModel;
import org.netbeans.spi.viewmodel.ExtendedNodeModelFilter;
import org.netbeans.spi.viewmodel.ModelEvent;
import org.netbeans.spi.viewmodel.ModelListener;
import org.netbeans.spi.viewmodel.NodeModel;
import org.netbeans.spi.viewmodel.UnknownTypeException;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;
import org.openide.util.datatransfer.PasteType;

/**
 *
 * @author Martin
 */
@DebuggerServiceRegistration(path="netbeans-JPDASession/DebuggingView",
                             types=ExtendedNodeModelFilter.class,
                             position=23000)
public class DebuggingTruffleNodeModel implements ExtendedNodeModelFilter {
    
    private final JPDADebugger debugger;
    private final List<ModelListener> listeners = new CopyOnWriteArrayList<ModelListener>();
    private final WeakSet<CurrentPCInfo> cpisListening = new WeakSet<CurrentPCInfo>();
    private final CurrentInfoPropertyChangeListener cpiChL = new CurrentInfoPropertyChangeListener();
    
    public DebuggingTruffleNodeModel(ContextProvider lookupProvider) {
        debugger = lookupProvider.lookupFirst(null, JPDADebugger.class);
    }

    @Override
    public boolean canRename(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return original.canRename(node);
    }

    @Override
    public boolean canCopy(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return original.canCopy(node);
    }

    @Override
    public boolean canCut(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        return original.canCut(node);
    }

    @Override
    public Transferable clipboardCopy(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        return original.clipboardCopy(node);
    }

    @Override
    public Transferable clipboardCut(ExtendedNodeModel original, Object node) throws IOException, UnknownTypeException {
        return original.clipboardCut(node);
    }

    @Override
    public PasteType[] getPasteTypes(ExtendedNodeModel original, Object node, Transferable t) throws UnknownTypeException {
        return original.getPasteTypes(node, t);
    }

    @Override
    public void setName(ExtendedNodeModel original, Object node, String name) throws UnknownTypeException {
        original.setName(node, name);
    }

    @Override
    public String getIconBaseWithExtension(ExtendedNodeModel original, Object node) throws UnknownTypeException {
        if (node instanceof TruffleStackFrame) {
            return original.getIconBaseWithExtension(EmptyCallStackFrame.INSTANCE);
        }
        return original.getIconBaseWithExtension(node);
    }

    @Override
    public String getDisplayName(NodeModel original, Object node) throws UnknownTypeException {
        if (node instanceof TruffleStackFrame) {
            TruffleStackFrame tf = (TruffleStackFrame) node;
            String displayName = tf.getDisplayName();
            CurrentPCInfo currentPCInfo = TruffleAccess.getCurrentPCInfo(debugger);
            if (currentPCInfo != null) {
                synchronized (cpisListening) {
                    if (!cpisListening.contains(currentPCInfo)) {
                        currentPCInfo.addPropertyChangeListener(
                                WeakListeners.propertyChange(cpiChL, currentPCInfo));
                        cpisListening.add(currentPCInfo);
                    }
                }
                TruffleStackFrame selectedStackFrame = currentPCInfo.getSelectedStackFrame();
                if (selectedStackFrame == tf) {
                    displayName = Utils.toHTML(displayName, true, false, null);
                }
            }
            return displayName;
        } else {
            return original.getDisplayName(node);
        }
    }

    @Override
    public String getIconBase(NodeModel original, Object node) throws UnknownTypeException {
        throw new UnsupportedOperationException("Not supported.");
    }

    @Override
    public String getShortDescription(NodeModel original, Object node) throws UnknownTypeException {
        if (node instanceof TruffleStackFrame) {
            return ((TruffleStackFrame) node).getDisplayName();
        } else {
            return original.getShortDescription(node);
        }
    }
    
    private void fireDisplayNamesChanged() {
        ModelEvent evt = new ModelEvent.NodeChanged(this, null);
        for (ModelListener l : listeners) {
            l.modelChanged(evt);
        }
    }

    @Override
    public void addModelListener(ModelListener l) {
        listeners.add(l);
    }

    @Override
    public void removeModelListener(ModelListener l) {
        listeners.remove(l);
    }
    
    private class CurrentInfoPropertyChangeListener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            fireDisplayNamesChanged();
        }
        
    }
    
    private static final class EmptyCallStackFrame implements CallStackFrame {
        
        static final CallStackFrame INSTANCE = new EmptyCallStackFrame();

        @Override
        public int getLineNumber(String struts) {
            return 1;
        }

        @Override
        public int getFrameDepth() {
            return 0;
        }

        @Override
        public EditorContext.Operation getCurrentOperation(String struts) {
            return null;
        }

        @Override
        public String getMethodName() {
            return "";
        }

        @Override
        public String getClassName() {
            return "";
        }

        @Override
        public String getDefaultStratum() {
            return "";
        }

        @Override
        public List<String> getAvailableStrata() {
            return Collections.emptyList();
        }

        @Override
        public String getSourceName(String struts) throws AbsentInformationException {
            return "";
        }

        @Override
        public String getSourcePath(String stratum) throws AbsentInformationException {
            return "";
        }

        @Override
        public LocalVariable[] getLocalVariables() throws AbsentInformationException {
            return new LocalVariable[] {};
        }

        @Override
        public This getThisVariable() {
            return null;
        }

        @Override
        public void makeCurrent() {
        }

        @Override
        public boolean isObsolete() {
            return false;
        }

        @Override
        public void popFrame() {
        }

        @Override
        public JPDAThread getThread() {
            return null;
        }

        @Override
        public List<MonitorInfo> getOwnedMonitors() {
            return Collections.emptyList();
        }
        
    }
    
}
