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
package org.netbeans.modules.cnd.debugger.gdb.actions;

import java.awt.event.KeyEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.KeyStroke;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;
import org.netbeans.modules.cnd.api.model.xref.CsmReference;
import org.netbeans.modules.cnd.debugger.gdb.GdbDebugger;
import org.netbeans.modules.cnd.spi.model.services.FunctionCallsProvider;
import org.netbeans.spi.debugger.ui.MethodChooser;
import org.openide.util.NbBundle;
import org.openide.text.Annotation;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;

public class MethodChooserSupport implements PropertyChangeListener {
    private final GdbDebugger debugger;
//    private JPDAThread currentThread;
    private final String url;
//    private ReferenceType clazzRef;
    private MethodChooser chooser;

    ArrayList<Annotation> annotations;
//    private int startLine;
//    private int endLine;
    private int selectedIndex = -1;
    private CsmReference[] refs;

    MethodChooserSupport(GdbDebugger debugger, String url) {
        this.debugger = debugger;
//        this.currentThread = debugger.getCurrentThread();
        this.url = url;
//        this.clazzRef = clazz;
    }

    public int getSelectedIndex() {
        return selectedIndex;
    }

    public MethodChooser.Segment[] getSegments() {
        MethodChooser.Segment[] segments = new MethodChooser.Segment[refs.length];
        for (int x = 0; x < segments.length; x++) {
            int start = refs[x].getStartOffset();
            int end = refs[x].getEndOffset();
            segments[x] = new MethodChooser.Segment(start, end);
        }
        return segments;
    }

    public int getSegmentsCount() {
        return refs.length;
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
//        System.setProperty("org.netbeans.modules.debugger.jpda.doNotShowTooltips", "true"); // NOI18N
        this.chooser = selector;
        debugger.addPropertyChangeListener(this);
//        debugger.getThreadsCollector().addPropertyChangeListener(this);
        annotateLines();
    }

    public void tearDown() {
        // hack - enable org.netbeans.modules.debugger.jpda.projects.ToolTipAnnotation
//        System.clearProperty("org.netbeans.modules.debugger.jpda.doNotShowTooltips"); // NOI18N
        debugger.removePropertyChangeListener(this);
//        debugger.getThreadsCollector().removePropertyChangeListener(this);
        clearAnnotations();
    }

    public void doStepInto() {
        final CsmReference ref = refs[chooser.getSelectedIndex()];
        RequestProcessor.getDefault().post(new Runnable() {
            @Override
            public void run() {
                final CsmObject referencedObject = ref.getReferencedObject();
                if (CsmKindUtilities.isFunction(referencedObject)) {
                    debugger.until((CsmFunction)referencedObject);
                }
            }
        });
//        debugger.getRequestProcessor().post(new Runnable() {
//            public void run() {
//                RunIntoMethodActionProvider.doAction(debugger, name, locations[index], true);
//            }
//        });
    }

    public boolean init() {
        refs = new CsmReference[0];
        FunctionCallsProvider fcProvider = Lookup.getDefault().lookup(FunctionCallsProvider.class);

        if (fcProvider == null) {
            //System.out.println("No function calls provider - just step into");
            debugger.stepInto();
            return true;
        }
        List<CsmReference> functionCallsList = fcProvider.getFunctionCalls(
                debugger.getCurrentCallStackFrame().getDocument(),
                debugger.getCurrentCallStackFrame().getLineNumber()-1);

        refs = functionCallsList.toArray(new CsmReference[functionCallsList.size()]);
        selectedIndex = 0;

        if (refs.length == 0) {
            debugger.stepInto();
            return true;
        } else if (refs.length == 1) {
            // do not show UI, continue directly using the selection
            //String name = functionCalls[selectedIndex].name;

            debugger.stepInto();
            //RunIntoMethodActionProvider.doAction(debugger, name, locations[selectedIndex], true);
            return true;
        }
        return false;
    }

    private void annotateLines() {
//        annotations = new ArrayList<Annotation>();
//        EditorContext context = EditorContextBridge.getContext();
//        JPDAThread thread = debugger.getCurrentThread();
//        Operation currOp = thread.getCurrentOperation();
//        int currentLine = currOp != null ? currOp.getStartPosition().getLine() : thread.getLineNumber(null);
//        String annoType = currOp != null ?
//            EditorContext.CURRENT_EXPRESSION_CURRENT_LINE_ANNOTATION_TYPE :
//            EditorContext.CURRENT_LINE_ANNOTATION_TYPE;
//        for (int lineNum = startLine; lineNum <= endLine; lineNum++) {
//            if (lineNum != currentLine) {
//                Object anno = context.annotate(url, lineNum, annoType, null);
//                if (anno instanceof Annotation) {
//                    annotations.add((Annotation)anno);
//                }
//            } // if
//        } // for
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
        if (debugger.getState() == GdbDebugger.State.EXITED) {
            chooser.releaseUI(false);
        }
    }

    // **************************************************************************
    // inner classes
    // **************************************************************************
    
//    private static final class OperatorsComparator implements Comparator {
//
//        @Override
//        public int compare(Object o1, Object o2) {
//            Object[] a1 = (Object[])o1;
//            Object[] a2 = (Object[])o2;
//            Operation op1 = (Operation)a1[0];
//            Operation op2 = (Operation)a2[0];
//            return op1.getMethodStartPosition().getOffset() - op2.getMethodStartPosition().getOffset();
//        }
//
//    }
}
