/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.ruby.debugger.ui;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.modules.ruby.debugger.DebuggerAnnotation;
import org.netbeans.modules.ruby.debugger.EditorUtil;
import org.netbeans.modules.ruby.debugger.model.CallSite;
import org.openide.util.RequestProcessor;

/**
 * @author Martin Krauskopf
 */
public final class CallStackAnnotation {

    private static Map<CallSite, DebuggerAnnotation[]> stackAnnotations;
    private static CallSite[] callSites;

    private static RequestProcessor rp = new RequestProcessor("Ruby Debugger CallStack Annotation Refresher");
    private static RequestProcessor.Task taskRemove;
    private static RequestProcessor.Task taskAnnotate;

    private static DebuggerAnnotation[] annotateCallSite(final CallSite site) {
        Object line = EditorUtil.getLine(site.getPath(), site.getLine());
        if (line == null) {
            return null;
        }
        DebuggerAnnotation[] annotations = EditorUtil.createDebuggerAnnotation(
                line, DebuggerAnnotation.CALL_STACK_FRAME_ANNOTATION_TYPE);
        return annotations;
    }
    
    public static void clearAnnotations() {
        synchronized (rp) {
            if (taskRemove == null) {
                taskRemove = rp.create(new Runnable() {
                    public void run() {
                        for (DebuggerAnnotation[] ann : stackAnnotations.values()) {
                            EditorUtil.removeAnnotation(ann);
                        }
                        synchronized(rp) {
                            stackAnnotations.clear();
                            stackAnnotations = null;
                        }
                    }
                });
            }
        }
        taskRemove.schedule(500);
    }

    public static void annotate(final CallSite[] callSites) {
        synchronized (rp) {
            if (taskRemove != null) {
                taskRemove.cancel();
            }
            CallStackAnnotation.callSites = callSites;
            if (taskAnnotate == null) {
                taskAnnotate = rp.post(new Runnable() {
                    public void run() {
                        CallSite[] callSites;
                        synchronized (rp) {
                            callSites = CallStackAnnotation.callSites;
                            if (stackAnnotations == null) {
                                stackAnnotations = new HashMap<CallSite, DebuggerAnnotation[]>();
                            }
                        }
                        Map<CallSite, DebuggerAnnotation[]> newAnnotations = new HashMap<CallSite, DebuggerAnnotation[]>();
                        for (int i = 0; i < callSites.length; i++) {
                            CallSite site = callSites[i];
                            if (newAnnotations.containsKey(site)) {
                                continue;
                            }
                            DebuggerAnnotation[] annotation = stackAnnotations.remove(site);
                            if (annotation == null) {
                                // line has not been annotated -> annotate
                                annotation = annotateCallSite(site);
                            }
                            if (annotation != null) {
                                newAnnotations.put(site, annotation);
                            }
                        }
                        // delete old anotations
                        for (DebuggerAnnotation[] ann : stackAnnotations.values()) {
                            EditorUtil.removeAnnotation(ann);
                        }
                        stackAnnotations = newAnnotations;
                    }
                });
            }
        }
        taskAnnotate.schedule(500);
    }

}
