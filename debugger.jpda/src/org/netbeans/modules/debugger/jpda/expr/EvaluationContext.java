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

import com.sun.jdi.ArrayReference;
import com.sun.jdi.ClassNotLoadedException;
import com.sun.jdi.ClassType;
import com.sun.jdi.Field;
import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.InvalidTypeException;
import com.sun.jdi.LocalVariable;
import com.sun.jdi.Mirror;
import com.sun.jdi.ObjectReference;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;
import com.sun.jdi.Type;
import com.sun.jdi.Value;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;

import java.util.*;

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
    private Stack<Map<String, ScriptVariable>> stack = new Stack<Map<String, ScriptVariable>>();
    private Map<String, ScriptVariable> scriptLocalVariables = new HashMap<String, ScriptVariable>();

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

        stack.push(new HashMap<String, ScriptVariable>());
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

    public VariableInfo getVariableInfo(Tree tree) {
        return variables.get(tree);
    }

    public void putField(Tree tree, Field field, ObjectReference objectRef) {
        VariableInfo info = new VariableInfo.FieldI(field, objectRef);
        variables.put(tree, info);
    }

    public void putLocalVariable(Tree tree, LocalVariable var) {
        VariableInfo info = new VariableInfo.LocalVarI(var, this);
        variables.put(tree, info);
    }

    public void putArrayAccess(Tree tree, ArrayReference array, int index) {
        VariableInfo info = new VariableInfo.ArrayElementI(array, index);
        variables.put(tree, info);
    }

    public void putScriptVariable(Tree tree, ScriptVariable var) {
        VariableInfo info = new VariableInfo.ScriptLocalVarI(var);
        variables.put(tree, info);
    }

    public ScriptVariable getScriptVariableByName(String name) {
        return scriptLocalVariables.get(name);
    }

    public ScriptVariable createScriptLocalVariable(String name, Type type) {
        Map<String, ScriptVariable> map = stack.peek();
        ScriptVariable var = new ScriptVariable(name, type);
        map.put(name, var);
        scriptLocalVariables.put(name, var);
        return var;
    }

    public void pushBlock() {
        stack.push(new HashMap<String, ScriptVariable>());
    }

    public void popBlock() {
        Map<String, ScriptVariable> map = stack.pop();
        for (String name : map.keySet()) {
            scriptLocalVariables.remove(name);
        }
    }

    // *************************************************************************

    public static class ScriptVariable {
        private String name;
        private Type type;
        private Mirror value;
        private boolean valueInited = false;

        public ScriptVariable(String name, Type type) {
            this.name = name;
            this.type = type;
        }

        public Mirror getValue() {
            // check if value is inited [TODO]
            return value;
        }

        public void setValue(Mirror value) {
            this.value = value;
            // check value type [TODO]
            valueInited = true;
        }

    }

    public static abstract class VariableInfo {

        public abstract void setValue(Value value);

        private static class FieldI extends VariableInfo {

            private Field field;
            private ObjectReference fieldObject;

            FieldI(Field field) {
                this.field = field;
            }

            FieldI(Field field, ObjectReference fieldObject) {
                this.field = field;
                this.fieldObject = fieldObject;
            }

            @Override
            public void setValue(Value value) {
                try {
                    if (fieldObject != null) {
                        fieldObject.setValue(field, value);
                    } else {
                        ((ClassType) field.declaringType()).setValue(field, value);
                    }
                } catch (InvalidTypeException itex) {
                } catch (ClassNotLoadedException cnlex) {
                }
            }
        } // FieldI class

        private static class LocalVarI extends VariableInfo {

            private LocalVariable var;
            private EvaluationContext context;

            LocalVarI(LocalVariable var, EvaluationContext context) {
                this.var = var;
                this.context = context;
            }

            @Override
            public void setValue(Value value) {
                try {
                    context.getFrame().setValue(var, value);
                } catch (InvalidTypeException itex) {
                } catch (ClassNotLoadedException cnlex) {
                }
            }
        } // LocalVarI class

        private static class ArrayElementI extends VariableInfo {

            private ArrayReference array;
            private int index;

            ArrayElementI(ArrayReference array, int index) {
                this.array = array;
                this.index = index;
            }

            @Override
            public void setValue(Value value) {
                try {
                    array.setValue(index, value);
                } catch (ClassNotLoadedException ex) {
                } catch (InvalidTypeException ex) {
                }
            }
        } // ArrayElementI class

        private static class ScriptLocalVarI extends VariableInfo {

            private ScriptVariable variable;

            ScriptLocalVarI(ScriptVariable variable) {
                this.variable = variable;
            }

            @Override
            public void setValue(Value value) {
                variable.setValue(value);
            }

        } // ScriptLocalVarI class
    }

}