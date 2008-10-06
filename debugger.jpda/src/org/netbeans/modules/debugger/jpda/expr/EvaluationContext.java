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

package org.netbeans.modules.debugger.jpda.expr;

import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import java.util.*;
import javax.lang.model.element.TypeElement;

import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;

/**
 * Defines the exection context in which to evaluate a given expression. The context consists of:
 * the current stack frame and the source file in which the expression would exist. The source file
 * is needed for the import facility to work.
 *
 * @author Maros Sandor
 */
public class EvaluationContext {

    /**
     * The runtime context of a JVM is represented by a stack frame.
     */
    private StackFrame frame;
    private int frameDepth;
    private ThreadReference thread;
    private List<String> sourceImports;
    private List<String> staticImports;
    private boolean canInvokeMethods;
    private Runnable methodInvokePreproc;
    private JPDADebuggerImpl debugger;
    
    private Trees trees;
    private CompilationUnitTree compilationUnitTree;
    private TreePath treePath;
    private Map<Tree, VariableInfo> variables = new HashMap<Tree, VariableInfo>();

    /**
     * Creates a new context in which to evaluate expresions.
     *
     * @param frame the frame in which context evaluation occurrs
     * @param imports list of imports
     * @param staticImports list of static imports
     */
    public EvaluationContext(ThreadReference thread, StackFrame frame, int frameDepth,
                             List<String> imports, List<String> staticImports,
                             boolean canInvokeMethods, Runnable methodInvokePreproc,
                             JPDADebuggerImpl debugger) {
        if (thread == null) throw new IllegalArgumentException("Thread argument must not be null");
        if (frame == null) throw new IllegalArgumentException("Frame argument must not be null");
        if (imports == null) throw new IllegalArgumentException("Imports argument must not be null");
        if (staticImports == null) throw new IllegalArgumentException("Static imports argument must not be null");
        this.thread = thread;
        this.frame = frame;
        this.frameDepth = frameDepth;
        this.sourceImports = imports;
        this.staticImports = staticImports;
        this.canInvokeMethods = canInvokeMethods;
        this.methodInvokePreproc = methodInvokePreproc;
        this.debugger = debugger;
    }

    public List<String> getStaticImports() {
        return staticImports;
    }

    public List<String> getImports() {
        return sourceImports;
    }

    public StackFrame getFrame() {
        return frame;
    }
    
    public boolean canInvokeMethods() {
        return canInvokeMethods;
    }
    
    void setCanInvokeMethods(boolean canInvokeMethods) {
        this.canInvokeMethods = canInvokeMethods;
    }
    
    void methodToBeInvoked() {
        if (methodInvokePreproc != null) {
            methodInvokePreproc.run();
        }
    }
    
    void methodInvokeDone() throws IncompatibleThreadStateException {
        // Refresh the stack frame
        frame = thread.frame(frameDepth);
    }
    
    JPDADebuggerImpl getDebugger() {
        return debugger;
    }
    
    public void setTrees(Trees trees) {
        this.trees = trees;
    }
    
    Trees getTrees() {
        return trees;
    }
    
    public void setCompilationUnit(CompilationUnitTree compilationUnitTree) {
        this.compilationUnitTree = compilationUnitTree;
    }
    
    CompilationUnitTree getCompilationUnit() {
        return compilationUnitTree;
    }
    
    public void setTreePath(TreePath treePath) {
        this.treePath = treePath;
    }
    
    TreePath getTreePath() {
        return treePath;
    }
    
    Map<Tree, VariableInfo> getVariables() {
        return variables;
    }
    
    static final class VariableInfo {
        public Field field;
        public ObjectReference fieldObject;
        public LocalVariable var;
        
        public VariableInfo(Field field) {
            this.field = field;
        }
        
        public VariableInfo(Field field, ObjectReference fieldObject) {
            this.field = field;
            this.fieldObject = fieldObject;
        }
        
        public VariableInfo(LocalVariable var) {
            this.var = var;
        }
    }
    
}

