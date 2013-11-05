/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.debugger.jpda.js.breakpoints;

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
import org.netbeans.modules.debugger.jpda.js.Context;
import org.netbeans.modules.debugger.jpda.js.JSUtils;
import org.netbeans.spi.debugger.DebuggerServiceRegistration;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.text.AnnotationProvider;
import org.openide.text.Line;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;

/**
 *
 * @author Martin
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.text.AnnotationProvider.class)
@DebuggerServiceRegistration(types=LazyDebuggerManagerListener.class)
public class JSBreakpointAnnotationListener extends DebuggerManagerAdapter
                                            implements AnnotationProvider {
    
    private static final Logger logger = Logger.getLogger(JSBreakpointAnnotationListener.class.getName());
    
    private final Map<JSBreakpoint, Object> breakpointAnnotations = new HashMap<>();
    private final Set<FileObject> annotatedFiles = new WeakSet<>();
    private Set<PropertyChangeListener> dataObjectListeners;
    private final RequestProcessor annotationProcessor = new RequestProcessor("Annotation Refresh", 1);

    @Override
    public String[] getProperties() {
        return new String[] { DebuggerManager.PROP_BREAKPOINTS, DebuggerManager.PROP_DEBUGGER_ENGINES }; 
    }
    
    private static boolean isAnnotateable(Breakpoint breakpoint) {
        return breakpoint instanceof JSBreakpoint;
    }
    
    private static boolean isAnnotateable(FileObject fo) {
        return JSUtils.JS_MIME_TYPE.equals(fo.getMIMEType());
    }

    @Override
    public void breakpointAdded(Breakpoint breakpoint) {
        if (!isAnnotateable(breakpoint)) {
            return;
        }
        JSBreakpoint b = (JSBreakpoint) breakpoint;
        FileObject fo = b.getFileObject();
        synchronized (breakpointAnnotations) {
            boolean isFileAnnotated = annotatedFiles.contains(fo);
            logger.log(Level.FINE, "breakpointAdded({0}), fo = {1}, foID = {2}, annotated = {3}",
                       new Object[] { breakpoint, fo, System.identityHashCode(fo), isFileAnnotated });
            //if (isFileAnnotated) {
                //addAnnotation(b);
                annotationProcessor.post(new AnnotationRefresh(b, false, true));
            //}
        }
        
    }

    @Override
    public void breakpointRemoved(Breakpoint breakpoint) {
        if (!isAnnotateable(breakpoint)) {
            return;
        }
        JSBreakpoint b = (JSBreakpoint) breakpoint;
        logger.log(Level.FINE, "breakpointRemoved({0})", breakpoint);
        annotationProcessor.post(new AnnotationRefresh(b, true, false));
        //removeAnnotation((JSBreakpoint) breakpoint);
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
                    dataObjectListeners = new HashSet<PropertyChangeListener>();
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
                    JSBreakpoint b = (JSBreakpoint) breakpoint;
                    if (fo.equals(b.getFileObject())) {
                        logger.log(Level.FINE, "annotate({0} (ID={1})): b = {2}",
                                   new Object[] { fo, System.identityHashCode(fo), b });
                        annotationProcessor.post(new AnnotationRefresh(b, false, true));
                    }
                    /*
                    int[] lines = getAnnotationLines(b, fo);
                    if (lines != null && lines.length > 0) {
                        if (!annotatedBreakpoints.contains(b)) {
                            b.addPropertyChangeListener (this);
                            breakpointToAnnotations.put(b, new WeakSet<Annotation>());
                            if (b instanceof LineBreakpoint) {
                                LineBreakpoint lb = (LineBreakpoint) b;
                                LineTranslations.getTranslations().registerForLineUpdates(lb);
                            }
                        }
                        addAnnotationTo(b, fo, lines);
                    }
                    */
                }
            }
            annotatedFiles.add(fo);
            logger.log(Level.FINE, "Annotated files = {0}", annotatedFiles);
        }
//        if (attachManagerListener) {
//            attachManagerListener = false;
//            setCurrentDebugger(DebuggerManager.getDebuggerManager().getCurrentEngine());
//            DebuggerManager.getDebuggerManager().addDebuggerListener(
//                    WeakListeners.create(DebuggerManagerListener.class,
//                                         this,
//                                         DebuggerManager.getDebuggerManager()));
//        }
    }

    private void addAnnotation(JSBreakpoint breakpoint) {
        Object annotation = Context.annotate(((JSBreakpoint) breakpoint));
        logger.log(Level.FINE, "Added annotation of {0} : {1}",
                   new Object[] { breakpoint, annotation });
        if (annotation != null) {
            synchronized (breakpointAnnotations) {
                breakpointAnnotations.put(breakpoint, annotation);
            }
        }
    }

    private void removeAnnotation(JSBreakpoint breakpoint) {
        Object annotation;
        synchronized (breakpointAnnotations) {
            annotation = breakpointAnnotations.remove(breakpoint);
        }
        if (annotation != null) {
            logger.log(Level.FINE, "Removed annotation of {0} : {1}",
                       new Object[] { breakpoint, annotation });
            Context.removeAnnotation(annotation);
        }
    }
    
    private final class AnnotationRefresh implements Runnable {
        
        private final JSBreakpoint b;
        private final boolean remove, add;
        
        public AnnotationRefresh(JSBreakpoint b, boolean remove, boolean add) {
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
                    List<JSBreakpoint> bpts = new ArrayList<>(breakpointAnnotations.keySet());
                    for (JSBreakpoint bp : bpts) {
                        refreshAnnotation(bp);
                    }
                }
            }
        }
        
        private void refreshAnnotation(JSBreakpoint b) {
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
