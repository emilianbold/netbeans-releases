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

package org.netbeans.modules.cnd.debugger.common.breakpoints;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import java.util.logging.Logger;
import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.Breakpoint.VALIDITY;
import org.netbeans.api.debugger.DebuggerEngine;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerListener;
import org.netbeans.api.debugger.Session;
import org.netbeans.api.debugger.Watch;

import org.netbeans.modules.cnd.debugger.common.EditorContext;
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
public class BreakpointAnnotationProvider implements AnnotationProvider, DebuggerManagerListener {

    private final Map<CndBreakpoint, Set<Annotation>> breakpointToAnnotations = new HashMap<CndBreakpoint, Set<Annotation>>();
    private final Set<FileObject> annotatedFiles = new WeakSet<FileObject>();
    private Set<PropertyChangeListener> dataObjectListeners;
    private boolean attachManagerListener = true;
    private final Logger log = Logger.getLogger("gdb.breakpoint.annotations"); // NOI18N

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
                            FileObject newFO = dobj.getPrimaryFile();
                            annotate(newFO);
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
                log.fine("BreakpointAnnotationProvider.annotate: " + fo.getNameExt() + " is already annotated");
                return ;
            }
            Set<CndBreakpoint> annotatedBreakpoints = breakpointToAnnotations.keySet();
            for (Breakpoint breakpoint : DebuggerManager.getDebuggerManager().getBreakpoints()) {
                if (isAnnotatable(breakpoint)) {
                    CndBreakpoint b = (CndBreakpoint) breakpoint;
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
            DebuggerManager.getDebuggerManager().addDebuggerListener(WeakListeners.create(DebuggerManagerListener.class,
                 this, DebuggerManager.getDebuggerManager()));
        }
    }

    public void breakpointAdded(Breakpoint breakpoint) {
        if (isAnnotatable(breakpoint)) {
            CndBreakpoint b = (CndBreakpoint) breakpoint;
            log.fine("BreakpointAnnotationProvider.breakpointAdded: " + b.getPath() + ":" + b.getLineNumber());
            b.addPropertyChangeListener (this);
            RequestProcessor.getDefault().post(new AnnotationRefresh(b, false, true));
            if (b instanceof LineBreakpoint) {
                LineBreakpoint lb = (LineBreakpoint) b;
                LineTranslations.getTranslations().registerForLineUpdates(lb);
            }
        }
    }

    public void breakpointRemoved(Breakpoint breakpoint) {
        if (isAnnotatable(breakpoint)) {
            CndBreakpoint b = (CndBreakpoint) breakpoint;
            log.fine("BreakpointAnnotationProvider.breakpointRemoved: " + b.getPath() + ":" + b.getLineNumber());
            b.removePropertyChangeListener (this);
            RequestProcessor.getDefault().post(new AnnotationRefresh(b, true, false));
            if (b instanceof LineBreakpoint) {
                LineBreakpoint lb = (LineBreakpoint) b;
                LineTranslations.getTranslations().unregisterFromLineUpdates(lb);
            }
        }
    }

    public void propertyChange(PropertyChangeEvent evt) {
        String propertyName = evt.getPropertyName();
        if (propertyName == null) {
            return;
        }
        if ( (!CndBreakpoint.PROP_ENABLED.equals(propertyName)) &&
             (!CndBreakpoint.PROP_VALIDITY.equals(propertyName)) &&
             (!CndBreakpoint.PROP_CONDITION.equals(propertyName)) &&
             (!CndBreakpoint.PROP_SKIP_COUNT.equals(propertyName)) &&
             (!CndBreakpoint.PROP_URL.equals(propertyName)) &&
             (!FunctionBreakpoint.PROP_FUNCTION_NAME.equals(propertyName)) &&
             (!CndBreakpoint.PROP_LINE_NUMBER.equals(propertyName)) &&
             (!AddressBreakpoint.PROP_ADDRESS_VALUE.equals(propertyName)) &&
             (!AddressBreakpoint.PROP_REFRESH.equals(propertyName))) {
            return;
        }
        
        CndBreakpoint b = (CndBreakpoint) evt.getSource();
        DebuggerManager manager = DebuggerManager.getDebuggerManager();
        Breakpoint[] bkpts = manager.getBreakpoints();
        boolean found = false;
        for (Breakpoint breakpoint : bkpts) {
            if (b == breakpoint) {
                found = true;
                break;
            }
        }
        if (!found) {
            // breakpoint has been removed
            return;
        }
        RequestProcessor.getDefault().post(new AnnotationRefresh(b, true, true));
    }

    private final class AnnotationRefresh implements Runnable {
        private CndBreakpoint b;
        private boolean remove, add;

        public AnnotationRefresh(CndBreakpoint b, boolean remove, boolean add) {
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
                b instanceof FunctionBreakpoint ||
                b instanceof AddressBreakpoint) &&
               !((CndBreakpoint) b).isHidden();
    }
    
    private static String getAnnotationType(CndBreakpoint b, boolean isConditional) {
        boolean isInvalid = b.getValidity() == VALIDITY.INVALID;
        String annotationType;
        if (b instanceof LineBreakpoint) {
            annotationType = b.isEnabled() ?
                (isConditional ? EditorContext.CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE :
                                 EditorContext.BREAKPOINT_ANNOTATION_TYPE) :
                (isConditional ? EditorContext.DISABLED_CONDITIONAL_BREAKPOINT_ANNOTATION_TYPE :
                                 EditorContext.DISABLED_BREAKPOINT_ANNOTATION_TYPE);
        } else if (b instanceof FunctionBreakpoint) {
            annotationType = b.isEnabled() ?
                EditorContext.FUNCTION_BREAKPOINT_ANNOTATION_TYPE :
                EditorContext.DISABLED_FUNCTION_BREAKPOINT_ANNOTATION_TYPE;
        } else if (b instanceof AddressBreakpoint) {
            annotationType = b.isEnabled() ?
                EditorContext.ADDRESS_BREAKPOINT_ANNOTATION_TYPE :
                EditorContext.DISABLED_ADDRESS_BREAKPOINT_ANNOTATION_TYPE;
        } else {
            throw new IllegalStateException(b.toString());
        }
        if (isInvalid && b.isEnabled()) {
            annotationType += "_broken"; // NOI18N
        }
        return annotationType;
    }
    
    /** @return The annotation lines or <code>null</code>. */
    private static int[] getAnnotationLines(CndBreakpoint b, FileObject fo) {
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
        } else if (b instanceof FunctionBreakpoint) {
            FunctionBreakpoint fb = (FunctionBreakpoint) b;
            String url = fb.getURL();
            if (url.length() > 0) {
                try {
                    if (fo.getURL().equals(new URL(url))) {
                        return new int[] { fb.getLineNumber() };
                    }
                } catch (MalformedURLException ex) {
                    Exceptions.printStackTrace(ex);
                } catch (FileStateInvalidException ex) {
                    Exceptions.printStackTrace(ex);
                }
            }
            return null;
        } else if (b instanceof AddressBreakpoint) {
//            if (fo != Disassembly.getFileObject()) {
//                return null;
//            }
//            Disassembly dis = Disassembly.getCurrent();
//            if (dis != null) {
//                AddressBreakpoint ab = (AddressBreakpoint) b;
//                return new int[] {dis.getAddressLine(ab.getAddress())};
//            }
            return null;
        } else {
            throw new IllegalStateException(b.toString());
        }
    }
    
    // Is called under synchronized (breakpointToAnnotations)
    private void addAnnotationTo(CndBreakpoint b, FileObject fo) {
        int[] lines = getAnnotationLines(b, fo);
        if (lines == null || lines.length == 0) {
            return;
        }
        log.fine("BreakpointAnnotationProvider.addAnnotationTo: " + b.getPath() + ":" + b.getLineNumber());
        String condition = b.getCondition();
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
        if (lc == null) {
            return;
        }
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
        if (annotations.isEmpty()) {
            return;
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
    private void removeAnnotations(CndBreakpoint b) {
        Set<Annotation> annotations = breakpointToAnnotations.remove(b);
        if (annotations == null) {
            return;
        }
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
