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

package org.netbeans.modules.bpel.debugger.ui.editor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.bpel.core.annotations.AnnotationManagerCookie;
import org.netbeans.modules.bpel.core.annotations.DiagramAnnotation;
import org.netbeans.modules.bpel.debugger.ui.util.EditorUtil;
import org.netbeans.modules.bpel.model.api.support.UniqueId;
import org.openide.loaders.DataObject;
import org.openide.text.Annotation;
import org.openide.text.Line;
import org.netbeans.modules.bpel.debugger.api.AnnotationType;
import org.netbeans.modules.bpel.debugger.ui.util.ModelUtil;
import org.netbeans.modules.bpel.model.api.BpelModel;

/**
 * @author Vladimir Yaroslavskiy
 * @author Alexander Zgursky
 * @version 2005.10.19
 */
class BpelAnnotation {
    
    public static final String PROP_STATE = "state"; //NOI18N
    
    private final AnnotationType myAnnotationType;
    private final DataObject myDataObject;
    private final String myXpath;
    private final UniqueId myBpelEntityId;
    private final int myLineNumber;

    private State myState;
    
    private DiagramAnnotation myDiagramAnnotation;
    private LineAnnotation myLineAnnotation;
    
    private PropertyChangeSupport myPcs;
    
    public BpelAnnotation(
            final AnnotationType annotationType,
            final DataObject dataObject,
            final String xpath,
            final int lineNumber) {
        
        myAnnotationType = annotationType;
        myDataObject = dataObject;
        myXpath = xpath;
        myLineNumber = lineNumber;
        
        myPcs = new PropertyChangeSupport(this);
        myState = State.DETACHED;
        
        if (xpath != null) {
            myBpelEntityId = ModelUtil.getBpelEntityId(
                    getBpelModel(), xpath);
        } else {
            myBpelEntityId = null;
        }
    }
    
    public synchronized boolean attach() {
        if (myState == State.ATTACHED) {
            return false;
        }
        
        assert myDiagramAnnotation == null;
        assert myLineAnnotation == null;
        
        updateDiagramAnnotation();
        updateLineAnnotation();
        
        setState(State.ATTACHED);
        
        return true;
    }
    
    public synchronized void detach() {
        if (myState == State.DETACHED) {
            return;
        }
        
        removeDiagramAnnotation();
        removeLineAnnotation();
        
        setState(State.DETACHED);
    }
    
    public AnnotationType getType() {
        return myAnnotationType;
    }
    
    public String getXpath() {
        return myXpath;
    }
    
    public UniqueId getBpelEntityId() {
        return myBpelEntityId;
    }
    
    public int getLineNumber() {
        return myLineNumber;
    }
    
    public State getState() {
        return myState;
    }
    
    public BpelModel getBpelModel() {
        return EditorUtil.getBpelModel(myDataObject);
    }
    
    /**
     * Adds property change listener.
     *
     * @param l new listener.
     */
    public final void addPropertyChangeListener(
            final PropertyChangeListener listener) {
        
        myPcs.addPropertyChangeListener(listener);
    }
    
    /**
     * Removes property change listener.
     *
     * @param l removed listener.
     */
    public final void removePropertyChangeListener(
            final PropertyChangeListener listener) {
        
        myPcs.removePropertyChangeListener(listener);
    }
    
    /**
     * Adds property change listener.
     *
     * @param propertyName property name to add listener for
     * @param l new listener.
     */
    public final void addPropertyChangeListener(
            final String propertyName, 
            final PropertyChangeListener listener) {
        
        myPcs.addPropertyChangeListener(propertyName, listener);
    }
    
    /**
     * Removes property change listener.
     *
     * @param propertyName property name to remove listener for
     * @param l listener to remove
     */
    public final void removePropertyChangeListener(
            final String propertyName, 
            final PropertyChangeListener listener) {
        
        myPcs.removePropertyChangeListener(propertyName, listener);
    }
    
    // Protected ///////////////////////////////////////////////////////////////
    protected synchronized void update() {
        if (myState == State.DETACHED) {
            return;
        }
        
        updateDiagramAnnotation();
        updateLineAnnotation();
        
        firePropertyChange(null, null, null);
    }

    /**
     * Fires property change.
     */
    protected final void firePropertyChange(
            final String name, 
            final Object oldValue, 
            final Object newValue) {
        
        myPcs.firePropertyChange(name, oldValue, newValue);
    }
    
    // Private /////////////////////////////////////////////////////////////////
    private void updateDiagramAnnotation() {
        if (!myAnnotationType.isForDiagram() || (myBpelEntityId == null)) {
            return;
        }
        
        if ((myDiagramAnnotation != null) && myDiagramAnnotation.
                getBpelEntityId().equals(myBpelEntityId)) {
            removeDiagramAnnotation();
        }
        
        final DiagramAnnotation diagramAnnotation = new DiagramAnnotation(
                myBpelEntityId, myAnnotationType.getType());
        
        final AnnotationManagerCookie annotationManager =
                myDataObject.getCookie(AnnotationManagerCookie.class);
        
        annotationManager.addAnnotation(diagramAnnotation);
        
        myDiagramAnnotation = diagramAnnotation;
        
        BpelAnnotationsObserver.subscribe(this);
    }
    
    private void removeDiagramAnnotation() {
        if (!myAnnotationType.isForDiagram() || (myBpelEntityId == null)) {
            return;
        }
        
        if (myDiagramAnnotation != null) {
            final AnnotationManagerCookie annotationManager =
                    myDataObject.getCookie(AnnotationManagerCookie.class);
                    
            annotationManager.removeAnnotation(myDiagramAnnotation);
            myDiagramAnnotation = null;
            
            BpelAnnotationsObserver.unsubscribe(this);
        }
    }
    
    private void updateLineAnnotation() {
        if (!myAnnotationType.isForSourceEditor() || (myLineNumber == -1)) {
            return;
        }
        
        final Line line = EditorUtil.getLine(myDataObject, myLineNumber);
        if (line == null) {
            removeLineAnnotation();
            return;
        }
        
        if ((myLineAnnotation != null) && 
                line.equals(myLineAnnotation.getAttachedAnnotatable())) {
            return;
        }
        
        removeLineAnnotation();
        
        myLineAnnotation = new LineAnnotation();
        myLineAnnotation.attach(line);
        
        return;
    }
    
    private void removeLineAnnotation() {
        if (!myAnnotationType.isForSourceEditor() || (myLineNumber == -1)) {
            return;
        }
        
        if (myLineAnnotation != null) {
            myLineAnnotation.detach();
            myLineAnnotation = null;
        }
    }
    
    private void setState(
            final State newState) {
        
        final State oldState = myState;
        myState = newState;
        firePropertyChange(PROP_STATE, oldState, newState);
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // Inner Classes
    public enum State {
        ATTACHED,
        DETACHED
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
