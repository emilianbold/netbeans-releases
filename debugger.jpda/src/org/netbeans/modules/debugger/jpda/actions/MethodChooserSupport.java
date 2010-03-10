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

import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.KeyStroke;
import org.openide.util.NbBundle;
import org.netbeans.api.debugger.jpda.JPDADebugger;
import org.netbeans.api.debugger.jpda.JPDAThread;
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
import org.openide.text.Annotation;

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
            segments[x] = new MethodChooser.Segment(start, end);
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

        if (currOpIndex == -1) {
            selectedOp = operations[operations.length - 1];
        } else if (currOpIndex == lastOpIndex) {
            tempOps = new Operation[operations.length - 1 - currOpIndex];
            tempLocs = new Location[operations.length - 1 - currOpIndex];
            for (int x = 0; x < tempOps.length; x++) {
                tempOps[x] = operations[x + currOpIndex + 1];
                tempLocs[x] = locations[x + currOpIndex + 1];
            }
            operations = tempOps;
            locations = tempLocs;
            if (operations.length == 0) {
                return false;
            }
            selectedOp = operations[0];
        } else {
            selectedIndex = currOpIndex;
            // do not show UI, continue directly using the selection
            String name = operations[selectedIndex].getMethodName();
            RunIntoMethodActionProvider.doAction(debugger, name, locations[selectedIndex], true);
            return true;
        }

        Object[][] elems = new Object[operations.length][2];
        for (int i = 0; i < operations.length; i++) {
            elems[i][0] = operations[i];
            elems[i][1] = locations[i];
        }
        Arrays.sort(elems, new OperatorsComparator());
        selectedIndex = 0;
        for (int i = 0; i < operations.length; i++) {
            operations[i] = (Operation)elems[i][0];
            locations[i] = (Location)elems[i][1];
            if (operations[i].equals(selectedOp)) {
                selectedIndex = i;
            }
        }
        if (operations.length == 1) {
            // do not show UI, continue directly using the selection
            String name = operations[selectedIndex].getMethodName();
            RunIntoMethodActionProvider.doAction(debugger, name, locations[selectedIndex], true);
            return true;
        }
        return false;
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
        if (debugger.getState() == JPDADebugger.STATE_DISCONNECTED ||
                currentThread != debugger.getCurrentThread() || !currentThread.isSuspended()) {
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
    
}
