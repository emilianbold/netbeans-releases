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

package org.netbeans.modules.javascript2.debug.annotation;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;
import org.netbeans.api.debugger.LazyDebuggerManagerListener;
import org.netbeans.modules.javascript2.debug.breakpoints.JSBreakpointsInfo;
import org.netbeans.modules.javascript2.debug.breakpoints.JSBreakpointsInfoManager;
import org.netbeans.modules.javascript2.debug.breakpoints.JSLineBreakpoint;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.AnnotationProvider;
import org.openide.text.Line;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;


/**
 * Listens on {@org.netbeans.api.debugger.DebuggerManager} on
 * {@link org.netbeans.api.debugger.DebuggerManager#PROP_BREAKPOINTS}
 * property and annotates Debugger line breakpoints in NetBeans editor.
 *
 * @author ads
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.text.AnnotationProvider.class)
@DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
public class BreakpointAnnotationListener extends DebuggerManagerAdapter
    implements PropertyChangeListener, AnnotationProvider
{
    
    private static final Logger logger = Logger.getLogger(BreakpointAnnotationListener.class.getName());
    
    private final Map<JSLineBreakpoint, Annotation> breakpointAnnotations = new HashMap<>();
    private final Set<FileObject> annotatedFiles = new WeakSet<>();
    private Set<PropertyChangeListener> dataObjectListeners;
    private final RequestProcessor annotationProcessor = new RequestProcessor("Annotation Refresh", 1);
    private boolean active = true;
    
    public BreakpointAnnotationListener() {
        JSBreakpointsInfoManager.getDefault().addPropertyChangeListener(this);
        active = JSBreakpointsInfoManager.getDefault().areBreakpointsActivated();
    }

    @Override
    public String[] getProperties() {
        return new String[] { DebuggerManager.PROP_BREAKPOINTS }; 
    }
    
    private static boolean isAnnotateable(Breakpoint breakpoint) {
        return breakpoint instanceof JSLineBreakpoint;
    }
    
    private static boolean isAnnotateable(FileObject fo) {
        return JSBreakpointsInfoManager.getDefault().isAnnotatable(fo);
    }

    @Override
    public void annotate(Line.Set set, Lookup context) {
        final FileObject fo = context.lookup(FileObject.class);
        if (fo == null || !isAnnotateable(fo)) {
            return ;
        }
        DataObject dobj = context.lookup(DataObject.class);
        logger.log(Level.FINE, "annotate({0}, {1}), fo = {2}, foID = {3}, dobj = {4}",
                   new Object[] { set, context, fo, System.identityHashCode(fo), dobj });
        if (dobj != null) {
            PropertyChangeListener pchl = new PropertyChangeListener() {
                /** annotate renamed files. */
                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if (DataObject.PROP_PRIMARY_FILE.equals(evt.getPropertyName())) {
                        DataObject dobj = (DataObject) evt.getSource();
                        final FileObject newFO = dobj.getPrimaryFile();
                        annotationProcessor.post(new Runnable() {
                            @Override
                            public void run() {
                                annotate(newFO);
                            }
                        });
                    }
                }
            };
            dobj.addPropertyChangeListener(WeakListeners.propertyChange(pchl, dobj));
            synchronized (this) {
                if (dataObjectListeners == null) {
                    dataObjectListeners = new HashSet<>();
                }
                // Prevent from GC.
                dataObjectListeners.add(pchl);
            }
        }
        annotate(fo);
    }
    
    public void annotate (final FileObject fo) {
        synchronized (breakpointAnnotations) {
//            if (annotatedFiles.contains(fo)) {
//                // Already annotated
//                return ;
//            }
            //Set<JSBreakpoint> annotatedBreakpoints = breakpointAnnotations.keySet();
            for (Breakpoint breakpoint : DebuggerManager.getDebuggerManager().getBreakpoints()) {
                if (isAnnotateable(breakpoint)) {
                    JSLineBreakpoint b = (JSLineBreakpoint) breakpoint;
                    if (fo.equals(b.getFileObject())) {
                        logger.log(Level.FINE, "annotate({0} (ID={1})): b = {2}",
                                   new Object[] { fo, System.identityHashCode(fo), b });
                        b.addPropertyChangeListener(this);
                        annotationProcessor.post(new AnnotationRefresh(b, false, true));
                    }
                }
            }
            annotatedFiles.add(fo);
            logger.log(Level.FINE, "Annotated files = {0}", annotatedFiles);
        }
    }
    
    @Override
    public void breakpointAdded(Breakpoint breakpoint) {
        if (!isAnnotateable(breakpoint)) {
            return;
        }
        JSLineBreakpoint lb = (JSLineBreakpoint) breakpoint;
        FileObject fo = lb.getFileObject();
        synchronized (breakpointAnnotations) {
            boolean isFileAnnotated = annotatedFiles.contains(fo);
            logger.log(Level.FINE, "breakpointAdded({0}), fo = {1}, foID = {2}, annotated = {3}",
                       new Object[] { breakpoint, fo, System.identityHashCode(fo), isFileAnnotated });
            //if (isFileAnnotated) {
                lb.addPropertyChangeListener(this);
                annotationProcessor.post(new AnnotationRefresh(lb, false, true));
            //}
        }
    }

    @Override
    public void breakpointRemoved(Breakpoint breakpoint) {
        if (!isAnnotateable(breakpoint)) {
            return;
        }
        JSLineBreakpoint lb = (JSLineBreakpoint) breakpoint;
        logger.log(Level.FINE, "breakpointRemoved({0})", breakpoint);
        lb.removePropertyChangeListener(this);
        annotationProcessor.post(new AnnotationRefresh(lb, true, false));
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (Breakpoint.PROP_ENABLED.equals(propertyName) ||
            JSLineBreakpoint.PROP_LINE.equals(propertyName) ||
            Breakpoint.PROP_VALIDITY.equals(propertyName)) {
            
            JSLineBreakpoint lb = (JSLineBreakpoint) evt.getSource();
            removeAnnotation(lb);
            addAnnotation(lb);
        } else if (JSBreakpointsInfo.PROP_BREAKPOINTS_ACTIVE.equals(propertyName)) {
            boolean a = JSBreakpointsInfoManager.getDefault().areBreakpointsActivated();
            if (a != active) {
                active = a;
                annotationProcessor.post(new AnnotationRefresh(null, true, true));
            }
        }
    }

    private void addAnnotation(JSLineBreakpoint breakpoint) {
        Line line = breakpoint.getLine();
        Annotation annotation = new LineBreakpointAnnotation(line, (JSLineBreakpoint) breakpoint, active);
        logger.log(Level.FINE, "Added annotation of {0} : {1}",
                   new Object[] { breakpoint, annotation });
        synchronized (breakpointAnnotations) {
            breakpointAnnotations.put(breakpoint, annotation);
        }
    }

    private void removeAnnotation(Breakpoint breakpoint) {
        Annotation annotation;
        synchronized (breakpointAnnotations) {
            annotation = breakpointAnnotations.remove(breakpoint);
        }
        if (annotation != null) {
            logger.log(Level.FINE, "Removed annotation of {0} : {1}",
                       new Object[] { breakpoint, annotation });
            annotation.detach();
        }
    }
    
    private final class AnnotationRefresh implements Runnable {
        
        private final JSLineBreakpoint b;
        private final boolean remove, add;
        
        public AnnotationRefresh(JSLineBreakpoint b, boolean remove, boolean add) {
            this.b = b;
            this.remove = remove;
            this.add = add;
        }

        @Override
        public void run() {
            synchronized (breakpointAnnotations) {
                if (b != null) {
                    refreshAnnotation(b);
                } else {
                    List<JSLineBreakpoint> bpts = new ArrayList<>(breakpointAnnotations.keySet());
                    for (JSLineBreakpoint bp : bpts) {
                        refreshAnnotation(bp);
                    }
                }
            }
        }
        
        private void refreshAnnotation(JSLineBreakpoint b) {
            removeAnnotation(b);
            /*if (remove) {
                if (!add) {
                    breakpointAnnotations.remove(b);
                }
            }*/
            if (add) {
                addAnnotation(b);
                /*breakpointAnnotations.put(b, new WeakSet<Annotation>());
                for (FileObject fo : annotatedFiles) {
                    addAnnotationTo(b, fo);
                }*/
            }
        }
        
    }

}
