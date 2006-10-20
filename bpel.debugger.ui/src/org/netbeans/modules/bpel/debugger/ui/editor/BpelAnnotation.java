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

package org.netbeans.modules.bpel.debugger.ui.editor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.bpel.core.annotations.AnnotationManagerCookie;
import org.netbeans.modules.bpel.core.annotations.DiagramAnnotation;
import org.netbeans.modules.bpel.debugger.ui.util.ModelUtil;
import org.netbeans.modules.bpel.debugger.ui.util.Util;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.netbeans.modules.bpel.debugger.api.AnnotationType;

/**
 * @author Vladimir Yaroslavskiy
 * @author Alexander Zgursky
 * @version 2005.10.19
 */
class BpelAnnotation {
    
    public static final String          PROP_STATE = "state"; //NOI18N
    
    public static final String          PROP_LINE = "line"; //NOI18N
    
    public static final String          PROP_XPATH = "xpath"; //NOI18N
    
    
    private final AnnotationType myAnnotationType;
    private final UniqueId myBpelEntityId;

    private State myState;
    private String myXpath;
    private DiagramAnnotation myDiagramAnnotation;
    private LineAnnotation myLineAnnotation;
    
    private PropertyChangeSupport myPcs;
    
    public BpelAnnotation(
            AnnotationType annotationType,
            UniqueId bpelEntityId)
    {
        myAnnotationType = annotationType;
        myBpelEntityId = bpelEntityId;
        myPcs = new PropertyChangeSupport(this);
        myState = State.DETACHED;
    }
    
    public synchronized boolean attach() {
        if (myState == State.ATTACHED || myState == State.BROKEN) {
            return false;
        }
        
        assert myDiagramAnnotation == null;
        assert myLineAnnotation == null;
        
        DiagramAnnotation diagramAnnotation = new DiagramAnnotation(
                myBpelEntityId, myAnnotationType.getType());
        
        AnnotationManagerCookie annotationManager =
                (AnnotationManagerCookie)getDataObject().getCookie(
                AnnotationManagerCookie.class);
        
        if (!annotationManager.addAnnotation(diagramAnnotation)) {
            return false;
        }
        myDiagramAnnotation = diagramAnnotation;
        subscribe();
        update();
        setState(State.ATTACHED);
        
        return true;
    }
    
    protected synchronized void update() {
        //kinda hack - check if myDiagramAnnotation is null to find out
        //if we are really in DETACHED state or update() is called from
        //the attach()
        if (myState == State.DETACHED && myDiagramAnnotation == null) {
            return;
        }
        
        updateLineAnnotation();
        firePropertyChange(null, null, null);
    }
    
    private void updateLineAnnotation() {
        int lineNumber = ModelUtil.getLineNumber(myBpelEntityId);
        if (lineNumber < 0) {
            removeLineAnnotation();
            return;
        }

        Line line = Util.getLine(getDataObject(), lineNumber);
        if (line == null) {
            removeLineAnnotation();
            return;
        }

        if (myLineAnnotation != null && line.equals(myLineAnnotation.getAttachedAnnotatable())) {
            //the line annotation is already on the valid place
            return;
        }

        removeLineAnnotation();

        myLineAnnotation = new LineAnnotation();
        myLineAnnotation.attach(line);

        return;
    }
    
    private void removeLineAnnotation() {
        if (myLineAnnotation != null) {
            myLineAnnotation.detach();
            myLineAnnotation = null;
        }
    }
    
    public synchronized void detach() {
        if (myState == State.DETACHED) {
            return;
        }
        
        assert myDiagramAnnotation != null;
        
        AnnotationManagerCookie annotationManager =
                (AnnotationManagerCookie)getDataObject().getCookie(
                AnnotationManagerCookie.class);
        annotationManager.removeAnnotation(myDiagramAnnotation);
        myDiagramAnnotation = null;
        
        removeLineAnnotation();
        unsubscribe();
        
        setState(State.DETACHED);
    }
    
    private void subscribe() {
        BpelAnnotationsObserver.subscribe(this);
    }
    
    private void unsubscribe() {
        BpelAnnotationsObserver.unsubscribe(this);
    }
    
    public UniqueId getBpelEntityId() {
        return myBpelEntityId;
    }
    
    public synchronized String getXpath() {
        return myXpath;
    }
    
    public synchronized State getState() {
        return myState;
    }
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    public final void addPropertyChangeListener(PropertyChangeListener l) {
        myPcs.addPropertyChangeListener(l);
    }
    
    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    public final void removePropertyChangeListener(PropertyChangeListener l) {
        myPcs.removePropertyChangeListener(l);
    }
    
    /**
     * Adds property change listener.
     *
     * @param propertyName property name to add listener for
     * @param l new listener.
     */
    public final void addPropertyChangeListener(
            String propertyName, PropertyChangeListener l)
    {
        myPcs.addPropertyChangeListener(propertyName, l);
    }
    
    /**
     * Removes property change listener.
     *
     * @param propertyName property name to remove listener for
     * @param l listener to remove
     */
    public final void removePropertyChangeListener(
            String propertyName, PropertyChangeListener l)
    {
        myPcs.removePropertyChangeListener(propertyName, l);
    }

    /**
     * Fires property change.
     */
    protected final void firePropertyChange(String name, Object o, Object n) {
        myPcs.firePropertyChange(name, o, n);
    }
    
    private void setState(State newState) {
        State oldState = myState;
        myState = newState;
        firePropertyChange(PROP_STATE, oldState, newState);
    }
    
    private DataObject getDataObject() {
        return (DataObject)myBpelEntityId.getModel().
                getModelSource().getLookup().lookup(DataObject.class);
    }
    
    public enum State {
        ATTACHED,
        DETACHED,
        BROKEN
    }
    
    private class LineAnnotation extends Annotation {
        public String getAnnotationType() {
            return myAnnotationType.getType();
        }

        public String getShortDescription() {
            return myAnnotationType.getDescription();
        }
        
    }
}
