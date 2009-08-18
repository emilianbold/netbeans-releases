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

package org.netbeans.modules.cnd.debugger.gdb;

import org.netbeans.modules.cnd.debugger.common.EditorContextBridge;
import java.beans.PropertyChangeEvent;
import java.util.*;

import javax.swing.SwingUtilities;
import org.netbeans.api.debugger.*;

import org.openide.text.Annotation;
import org.openide.util.RequestProcessor;


/**
 * Listens on {@link org.netbeans.api.debugger.DebuggerManager}
 * property and annotates current line and call stack in NetBeans editor.
 *
 * @author Gordon Prieur (copied from Jan Jancura's JPDA implementation)
 */
public class CurrentThreadAnnotationListener extends DebuggerManagerAdapter {
    private GdbDebugger                     currentDebugger;

    public CurrentThreadAnnotationListener() {
	updateCurrentDebugger(); // ensure currentDebugger gets set
	assert (!(currentDebugger != null && Boolean.getBoolean("gdb.assertions.enabled"))); // NOI18N
    }

    @Override
    public String[] getProperties() {
        return new String[] {DebuggerManager.PROP_CURRENT_ENGINE};
    }

    /**
     * Listens GdbDebuggerEngineImpl and DebuggerManager.
     */
    @Override
    public void propertyChange(PropertyChangeEvent e) {
        if (e.getPropertyName().equals(DebuggerManager.PROP_CURRENT_ENGINE)) {
            updateCurrentDebugger();
            annotate(false);
        } else if (e.getPropertyName().equals(GdbDebugger.PROP_CURRENT_THREAD)) {
            annotate(false);
        } else if (e.getPropertyName().equals(GdbDebugger.PROP_CURRENT_CALL_STACK_FRAME)) {
            annotate(false);
        } else if (e.getPropertyName().equals(GdbDebugger.PROP_STATE)) {
            annotate(false);
        } else if (e.getPropertyName().equals(GdbDebugger.DIS_UPDATE)) {
            annotate((Boolean)e.getOldValue());
        } 
    }


    // helper methods ..........................................................

    private void updateCurrentDebugger() {
        GdbDebugger newDebugger = GdbDebugger.getGdbDebugger();
        if (currentDebugger == newDebugger) {
            return;
        }
        if (currentDebugger != null) {
            currentDebugger.removePropertyChangeListener(this);
        }
        if (newDebugger != null) {
            newDebugger.addPropertyChangeListener(this);
        }
        currentDebugger = newDebugger;
    }

    /**
     * Annotates current thread or removes annotations.
     */
    private void annotate(boolean dis) {
        if (currentDebugger == null) {
            return;
        }
        
        // 1) no current thread => remove annotations
        if (!currentDebugger.isStopped()) {
            removeAnnotations();
            return;
        }
        
        // 2) show current place
        currentDebugger.showCurrentSource(dis);

        // 3) annotate current line & stack
        annotateCallStack(currentDebugger.getCallStack());
    }


    // do not need synchronization, called in a 1-way RP
    private final Collection<Annotation> stackAnnotations = new LinkedList<Annotation>();
    
    // this set is used to avoid duplicated annotations (of the same line)
    private final Set<String> annotatedAddresses = new HashSet<String>();
    
    private final RequestProcessor rp = new RequestProcessor("Debugger Thread Annotation Refresher"); // NOI18N

    // currently waiting / running refresh task
    // there is at most one
    private RequestProcessor.Task taskRemove;
    private RequestProcessor.Task taskAnnotate;
    private List<GdbCallStackFrame> stackToAnnotate;

    private void removeAnnotations() {
        synchronized (rp) {
            if (taskRemove == null) {
                taskRemove = rp.create(new Runnable() {
                    public void run() {
                        clearAnnotations();
                    }
                });
            }
        }
        taskRemove.schedule(500);
    }
    
    private void clearAnnotations() {
        for (Annotation ann : stackAnnotations) {
            EditorContextBridge.removeAnnotation(ann);
        }
        stackAnnotations.clear();
        annotatedAddresses.clear();
    }

    private void annotateCallStack(List<GdbCallStackFrame> stack) {
        synchronized (rp) {
            if (taskRemove != null) {
                taskRemove.cancel();
            }
            this.stackToAnnotate = stack;
            //this.sourcePathToAnnotate = sourcePath;
            if (taskAnnotate == null) {
                taskAnnotate = rp.post(new Runnable() {
                    public void run() {
                        List<GdbCallStackFrame> stack;
                        synchronized (rp) {
                            if (stackToAnnotate == null) {
                                return ; // Nothing to do
                            }
                            stack = new ArrayList<GdbCallStackFrame>(stackToAnnotate);
                            stackToAnnotate = null;
                        }
                        
                        // Remove old annotations
                        clearAnnotations();
                        
                        // Add new annotations
                        String annotationType = EditorContext.CURRENT_LINE_ANNOTATION_TYPE;
                        for (GdbCallStackFrame csf : stack) {
                            // 1) Is current stackFrame annotated
                            if (!annotatedAddresses.add(csf.getAddr())) {
                                continue;
                            }
                            
                            // 2) annotate line
                            final Annotation da = EditorContextBridge.annotate(csf, annotationType);

                            // 3) add new frame to set and bring to front
                            if (da != null) {
                                stackAnnotations.add(da);
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        da.moveToFront();
                                    }
                                });
                            }
                            
                            // 4) annotate dis
                            final Annotation disa = EditorContextBridge.annotateDis(csf, annotationType);
                            
                            // 5) add new dis line to hashMap
                            if (disa != null) {
                                stackAnnotations.add(disa);
                            }
                            
                            annotationType = EditorContext.CALL_STACK_FRAME_ANNOTATION_TYPE;
                        }
                    }
                });
            }
        }
        taskAnnotate.schedule(50);
    }
}

