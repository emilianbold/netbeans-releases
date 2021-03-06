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

package org.netbeans.modules.debugger.jpda.truffle.access;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.function.IntFunction;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.debugger.jpda.LocalVariable;
import org.netbeans.modules.debugger.jpda.truffle.ast.TruffleNode;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackFrame;
import org.netbeans.modules.debugger.jpda.truffle.frames.TruffleStackInfo;
import org.netbeans.modules.debugger.jpda.truffle.source.SourcePosition;
import org.netbeans.modules.debugger.jpda.truffle.vars.TruffleScope;

/**
 * Container of information about the current program counter.
 * 
 * @author Martin
 */
public class CurrentPCInfo {
    
    public static final String PROP_SELECTED_FRAME = "selectedFrame";           // NOI18N
    
    private final LocalVariable stepCmd;
    private final Reference<JPDAThread> threadRef;
    private final SourcePosition sp;
    private final TruffleScope[] scopes;
    private final TruffleStackFrame topFrame;
    private final TruffleStackInfo stack;
    private final IntFunction<TruffleNode> truffleNodes;
    private volatile TruffleStackFrame selectedStackFrame; // the top frame initially
    
    private PropertyChangeSupport pchs = new PropertyChangeSupport(this);
    
    CurrentPCInfo(LocalVariable stepCmd, JPDAThread thread, SourcePosition sp,
                  TruffleScope[] scopes, TruffleStackFrame topFrame,
                  TruffleStackInfo stack, IntFunction<TruffleNode> truffleNodes) {
        this.stepCmd = stepCmd;
        this.threadRef = new WeakReference<>(thread);
        this.sp = sp;
        this.scopes = scopes;
        this.topFrame = topFrame;
        this.stack = stack;
        this.truffleNodes = truffleNodes;
        selectedStackFrame = topFrame;
    }
    
    public LocalVariable getStepCommandVar() {
        return stepCmd;
    }
    
    public JPDAThread getThread() {
        return threadRef.get();
    }
    
    public SourcePosition getSourcePosition() {
        return sp;
    }

    public TruffleScope[] getScopes() {
        return scopes;
    }
    
    public TruffleStackFrame getTopFrame() {
        return topFrame;
    }

    public TruffleStackInfo getStack() {
        return stack;
    }

    public TruffleStackFrame getSelectedStackFrame() {
        return selectedStackFrame;
    }

    public void setSelectedStackFrame(TruffleStackFrame selectedStackFrame) {
        TruffleStackFrame old = this.selectedStackFrame;
        this.selectedStackFrame = selectedStackFrame;
        if (old != selectedStackFrame) {
            pchs.firePropertyChange(PROP_SELECTED_FRAME, old, selectedStackFrame);
        }
    }
    
    public TruffleNode getAST(TruffleStackFrame frame) {
        return truffleNodes.apply(frame.getDepth());
    }
    
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pchs.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pchs.removePropertyChangeListener(listener);
    }

}
