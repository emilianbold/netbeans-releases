/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.bpel.debugger.ui.breakpoint;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.netbeans.api.debugger.Breakpoint;
import org.netbeans.api.debugger.DebuggerManager;
import org.netbeans.api.debugger.DebuggerManagerAdapter;

import org.netbeans.modules.bpel.debugger.api.AnnotationType;
import org.netbeans.modules.bpel.debugger.api.EditorContextBridge;
import org.netbeans.modules.bpel.debugger.api.breakpoints.LineBreakpoint;

/**
 * @author Vladimir Yaroslavskiy
 * @version 2005.10.21
 */
public class BpelBreakpointListener extends DebuggerManagerAdapter {
    
    private Map<Breakpoint, Object> myBreakpointToAnnotation =
            new HashMap<Breakpoint, Object>();
    
    private Map<Object, Breakpoint> myAnnotationToBreakpoint =
            new HashMap<Object, Breakpoint>();
    
    private final AnnotationListener myAnnotationListener =
            new AnnotationListener();
    
//    private Map<LineBreakpoint, DataObject> myBreakpointToDataObject =
//            new HashMap<LineBreakpoint, DataObject>();
    
    
    /**{@inheritDoc}*/
    public void breakpointAdded(Breakpoint breakpoint) {
        if (breakpoint instanceof LineBreakpoint) {
            LineBreakpoint lbp = (LineBreakpoint)breakpoint;
            lbp.addPropertyChangeListener(Breakpoint.PROP_ENABLED, this);
            annotate(lbp);
            
//            subscribeToDataObject(lbp);
        }
    }
    
    public Breakpoint[] initBreakpoints () {
        return new Breakpoint[0];
    }
    
    /**{@inheritDoc}*/
    public void breakpointRemoved(Breakpoint breakpoint) {
        if (breakpoint instanceof LineBreakpoint) {
            LineBreakpoint lbp = (LineBreakpoint)breakpoint;
            removeAnnotation(lbp);
            lbp.removePropertyChangeListener(Breakpoint.PROP_ENABLED, this);
            
//            unsubscribeFromDataObject(lbp);
        }
    }
    
    /**{@inheritDoc}*/
    public void propertyChange(PropertyChangeEvent event) {
        if (event.getPropertyName() == Breakpoint.PROP_ENABLED) {
            annotate((LineBreakpoint) event.getSource());
        }
    }
    
    /**{@inheritDoc}*/
    public String[] getProperties() {
        return new String[] {
            DebuggerManager.PROP_BREAKPOINTS_INIT,
            DebuggerManager.PROP_BREAKPOINTS };
    }
    
    public synchronized LineBreakpoint findBreakpoint (String url, String xpath) {
        Iterator i = myBreakpointToAnnotation.keySet ().iterator ();
        while (i.hasNext ()) {
            LineBreakpoint lb = (LineBreakpoint) i.next ();
            if (!lb.getURL ().equals (url)) {
                continue;
            }
            Object annotation = myBreakpointToAnnotation.get (lb);
            String bpXpath = EditorContextBridge.getXpath(annotation);
            if (xpath.equals(bpXpath)) {
                return lb;
            }
        }
        return null;
    }
    
    private synchronized void annotate(LineBreakpoint breakpoint) {
        AnnotationType annotationType;
        
        Object annotation = myBreakpointToAnnotation.get(breakpoint);
        if (annotation != null) {
            EditorContextBridge.removeAnnotation(annotation);
        }

        if (breakpoint.isEnabled()) {
            annotationType = AnnotationType.ENABLED_BREAKPOINT;
        }
        else {
            annotationType = AnnotationType.DISABLED_BREAKPOINT;
        }
        
        annotation = EditorContextBridge.annotate (
                breakpoint.getURL(),
                breakpoint.getXpath(),
                annotationType);
        
        if (annotation == null) {
            return;
        }
        
        EditorContextBridge.addAnnotationListener(
                annotation, myAnnotationListener);
        
        myBreakpointToAnnotation.put(breakpoint, annotation);
        myAnnotationToBreakpoint.put(annotation, breakpoint);
    }
    
    private synchronized void removeAnnotation(LineBreakpoint breakpoint) {
        Object annotation = myBreakpointToAnnotation.remove(breakpoint);
        if (annotation != null) {
            myAnnotationToBreakpoint.remove(annotation);
            EditorContextBridge.removeAnnotationListener(
                    annotation, myAnnotationListener);
            EditorContextBridge.removeAnnotation(annotation);
        }
    }

//    private void subscribeToDataObject(LineBreakpoint lbp) {
//        DataObject dataObject = findDataObject(lbp);
//        if (dataObject == null) {
//            return;
//        }
//        
//        DataObjectObserver observer = myDataObjectToObservers.get(dataObject);
//        if (observer == null) {
//            observer = new DataObjectObserver(dataObject);
//            myDataObjectToObservers.put(dataObject, observer);
//        }
//        
//        observer.registerBreakpoint(lbp);
//    }
//    
//    private void unsubscribeFromDataObject(LineBreakpoint lbp) {
//        DataObject dataObject = findDataObject(lbp);
//        if (dataObject == null) {
//            return;
//        }
//        
//        DataObjectObserver observer = myDataObjectToObservers.get(dataObject);
//        if (observer != null) {
//            observer.unregisterBreakpoint(lbp);
//            if (observer.getBreakpoints().size() == 0) {
//                myDataObjectToObservers.remove(dataObject);
//            }
//        }
//    }
//
//    private DataObject findDataObject(LineBreakpoint lbp) {
//        DataObject dataObject = null;
//        FileObject fo = FileUtil.toFileObject(new File(lbp.getURL()));
//        if (fo != null) {
//            try {
//                dataObject = DataObject.find(fo);
//            } catch (DataObjectNotFoundException ex) {
//                ex.printStackTrace();
//            }
//        }
//        
//        return dataObject;
//    }
    
    private void updateBreakpointByAnnotation(Object annotation) {
        LineBreakpoint lbp = null;
        synchronized (this) {
            lbp = (LineBreakpoint)myAnnotationToBreakpoint.
                    get(annotation);
        }
        
        if (lbp == null) {
            System.out.println(
                    "Could not find a breakpoint for the annotation");
            return;
        }

        String xpath = EditorContextBridge.getXpath(annotation);
        if (xpath != null) {
            lbp.setXpath(xpath);
        }
        lbp.touch();
    }
    
    private class AnnotationListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            Object annotation = evt.getSource();
            updateBreakpointByAnnotation(annotation);
        }
    }
}
