/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.ruby.debugger.ui;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import org.netbeans.modules.ruby.debugger.DebuggerAnnotation;
import org.netbeans.modules.ruby.debugger.EditorUtil;
import org.netbeans.modules.ruby.debugger.model.CallSite;
import org.openide.util.RequestProcessor;

public final class CallStackAnnotation {

    private static Map<CallSite, DebuggerAnnotation[]> stackAnnotations;
    private static CallSite[] callSites;

    private static RequestProcessor rp = new RequestProcessor("Ruby Debugger CallStack Annotation Refresher");
    private static RequestProcessor.Task taskRemove;
    private static RequestProcessor.Task taskAnnotate;

    private static DebuggerAnnotation[] annotateCallSite(final CallSite site) {
        Object line = EditorUtil.getLineAnnotable(site.getPath(), site.getLine());
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
                        Collection<DebuggerAnnotation[]> ansToRemove = Collections.emptySet();
                        synchronized (rp) {
                            if (stackAnnotations != null) {
                                ansToRemove = new HashSet<DebuggerAnnotation[]>(stackAnnotations.values());
                                stackAnnotations.clear();
                                stackAnnotations = null;
                            }
                        }
                        CallStackAnnotation.removeAnnotations(ansToRemove);
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
                        Collection <DebuggerAnnotation[]> annsToRemove = Collections.emptySet();
                        synchronized (rp) {
                            Map<CallSite, DebuggerAnnotation[]> newAnnotations = new HashMap<CallSite, DebuggerAnnotation[]>();
                            CallSite[] callSites = CallStackAnnotation.callSites;
                            if (stackAnnotations == null) {
                                stackAnnotations = new HashMap<CallSite, DebuggerAnnotation[]>();
                            }
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
                            annsToRemove = new HashSet<DebuggerAnnotation[]>(stackAnnotations.values());
                            stackAnnotations = newAnnotations;
                        }
                        // delete old anotations
                        removeAnnotations(annsToRemove);
                    }
                });
            }
        }
        taskAnnotate.schedule(500);
    }
    
    private static void removeAnnotations(Collection<DebuggerAnnotation[]> annsToRemove) {
        for (DebuggerAnnotation[] ann : annsToRemove) {
            EditorUtil.removeAnnotation(ann);
        }
    }
    
}
