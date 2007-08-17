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

package org.netbeans.modules.debugger.jpda.projects;

import java.beans.PropertyChangeEvent;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Breakpoint.VALIDITY;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;
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
import org.openide.util.WeakListeners;
import org.openide.util.WeakSet;


/**
 * This class is called when some file in editor is openend. It changes if
 * some LineBreakpoints with annotations should be readed.
 *
 * @author Jan Jancura, Martin Entlicher
 */
public class BreakpointAnnotationProvider implements AnnotationProvider,
                                                     DebuggerManagerListener {

    private Map<JPDABreakpoint, Annotation[]> breakpointToAnnotations;
    private Set<FileObject> annotatedFiles;

    public void annotate (Line.Set set, Lookup lookup) {
        FileObject fo = (FileObject) lookup.lookup (FileObject.class);
        if (fo == null) return;
        boolean attachManagerListener = false;
        synchronized (this) {
            if (breakpointToAnnotations == null) {
                breakpointToAnnotations = new HashMap<JPDABreakpoint, Annotation[]>();
                annotatedFiles = new WeakSet<FileObject>();
                attachManagerListener = true;
            }
        }
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
                        breakpointToAnnotations.put(b, new Annotation[] {});
                    }
                    addAnnotationTo(b, fo);
                }
            }
            annotatedFiles.add(fo);
        }
        if (attachManagerListener) {
            DebuggerManager.getDebuggerManager().addDebuggerListener(
                    WeakListeners.create(DebuggerManagerListener.class,
                                         this,
                                         DebuggerManager.getDebuggerManager()));
        }
    }

    public void breakpointAdded(Breakpoint breakpoint) {
        if (isAnnotatable(breakpoint)) {
            JPDABreakpoint b = (JPDABreakpoint) breakpoint;
            synchronized (breakpointToAnnotations) {
                b.addPropertyChangeListener (this);
                breakpointToAnnotations.put(b, new Annotation[] {});
                for (FileObject fo : annotatedFiles) {
                    addAnnotationTo(b, fo);
                }
            }
        }
    }

    public void breakpointRemoved(Breakpoint breakpoint) {
        if (isAnnotatable(breakpoint)) {
            JPDABreakpoint b = (JPDABreakpoint) breakpoint;
            synchronized (breakpointToAnnotations) {
                b.removePropertyChangeListener (this);
                removeAnnotations(b);
                breakpointToAnnotations.remove(b);
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName ();
        if (propertyName == null) return;
        //if (!listen) return;
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
        synchronized (breakpointToAnnotations) {
            removeAnnotations(b);
            breakpointToAnnotations.put(b, new Annotation[] {});
            for (FileObject fo : annotatedFiles) {
                addAnnotationTo(b, fo);
            }
        }
    }
    
    private static boolean isAnnotatable(Breakpoint b) {
        return (b instanceof LineBreakpoint ||
                b instanceof FieldBreakpoint ||
                b instanceof MethodBreakpoint) &&
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
            int line = EditorContextImpl.getFieldLineNumber(fo, className, fieldName);
            return new int[] { line };
        } else if (b instanceof MethodBreakpoint) {
            MethodBreakpoint mb = (MethodBreakpoint) b;
            String[] filters = mb.getClassFilters();
            int[] lns = new int[] {};
            for (int i = 0; i < filters.length; i++) {
                // TODO: annotate also other matched classes
                if (!filters[i].startsWith("*") && !filters[i].endsWith("*")) {
                    int[] newlns = EditorContextImpl.getMethodLineNumbers(
                            fo, filters[i], mb.getClassExclusionFilters(),
                            mb.getMethodName(),
                            mb.getMethodSignature());
                    if (lns.length == 0) {
                        lns = newlns;
                    } else {
                        int[] ln = new int[lns.length + newlns.length];
                        System.arraycopy(lns, 0, ln, 0, lns.length);
                        System.arraycopy(newlns, 0, ln, lns.length, newlns.length);
                        lns = ln;
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
        List<DebuggerAnnotation> annotations = new ArrayList<DebuggerAnnotation>();
        for (int l : lines) {
            try {
                Line line = lc.getLineSet().getCurrent(l - 1);
                DebuggerAnnotation annotation = new DebuggerAnnotation (annotationType, line);
                annotations.add(annotation);
            } catch (IndexOutOfBoundsException e) {
            } catch (IllegalArgumentException e) {
            }
        }
        if (annotations.size() == 0) {
            return ;
        }
        Object[] oldAnnotations = breakpointToAnnotations.get(b);
        if (oldAnnotations == null || oldAnnotations.length == 0) {
            breakpointToAnnotations.put(b, annotations.toArray(new Annotation[0]));
        } else {
            Annotation[] newAnnotations = new Annotation[oldAnnotations.length + annotations.size()];
            System.arraycopy(oldAnnotations, 0, newAnnotations, 0, oldAnnotations.length);
            for (int i = 0; i < annotations.size(); i++) {
                newAnnotations[i + oldAnnotations.length] = annotations.get(i);
            }
            breakpointToAnnotations.put(b, newAnnotations);
        }
    }

    // Is called under synchronized (breakpointToAnnotations)
    private void removeAnnotations(JPDABreakpoint b) {
        Annotation[] annotations = breakpointToAnnotations.remove(b);
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
