/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.debugger.jpda.actions;

import com.sun.jdi.AbsentInformationException;
import com.sun.jdi.Location;
import com.sun.jdi.ReferenceType;

import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import java.util.concurrent.Future;
import javax.swing.JEditorPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import org.openide.util.NbBundle;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAStep;
import org.netbeans.api.debugger.jpda.JPDAThread;
import org.netbeans.api.java.source.CancellableTask;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.JavaSource.Phase;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.debugger.jpda.EditorContextBridge;
import org.netbeans.modules.debugger.jpda.JPDADebuggerImpl;
import org.netbeans.modules.debugger.jpda.ExpressionPool.Expression;
import org.netbeans.spi.debugger.jpda.EditorContext.Operation;

import org.netbeans.modules.debugger.jpda.jdi.ClassNotPreparedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.InternalExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ObjectCollectedExceptionWrapper;
import org.netbeans.modules.debugger.jpda.jdi.ReferenceTypeWrapper;
import org.netbeans.modules.debugger.jpda.jdi.VMDisconnectedExceptionWrapper;
import org.netbeans.spi.debugger.jpda.EditorContext;
import org.netbeans.spi.debugger.ui.MethodChooser;
import org.openide.ErrorManager;
import org.openide.cookies.EditorCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotation;
import org.openide.util.Exceptions;

public class MethodChooserSupport implements PropertyChangeListener {

    private JPDADebuggerImpl debugger;
    private JPDAThread currentThread;
    private String url;
    private ReferenceType clazzRef;
    private MethodChooser chooser;

    ArrayList<Annotation> annotations;
    private int startLine;
    private int endLine;
    private int selectedIndex = -1;
    private Operation[] operations;
    private Location[] locations;
    private boolean[] isCertainlyReachable;

    MethodChooserSupport(JPDADebuggerImpl debugger, String url, ReferenceType clazz, int methodLine, int methodOffset) {
        this.debugger = debugger;
        this.currentThread = debugger.getCurrentThread();
        this.url = url;
        this.clazzRef = clazz;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public MethodChooser.Segment[] getSegments() {
        MethodChooser.Segment[] segments = new MethodChooser.Segment[operations.length];
        for (int x = 0; x < segments.length; x++) {
            int start = operations[x].getMethodStartPosition().getOffset();
            int end = operations[x].getMethodEndPosition().getOffset();
            if (isCertainlyReachable[x]) {
                segments[x] = new MethodChooser.Segment(start, end);
            } else {
                segments[x] = new UncertainSegment(start, end);
            }
        }
        return segments;
    }

    public int getSegmentsCount() {
        return operations.length;
    }

    public String getHint() {
        return NbBundle.getMessage(MethodChooserSupport.class, "MSG_RunIntoMethod_Status_Line_Help");
    }

    public KeyStroke[] getStopEvents() {
        return new KeyStroke[] {
            KeyStroke.getKeyStroke(KeyEvent.VK_F8, 0),
            KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.SHIFT_DOWN_MASK),
            KeyStroke.getKeyStroke(KeyEvent.VK_F7, KeyEvent.CTRL_DOWN_MASK)
        };
    }

    public KeyStroke[] getConfirmEvents() {
        return new KeyStroke[] {
            KeyStroke.getKeyStroke(KeyEvent.VK_F7, 0)
        };
    }

    public MethodChooser createChooser() {
        return new MethodChooser(
                    url, getSegments(), selectedIndex,
                    getHint(), getStopEvents(), getConfirmEvents()
                );
    }

    public void tearUp(MethodChooser selector) {
        // hack - disable org.netbeans.modules.debugger.jpda.projects.ToolTipAnnotation
        System.setProperty("org.netbeans.modules.debugger.jpda.doNotShowTooltips", "true"); // NOI18N
        this.chooser = selector;
        debugger.addPropertyChangeListener(this);
        debugger.getThreadsCollector().addPropertyChangeListener(this);
        annotateLines();
    }

    public void tearDown() {
        // hack - enable org.netbeans.modules.debugger.jpda.projects.ToolTipAnnotation
        System.clearProperty("org.netbeans.modules.debugger.jpda.doNotShowTooltips"); // NOI18N
        debugger.removePropertyChangeListener(this);
        debugger.getThreadsCollector().removePropertyChangeListener(this);
        clearAnnotations();
    }

