/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.web.client.javascript.debugger.ui;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.modules.web.client.javascript.debugger.api.NbJSDebugger;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSCallStackFrame;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerEvent;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerEventListener;
import org.netbeans.modules.web.client.tools.javascript.debugger.api.JSDebuggerState;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.openide.util.WeakListeners;

public final class NbJSCallStackAnnotationListener extends
        DebuggerManagerAdapter implements JSDebuggerEventListener {

    @Override
    public String[] getProperties() {
        return new String[] { DebuggerManager.PROP_DEBUGGER_ENGINES };
    }

    public void onDebuggerEvent(JSDebuggerEvent debuggerEvent) {
        NbJSDebugger nbJSDebugger = (NbJSDebugger) debuggerEvent.getSource();
        JSDebuggerState jsDebuggerState = debuggerEvent.getDebuggerState();
        if ( nbJSDebugger != null && jsDebuggerState == null  ){
            NbJSUtil.severe("STATE SHOULD NOT BE NULL");
            return;
        }
        switch (jsDebuggerState.getState()) {
        case SUSPENDED:
            JSCallStackFrame[] frames = nbJSDebugger.getCallStackFrames();
            clearAnnotations();
            addAnnotations(nbJSDebugger, frames);
            JSCallStackFrame selectedFrame = nbJSDebugger.getSelectedFrame();
            if (selectedFrame == null) {
                return;
            }
            annotateCurrentLineAnnotation(nbJSDebugger, selectedFrame);
            break;
        case RUNNING:
        case DISCONNECTED:
            clearAnnotations();
            removeCurrentLineAnnotation();
            break;
        default:
            break;
        }
    }

    private final void addAnnotations(NbJSDebugger debugger,
            final JSCallStackFrame[] frames) {
        for (JSCallStackFrame frame : frames) {
            addAnnotation(debugger, frame);
        }
    }

    private final static void clearAnnotations() {

        Collection<JSCallStackFrame> frameKeys = new HashSet<JSCallStackFrame>(
                callStackToAnnotation.keySet());
        for (JSCallStackFrame frame : frameKeys) {
            removeAnnotation(frame);
        }
    }

    private final static Map<JSCallStackFrame, Annotation> callStackToAnnotation = new HashMap<JSCallStackFrame, Annotation>();

    private final static void addAnnotation(NbJSDebugger debugger,
            final JSCallStackFrame frame) {
        Line line = NbJSEditorUtil.getLine(debugger, frame);
        if (line != null) {
            Annotation debugAnnotation = new NbJSDebuggerAnnotation(
                    NbJSEditorUtil.CALL_STACK_FRAME_ANNOTATION_TYPE,
                    line);
            callStackToAnnotation.put(frame, debugAnnotation);
        }
    }
    
    private static NbJSDebuggerAnnotation currentLineAnnotation;
    private final static void annotateCurrentLineAnnotation(
            NbJSDebugger debugger, final JSCallStackFrame currentFrame) {
        
        if( currentLineAnnotation != null){
            currentLineAnnotation.detach();
        }
        Line line = NbJSEditorUtil.getLine(debugger, currentFrame);
        if (line != null) {
            currentLineAnnotation = new NbJSDebuggerAnnotation(
                    NbJSEditorUtil.CURRENT_LINE_ANNOTATION_TYPE, line);
            NbJSEditorUtil.showLine(line);
        }
    }
    
    private final static void removeCurrentLineAnnotation(){
        if ( currentLineAnnotation != null){
            currentLineAnnotation.detach();
            currentLineAnnotation = null;
        }
    }

    private final static void removeAnnotation(final JSCallStackFrame frame) {
        Annotation annotation = callStackToAnnotation.remove(frame);
        if (annotation == null)
            return;
        annotation.detach();
    }

    /* Joelle: Talk to Sandip about this. Is it okay to create a static listener for this.*/
    private final static NbJSCallStackAnnotationListener annotationListener = new NbJSCallStackAnnotationListener();
    @Override
    public void engineAdded(DebuggerEngine engine) {
        /* Add all breakpoints to this engine */
        NbJSDebugger debugger = engine.lookupFirst(null, NbJSDebugger.class);
        if (debugger == null) {
            return;
        }
        debugger.addJSDebuggerEventListener( WeakListeners.create(JSDebuggerEventListener.class, annotationListener,debugger));
        super.engineAdded(engine);
    }

    @Override
    public void engineRemoved(DebuggerEngine engine) {
        NbJSDebugger debugger = engine.lookupFirst(null, NbJSDebugger.class);
        if (debugger == null) {
            return;
        }
        debugger.removeJSDebuggerEventListener(annotationListener);
        /* I don't think I need to remove the annotation because it is closing. */
        super.engineRemoved(engine);
    }

}
