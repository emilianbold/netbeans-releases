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

package org.netbeans.modules.debugger.jpda.projects;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Breakpoint.VALIDITY;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
import org.netbeans.api.debugger.jpda.ClassLoadUnloadBreakpoint;
import org.netbeans.api.debugger.jpda.FieldBreakpoint;
import org.netbeans.api.debugger.jpda.JPDABreakpoint;
import org.netbeans.api.debugger.jpda.LineBreakpoint;
import org.netbeans.api.debugger.jpda.MethodBreakpoint;
import org.netbeans.spi.debugger.jpda.EditorContext;

import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Annotation;
import org.openide.text.AnnotationProvider;
import org.openide.text.Line;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;


/**
 * This class is called when some file in editor is openend. It changes if
 * some LineBreakpoints with annotations should be readed.
 *
 * @author Jan Jancura, Martin Entlicher
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.text.AnnotationProvider.class)
public class BreakpointAnnotationProvider implements AnnotationProvider,
                                                     DebuggerManagerListener {

    private final Map<JPDABreakpoint, Set<Annotation>> breakpointToAnnotations =
            new IdentityHashMap<JPDABreakpoint, Set<Annotation>>();
    private final Set<FileObject> annotatedFiles = new WeakSet<FileObject>();
    private Set<PropertyChangeListener> dataObjectListeners;
    private boolean attachManagerListener = true;
    private RequestProcessor annotationProcessor = new RequestProcessor("Annotation Refresh", 1);

    public void annotate (Line.Set set, Lookup lookup) {
        final FileObject fo = lookup.lookup(FileObject.class);
        if (fo != null) {
            DataObject dobj = lookup.lookup(DataObject.class);
            if (dobj != null) {
                PropertyChangeListener pchl = new PropertyChangeListener() {
                    /** annotate renamed files. */
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
    }
    
    public void annotate (final FileObject fo) {
        synchronized (breakpointToAnnotations) {
            if (annotatedFiles.contains(fo)) {
                // Already annotated
                return ;
            }
            Set<JPDABreakpoint> annotatedBreakpoints = breakpointToAnnotations.keySet();
            for (Breakpoint breakpoint : DebuggerManager.getDebuggerManager().getBreakpoints()) {
                if (isAnnotatable(breakpoint)) {
                    JPDABreakpoint b = (JPDABreakpoint) breakpoint;
                    if (!annotatedBreakpoints.contains(b)) {
                        b.addPropertyChangeListener (this);
                        breakpointToAnnotations.put(b, new WeakSet<Annotation>());
                        if (b instanceof LineBreakpoint) {
                            LineBreakpoint lb = (LineBreakpoint) b;
                            LineTranslations.getTranslations().registerForLineUpdates(lb);
                        }
                    }
                    addAnnotationTo(b, fo);
                }
            }
            annotatedFiles.add(fo);
        }
        if (attachManagerListener) {
            attachManagerListener = false;
            DebuggerManager.getDebuggerManager().addDebuggerListener(
                    WeakListeners.create(DebuggerManagerListener.class,
                                         this,
                                         DebuggerManager.getDebuggerManager()));
        }
    }

    public void breakpointAdded(Breakpoint breakpoint) {
        if (isAnnotatable(breakpoint)) {
            JPDABreakpoint b = (JPDABreakpoint) breakpoint;
            b.addPropertyChangeListener (this);
            annotationProcessor.post(new AnnotationRefresh(b, false, true));
            if (b instanceof LineBreakpoint) {
                LineBreakpoint lb = (LineBreakpoint) b;
                LineTranslations.getTranslations().registerForLineUpdates(lb);
            }
        }
    }

    public void breakpointRemoved(Breakpoint breakpoint) {
        if (isAnnotatable(breakpoint)) {
            JPDABreakpoint b = (JPDABreakpoint) breakpoint;
            b.removePropertyChangeListener (this);
            annotationProcessor.post(new AnnotationRefresh(b, true, false));
            if (b instanceof LineBreakpoint) {
                LineBreakpoint lb = (LineBreakpoint) b;
                LineTranslations.getTranslations().unregisterFromLineUpdates(lb);
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName ();
        if (propertyName == null) return;
        if ( (!JPDABreakpoint.PROP_ENABLED.equals (propertyName)) &&
             (!JPDABreakpoint.PROP_VALIDITY.equals (propertyName)) &&
             (!LineBreakpoint.PROP_CONDITION.equals (propertyName)) &&
             (!LineBreakpoint.PROP_URL.equals (propertyName)) &&
             (!LineBreakpoint.PROP_LINE_NUMBER.equals (propertyName)) &&
             (!FieldBreakpoint.PROP_CLASS_NAME.equals (propertyName)) &&
             (!FieldBreakpoint.PROP_FIELD_NAME.equals (propertyName)) &&
             (!MethodBreakpoint.PROP_CLASS_FILTERS.equals (propertyName)) &&
             (!MethodBreakpoint.PROP_CLASS_EXCLUSION_FILTERS.equals (propertyName)) &&
             (!MethodBreakpoint.PROP_METHOD_NAME.equals (propertyName)) &&
             (!MethodBreakpoint.PROP_METHOD_SIGNATURE.equals (propertyName))
        ) return;
        JPDABreakpoint b = (JPDABreakpoint) evt.getSource ();
        DebuggerManager manager = DebuggerManager.getDebuggerManager();
        Breakpoint[] bkpts = manager.getBreakpoints();
        boolean found = false;
        for (int x = 0; x < bkpts.length; x++) {
            if (b == bkpts[x]) {
                found = true;
                break;
            }
        }
        if (!found) {
            // breakpoint has been removed
            return;
        }
        annotationProcessor.post(new AnnotationRefresh(b, true, true));
    }
    
    private final class AnnotationRefresh implements Runnable {
        
        private JPDABreakpoint b;
        private boolean remove, add;
        
        public AnnotationRefresh(JPDABreakpoint b, boolean remove, boolean add) {
            this.b = b;
            this.remove = remove;
            this.add = add;
        }

        public void run() {
            synchronized (breakpointToAnnotations) {
                if (remove) {
                    removeAnnotations(b);
                    if (!add) breakpointToAnnotations.remove(b);
                }
                if (add) {
                    breakpointToAnnotations.put(b, new WeakSet<Annotation>());
                    for (FileObject fo : annotatedFiles) {
                        addAnnotationTo(b, fo);
                    }
                }
            }
        }
        
    }
    
    private static boolean isAnnotatable(Breakpoint b) {
        return (b instanceof LineBreakpoint ||
                b instanceof FieldBreakpoint ||
                b instanceof MethodBreakpoint ||
                b instanceof ClassLoadUnloadBreakpoint) &&
               !((JPDABreakpoint) b).isHidden();
    }
    
    private static String getAnnotationType(JPDABreakpoint b, boolean isConditional) {
        boolean isInvalid = b.getValidity() == VALIDITY.INVALID;
        String annotationType;
        if (b instanceof LineBreakpoint) {
            annotationType = b.isEnabled () ?
            (isConditional ? EditorContext.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE :
                             EditorContext.BREAKPOINT_ANNOTATION_TYPE) :
            (isConditional ? EditorContext.DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE :
                             EditorContext.DISABLED_BREAKPOINT_ANNOTATION_TYPE);
        } else if (b instanceof FieldBreakpoint) {
            annotationType = b.isEnabled () ?
                EditorContext.FIELD_BREAKPOINT_ANNOTATION_TYPE :
                EditorContext.DISABLED_FIELD_BREAKPOINT_ANNOTATION_TYPE;
        } else if (b instanceof MethodBreakpoint) {
            annotationType = b.isEnabled () ?
                EditorContext.METHOD_BREAKPOINT_ANNOTATION_TYPE :
                EditorContext.DISABLED_METHOD_BREAKPOINT_ANNOTATION_TYPE;
        } else if (b instanceof ClassLoadUnloadBreakpoint) {
            annotationType = b.isEnabled() ?
                "ClassBreakpoint" :
                "DisabledClassBreakpoint";
        } else {
            throw new IllegalStateException(b.toString());
        }
        if (isInvalid && b.isEnabled ()) annotationType += "_broken";
        return annotationType;
    }
    
    /** @return The annotation lines or <code>null</code>. */
    private static int[] getAnnotationLines(JPDABreakpoint b, FileObject fo) {
        if (b instanceof LineBreakpoint) {
            LineBreakpoint lb = (LineBreakpoint) b;
            try {
                if (fo.getURL().equals(new URL(lb.getURL()))) {
                    return new int[] { lb.getLineNumber() };
                }
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            } catch (FileStateInvalidException ex) {
                Exceptions.printStackTrace(ex);
            }
            return null;
        } else if (b instanceof FieldBreakpoint) {
            FieldBreakpoint fb = (FieldBreakpoint) b;
            String className = fb.getClassName();
            String fieldName = fb.getFieldName();
            Future<Integer> fi = EditorContextImpl.getFieldLineNumber(fo, className, fieldName);
            int line;
            if (fi != null) {
                try {
                    line = fi.get();
                } catch (InterruptedException ex) {
                    return null;
                } catch (ExecutionException ex) {
                    Exceptions.printStackTrace(ex);
                    return null;
                }
            } else {
                return null;
            }
            return new int[] { line };
        } else if (b instanceof MethodBreakpoint) {
            MethodBreakpoint mb = (MethodBreakpoint) b;
            String[] filters = mb.getClassFilters();
            int[] lns = new int[] {};
            for (int i = 0; i < filters.length; i++) {
                // TODO: annotate also other matched classes
                if (!filters[i].startsWith("*") && !filters[i].endsWith("*")) {
                    Future<int[]> futurelns = EditorContextImpl.getMethodLineNumbers(
                            fo, filters[i], mb.getClassExclusionFilters(),
                            mb.getMethodName(),
                            mb.getMethodSignature());
                    int[] newlns;
                    if (futurelns != null) {
                        try {
                            newlns = futurelns.get();
                            if (lns.length == 0) {
                                lns = newlns;
                            } else {
                                int[] ln = new int[lns.length + newlns.length];
                                System.arraycopy(lns, 0, ln, 0, lns.length);
                                System.arraycopy(newlns, 0, ln, lns.length, newlns.length);
                                lns = ln;
                            }
                        } catch (InterruptedException ex) {
                        } catch (ExecutionException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
            return lns;
        } else if (b instanceof ClassLoadUnloadBreakpoint) {
            ClassLoadUnloadBreakpoint cb = (ClassLoadUnloadBreakpoint) b;
            String[] filters = cb.getClassFilters();
            int[] lns = new int[] {};
            for (int i = 0; i < filters.length; i++) {
                // TODO: annotate also other matched classes
                if (!filters[i].startsWith("*") && !filters[i].endsWith("*")) {
                    Future<Integer> futurelns = EditorContextImpl.getClassLineNumber(
                            fo, filters[i], cb.getClassExclusionFilters());
                    Integer newline;
                    if (futurelns != null) {
                        try {
                            newline = futurelns.get();
                            if (newline == null) {
                                continue;
                            }
                            if (lns.length == 0) {
                                lns = new int[] { newline };
                            } else {
                                int[] ln = new int[lns.length + 1];
                                System.arraycopy(lns, 0, ln, 0, lns.length);
                                ln[lns.length] = newline;
                                lns = ln;
                            }
                        } catch (InterruptedException ex) {
                        } catch (ExecutionException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
            return lns;
        } else {
            throw new IllegalStateException(b.toString());
        }
    }
    
    // Is called under synchronized (breakpointToAnnotations)
    private void addAnnotationTo(JPDABreakpoint b, FileObject fo) {
        int[] lines = getAnnotationLines(b, fo);
        if (lines == null || lines.length == 0) {
            return ;
        }
        String condition;
        if (b instanceof LineBreakpoint) {
            condition = ((LineBreakpoint) b).getCondition();
        } else if (b instanceof FieldBreakpoint) {
            condition = ((FieldBreakpoint) b).getCondition();
        } else if (b instanceof MethodBreakpoint) {
            condition = ((MethodBreakpoint) b).getCondition();
        } else if (b instanceof ClassLoadUnloadBreakpoint) {
            condition = null;
        } else {
            throw new IllegalStateException(b.toString());
        }
        boolean isConditional = (condition != null) && condition.trim().length() > 0;
        String annotationType = getAnnotationType(b, isConditional);
        DataObject dataObject;
        try {
            dataObject = DataObject.find(fo);
        } catch (DataObjectNotFoundException donfex) {
            donfex.printStackTrace();
            return ;
        }
        LineCookie lc = dataObject.getCookie(LineCookie.class);
        if (lc == null) return;
        List<DebuggerBreakpointAnnotation> annotations = new ArrayList<DebuggerBreakpointAnnotation>();
        for (int l : lines) {
            try {
                Line line = lc.getLineSet().getCurrent(l - 1);
                DebuggerBreakpointAnnotation annotation = new DebuggerBreakpointAnnotation (annotationType, line, b);
                annotations.add(annotation);
            } catch (IndexOutOfBoundsException e) {
            } catch (IllegalArgumentException e) {
            }
        }
        if (annotations.size() == 0) {
            return ;
        }
        Set<Annotation> bpAnnotations = breakpointToAnnotations.get(b);
        if (bpAnnotations == null) {
            breakpointToAnnotations.put(b, new WeakSet<Annotation>(annotations));
        } else {
            bpAnnotations.addAll(annotations);
            breakpointToAnnotations.put(b, bpAnnotations);
        }
    }

    // Is called under synchronized (breakpointToAnnotations)
    private void removeAnnotations(JPDABreakpoint b) {
        Set<Annotation> annotations = breakpointToAnnotations.remove(b);
        if (annotations == null) return ;
        for (Annotation a : annotations) {
            a.detach();
        }
    }
    

    // Not used
    public Breakpoint[] initBreakpoints() { return new Breakpoint[] {}; }

    // Not used
    public void initWatches() {}

    // Not used
    public void watchAdded(Watch watch) {}

    // Not used
    public void watchRemoved(Watch watch) {}

    // Not used
    public void sessionAdded(Session session) {}

    // Not used
    public void sessionRemoved(Session session) {}

    // Not used
    public void engineAdded(DebuggerEngine engine) {}

    // Not used
    public void engineRemoved(DebuggerEngine engine) {}

}