    public void doStepInto() {
        final int index = chooser.getSelectedIndex();
        final String name = operations[index].getMethodName();
        debugger.getRequestProcessor().post(new Runnable() {
            public void run() {
                RunIntoMethodActionProvider.doAction(debugger, name, locations[index], true);
            }
        });
    }

    public boolean init() {
        operations = new Operation[0];
        int methodLine = currentThread.getLineNumber(null);
        
        List<Location> locs = java.util.Collections.emptyList();
        try {
            while (methodLine > 0 && (locs = ReferenceTypeWrapper.locationsOfLine(clazzRef, methodLine)).isEmpty()) {
                methodLine--;
            }
        } catch (InternalExceptionWrapper aiex) {
        } catch (VMDisconnectedExceptionWrapper aiex) {
            return false;
        } catch (ObjectCollectedExceptionWrapper aiex) {
            return false;
        } catch (ClassNotPreparedExceptionWrapper aiex) {
        } catch (AbsentInformationException aiex) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, aiex);
        }
        if (locs.isEmpty()) {
            return false;
        }
        Expression expr = debugger.getExpressionPool().getExpressionAt(locs.get(0), url);
        if (expr == null) {
            return false;
        }
        Operation currOp = currentThread.getCurrentOperation();
        List<Operation> lastOpsList = currentThread.getLastOperations();
        Operation lastOp = lastOpsList != null && lastOpsList.size() > 0 ? lastOpsList.get(lastOpsList.size() - 1) : null;
        Operation selectedOp = null;
        Operation[] tempOps = expr.getOperations();
        if (tempOps.length == 0) {
            return false;
        }
        Location[] tempLocs = expr.getLocations();
        operations = new Operation[tempOps.length];
        locations = new Location[tempOps.length];
        for (int x = 0; x < tempOps.length; x++) {
            operations[x] = tempOps[x];
            locations[x] = tempLocs[x];
        }
        startLine = operations[0].getMethodStartPosition().getLine();
        endLine = operations[operations.length - 1].getMethodEndPosition().getLine();
        for (int i = 1; i < (operations.length - 1); i++) {
            int line = operations[i].getMethodStartPosition().getLine();
            if (line < startLine) {
                startLine = line;
            }
            if (line > endLine) {
                endLine = line;
            }
        }

        int currOpIndex = -1;
        int lastOpIndex = -1;

        if (currOp != null) {
            int index = currOp.getBytecodeIndex();
            for (int x = 0; x < operations.length; x++) {
                if (operations[x].getBytecodeIndex() == index) {
                    currOpIndex = x;
                    break;
                }
            }
        }
        if (lastOp != null) {
            int index = lastOp.getBytecodeIndex();
            for (int x = 0; x < operations.length; x++) {
                if (operations[x].getBytecodeIndex() == index) {
                    lastOpIndex = x;
                    break;
                }
            }
        }

        Operation opToExecute = null;
        if (currOpIndex == -1) {
            selectedOp = operations[operations.length - 1];
            opToExecute = operations[0];
        } else {
            int splitIndex = currOpIndex == lastOpIndex ? currOpIndex : currOpIndex - 1;
            if (splitIndex + 1 < operations.length) {
                opToExecute = operations[splitIndex + 1];
            }
            tempOps = new Operation[operations.length - 1 - splitIndex];
            tempLocs = new Location[operations.length - 1 - splitIndex];
            for (int x = 0; x < tempOps.length; x++) {
                tempOps[x] = operations[x + splitIndex + 1];
                tempLocs[x] = locations[x + splitIndex + 1];
            }
            operations = tempOps;
            locations = tempLocs;
            if (operations.length == 0) {
                return false;
            }
            selectedOp = operations[0];
        }

        Object[][] elems = new Object[operations.length][2];
        for (int i = 0; i < operations.length; i++) {
            elems[i][0] = operations[i];
            elems[i][1] = locations[i];
        }
        Arrays.sort(elems, new OperatorsComparator());
        isCertainlyReachable = new boolean[operations.length];
        for (int i = 0; i < operations.length; i++) {
            operations[i] = (Operation)elems[i][0];
            locations[i] = (Location)elems[i][1];
            isCertainlyReachable[i] = true;
        }
        int[] flags = new int[operations.length];
        for (int i = 0; i < flags.length; i++) {
            flags[i] = 0;
        }
        detectUnreachableOps(flags, currOp);
        int count = 0;
        for (int i = 0; i < flags.length; i++) {
            if (flags[i] < 2) {
                count++;
            }
        }
        tempOps = operations;
        tempLocs = locations;
        operations = new Operation[count];
        locations = new Location[count];
        isCertainlyReachable = new boolean[count];
        int index = 0;
        int opToExecuteIndex = -1;
        for (int i = 0; i < flags.length; i++) {
            if (flags[i] < 2) {
                operations[index] = tempOps[i];
                locations[index] = tempLocs[i];
                isCertainlyReachable[index] = flags[i] == 0;
                if (opToExecute == operations[index]) {
                    opToExecuteIndex = index;
                }
                index++;
            }
        }

        selectedIndex = 0;
        for (int i = 0; i < operations.length; i++) {
            if (operations[i].equals(selectedOp) && isCertainlyReachable[i]) {
                selectedIndex = i;
            }
        }

        if (opToExecuteIndex >= 0 && !isCertainlyReachable[opToExecuteIndex]) {
            // perform step over expression and run init() again
            synchronized(this) {
                StepOperationActionProvider.doAction(debugger, this);
                try {
                    wait();
                } catch (InterruptedException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return init();
        }

        if (operations.length == 1) {
            // do not show UI, continue directly using the selection
            String name = operations[selectedIndex].getMethodName();
            RunIntoMethodActionProvider.doAction(debugger, name, locations[selectedIndex], true);
            return true;
        }
        return false;
    }

    private void detectUnreachableOps(final int[] flags, final Operation currOp) {
        FileObject fileObj = null;
        try {
            fileObj = URLMapper.findFileObject(new URL(url));
        } catch (MalformedURLException e) {
        }
        if (fileObj == null) return;
        DataObject dobj = null;
        try {
            dobj = DataObject.find(fileObj);
        } catch (DataObjectNotFoundException ex) {
        }
        if (dobj == null) return;
        final EditorCookie ec = (EditorCookie)dobj.getCookie(EditorCookie.class);
        if (ec == null) return;
        JavaSource js = JavaSource.forFileObject(fileObj);
        if (js == null) return;

        final JEditorPane[] editorPane = new JEditorPane[1];
        if (SwingUtilities.isEventDispatchThread()) {
            JEditorPane[] openedPanes = ec.getOpenedPanes();
            if (openedPanes != null && openedPanes.length > 0) {
                editorPane[0] = openedPanes[0];
            }
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    public void run() {
                        JEditorPane[] openedPanes = ec.getOpenedPanes();
                        if (openedPanes != null && openedPanes.length > 0) {
                            editorPane[0] = openedPanes[0];
                        }
                    }
                });
            } catch (InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            } catch (InterruptedException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        final Future<Void> scanFinished;
        try {
            scanFinished = js.runWhenScanFinished(new CancellableTask<CompilationController>() {
                public void cancel() {
                }
                public void run(CompilationController ci) throws Exception {
                    if (ci.toPhase(Phase.RESOLVED).compareTo(Phase.RESOLVED) < 0) {
                        ErrorManager.getDefault().log(ErrorManager.WARNING,
                                "Unable to resolve "+ci.getFileObject()+" to phase "+Phase.RESOLVED+", current phase = "+ci.getPhase()+
                                "\nDiagnostics = "+ci.getDiagnostics()+
                                "\nFree memory = "+Runtime.getRuntime().freeMemory());
                        return;
                    }
                    SourcePositions positions = ci.getTrees().getSourcePositions();
                    CompilationUnitTree compUnit = ci.getCompilationUnit();
                    TreeUtilities treeUtils = ci.getTreeUtilities();
                    int pcOffset = currOp == null ? 0 : currOp.getMethodStartPosition().getOffset() + 1;
                    for (int i = 0; i < operations.length; i++) {
                        int offset = operations[i].getMethodStartPosition().getOffset() + 1;
                        TreePath path = treeUtils.pathFor(offset);
                        while (path != null) {
                            Tree tree = path.getLeaf();
                            if (tree instanceof ConditionalExpressionTree) {
                                ConditionalExpressionTree ternaryOpTree = (ConditionalExpressionTree)tree;
                                //Tree condTree = ternaryOpTree.getCondition();
                                Tree trueTree = ternaryOpTree.getTrueExpression();
                                Tree falseTree = ternaryOpTree.getFalseExpression();
                                //long condStart = positions.getStartPosition(compUnit, condTree);
                                //long condEnd = positions.getEndPosition(compUnit, condTree);
                                long trueStart = positions.getStartPosition(compUnit, trueTree);
                                long trueEnd = positions.getEndPosition(compUnit, trueTree);
                                long falseStart = positions.getStartPosition(compUnit, falseTree);
                                long falseEnd = positions.getEndPosition(compUnit, falseTree);

                                if (trueStart <= offset && offset <= trueEnd) {
                                    if (pcOffset < trueStart) {
                                        markSegment(i, false);
                                    }
                                } else if (falseStart <= offset && offset <= falseEnd) {
                                    if (pcOffset < trueStart) {
                                        markSegment(i, false);
                                    } else if (trueStart <= pcOffset && pcOffset <= trueEnd) {
                                        markSegment(i, true);
                                    }
                                }
                            } else if (tree.getKind() == Tree.Kind.CONDITIONAL_AND ||
                                    tree.getKind() == Tree.Kind.CONDITIONAL_OR) {
                                BinaryTree binaryTree = (BinaryTree)tree;
                                Tree rightTree = binaryTree.getRightOperand();
                                long rightStart = positions.getStartPosition(compUnit, rightTree);
                                long rightEnd = positions.getEndPosition(compUnit, rightTree);

                                if (rightStart <= offset && offset <= rightEnd) {
                                    if (pcOffset < rightStart) {
                                        markSegment(i, false);
                                    }
                                }
                            }
                            path = path.getParentPath();
                        } // while
                    } // for
                }

                public void markSegment(int index, boolean excludeSegment) {
                    if (flags[index] == 2) return;
                    flags[index] = excludeSegment ? 2 : 1;
                }

            }, true);
            if (!scanFinished.isDone()) {
                if (java.awt.EventQueue.isDispatchThread()) {
                    return;
                } else {
                    try {
                        scanFinished.get();
                    } catch (InterruptedException iex) {
                    } catch (java.util.concurrent.ExecutionException eex) {
                        ErrorManager.getDefault().notify(eex);
                    }
                }
            }
        } catch (IOException ioex) {
            ErrorManager.getDefault().notify(ioex);
        }
    }

    private void annotateLines() {
        annotations = new ArrayList<Annotation>();
        EditorContext context = EditorContextBridge.getContext();
        JPDAThread thread = debugger.getCurrentThread();
        Operation currOp = thread.getCurrentOperation();
        int currentLine = currOp != null ? currOp.getStartPosition().getLine() : thread.getLineNumber(null);
        String annoType = currOp != null ?
            EditorContext.CURRENT_EXPRESSION_CURRENT_LINE_ANNOTATION_TYPE :
            EditorContext.CURRENT_LINE_ANNOTATION_TYPE;
        for (int lineNum = startLine; lineNum <= endLine; lineNum++) {
            if (lineNum != currentLine) {
                Object anno = context.annotate(url, lineNum, annoType, null);
                if (anno instanceof Annotation) {
                    annotations.add((Annotation)anno);
                }
            } // if
        } // for
    }

    private void clearAnnotations() {
        if (annotations != null) {
            for (Annotation anno : annotations) {
                anno.detach();
            }
        }
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        if (JPDAStep.PROP_STATE_EXEC.equals(evt.getPropertyName())) {
            synchronized(this) {
                notifyAll();
            }
        } else if (debugger.getState() == JPDADebugger.STATE_DISCONNECTED ||
                currentThread != debugger.getCurrentThread() || !currentThread.isSuspended()) {
            synchronized(this) {
                notifyAll();
            }
            chooser.releaseUI(false);
        }
    }

    // **************************************************************************
    // inner classes
    // **************************************************************************
    
    private static final class OperatorsComparator implements Comparator {

        @Override
        public int compare(Object o1, Object o2) {
            Object[] a1 = (Object[])o1;
            Object[] a2 = (Object[])o2;
            Operation op1 = (Operation)a1[0];
            Operation op2 = (Operation)a2[0];
            return op1.getMethodStartPosition().getOffset() - op2.getMethodStartPosition().getOffset();
        }
        
    }

    private static class UncertainSegment extends MethodChooser.Segment {

        public UncertainSegment(int start, int end) {
            super(start, end);
        }

    }
    
}
