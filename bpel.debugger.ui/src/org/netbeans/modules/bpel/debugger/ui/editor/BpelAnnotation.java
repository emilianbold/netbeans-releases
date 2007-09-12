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

package org.netbeans.modules.bpel.debugger.ui.editor;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import org.netbeans.modules.bpel.core.annotations.AnnotationManagerCookie;
import org.netbeans.modules.bpel.core.annotations.DiagramAnnotation;
import org.netbeans.modules.bpel.debugger.ui.util.ModelUtil;
import org.netbeans.modules.bpel.debugger.ui.util.EditorUtil;
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
        if (!myAnnotationType.isForSourceEditor()) {
            return;
        }
        
        int lineNumber = ModelUtil.getLineNumber(myBpelEntityId);
        if (lineNumber < 0) {
            removeLineAnnotation();
            return;
        }

        Line line = EditorUtil.getLine(getDataObject(), lineNumber);
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
