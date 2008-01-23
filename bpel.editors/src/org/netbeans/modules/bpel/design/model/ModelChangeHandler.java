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
package org.netbeans.modules.bpel.design.model;

import java.util.Iterator;
import javax.swing.SwingUtilities;
import org.netbeans.modules.bpel.model.api.ActivityHolder;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.Correlation;
import org.netbeans.modules.bpel.model.api.CorrelationSet;
import org.netbeans.modules.bpel.model.api.Import;
import org.netbeans.modules.bpel.model.api.NamedElement;
import org.netbeans.modules.bpel.model.api.OperationReference;
import org.netbeans.modules.bpel.model.api.PartnerLinkReference;
import org.netbeans.modules.bpel.model.api.PortTypeReference;
import org.netbeans.modules.bpel.model.api.Variable;
import org.netbeans.modules.bpel.model.api.Process;

import org.netbeans.modules.bpel.model.api.events.ArrayUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEventListener;
import org.netbeans.modules.bpel.model.api.events.EntityInsertEvent;
import org.netbeans.modules.bpel.model.api.events.EntityRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.EntityUpdateEvent;
import org.netbeans.modules.bpel.design.DesignView;
import org.netbeans.modules.bpel.design.model.patterns.CompositePattern;
import org.netbeans.modules.bpel.design.model.patterns.PartnerlinkPattern;
import org.netbeans.modules.bpel.design.model.patterns.Pattern;
import org.netbeans.modules.bpel.design.model.patterns.ProcessPattern;
import org.netbeans.modules.bpel.design.selection.EntitySelectionModel;
import org.netbeans.modules.bpel.model.api.events.PropertyRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.openide.ErrorManager;

/**
 *
 * @author Alexey
 */
public class ModelChangeHandler implements ChangeEventListener {

    private DiagramModel model;
    private BpelEntity bpelEntityToShow = null;

    /** Creates a new instance of ModelChangeHandler */
    public ModelChangeHandler(DiagramModel model) {
        this.model = model;
    }

    public void notifyPropertyUpdated(final PropertyUpdateEvent event) {
        Runnable r = new Runnable() {

                    public void run() {
                        String name = event.getName();


                        if (name.equals(BpelModel.STATE)) {

                            model.getView().reloadModel();
                            return;
                        }

                        Pattern pattern = getParentPattern(event.getParent());

                        if (pattern == null) {
                            return;
                        }

                        if (name.equals(NamedElement.NAME)) {
                            pattern.updateName();
                        } else if (pattern instanceof PartnerlinkPattern) {
                            reloadPartnerlinks();
                        }

                    }

                    private void reloadPartnerlinks() {
                        Process ps = model.getView().getBPELModel().getProcess();
                        ProcessPattern psp = (ProcessPattern) model.getPattern(ps);
                        if (psp != null){
                            psp.reloadPartnerlinks();
                        }

                    }
                };

        executeInAWTThread(r, event);
    }

    public void notifyEntityRemoved(final EntityRemoveEvent event) {
        Runnable r = new Runnable() {

                    public void run() {
                        Pattern child = model.getPattern(event.getOldValue());
                        if (child != null) {
                            model.getView().getSelectionModel().fixSelection();
                            child.setParent(null);

                        }
                    }
                };
        executeInAWTThread(r, event);

    }

    public void notifyEntityInserted(final EntityInsertEvent event) {
        Runnable r = new Runnable() {

                    public void run() {
                        BpelEntity child = event.getValue();

                        //filter out elements like variabless and correlation sets
                        if (child instanceof Variable ||
                                child instanceof CorrelationSet ||
                                child instanceof Correlation ||
                                child instanceof Import) {
                            return;
                        }

                        updateParent(getParentPattern(event.getParent()),
                                getChildPattern(child));
                        bpelEntityToShow = child;
                    }
                     
                    
                ;
         
    }

    ;
        executeInAWTThread(r, event);
        
    }
    
    
    public void notifyEntityUpdated(final EntityUpdateEvent event) {
        Runnable r = new Runnable() {

                    public void run() {
                        /**
                 * Remove old pattern
                 **/
                        BpelEntity oldChild = event.getOldValue();
                        if (oldChild != null) {
                            Pattern toRemove = model.getPattern(oldChild);
                            if (toRemove != null) {
                                toRemove.setParent(null);
                            }

                        }

                        BpelEntity parent = event.getParent();
                        BpelEntity child = event.getNewValue();

                        if (parent == null) {
                            //root pattern was changed
                            if (child == null) {
                                //null child means broken model
                                model.setRootPattern(null);
                            } else if (child instanceof Process) {
                                model.setRootPattern(getChildPattern(child));
                            }
                        } else {
                            updateParent(getParentPattern(parent), getChildPattern(child));
                            bpelEntityToShow = child;
                        }
                    }
                     
                    
                ;
         
    }

    ;
        executeInAWTThread(r, event);
        
    }
    /**
     * This method was supposed to delegate processing of events to AWT thread if necessary,
     * but..
     * read comments below
     **/
    private void executeInAWTThread(final Runnable inner, final ChangeEvent event) {
        Runnable outer = new Runnable() {

                    public void run() {
                        try {
                            if (inner != null) {
                                inner.run();
                            }

                            DesignView view = model.getView();
                            if (event.isLastInAtomic()) {


                                //update diagram
                                view.diagramChanged();

                                //notify the decoration model to update
                                view.getDecorationManager().decorationChanged();

                                //                        if (patternToShow != null){
//                            view.getSelectionModel().setSelectedPattern(patternToShow);
//                            view.scrollSelectedToView();
//                            patternToShow = null;
//                        }

                                if (bpelEntityToShow != null) {
                                    view.getModel().expandToBeVisible(bpelEntityToShow);
                                    view.getSelectionModel().setSelected(bpelEntityToShow);
                                    view.scrollSelectedToView();
                                    bpelEntityToShow = null;
                                }
                            }
                        } catch (Exception ex) {
                            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                        }
                    }
                };
        /**
         * PROBLEM! Visual OM access is not thread-safe.
         * We can use it only in AWT thread!
         *
         * We can't delegate the event dispatching to AWT.invokeLater,
         * because we can work with removed elements only before the dispatch loop is over
         *
         * We can't delegate the event dispatching to AWT.invokeAndWait,
         * because it can cause deadlock
         **/

        if (SwingUtilities.isEventDispatchThread()) {
            outer.run();
        } else {
            SwingUtilities.invokeLater(outer);
        }

    }

    private void updateParent(Pattern parent, Pattern child) {
        if (parent != null && child != null && parent instanceof CompositePattern) {
            child.setParent((CompositePattern) parent);
        //            patternToShow = child;
        }
    }
    /**
     * Function returns pattern for parent element.
     * In some cases intermediate BPEL container do not have their own patterns,
     * so pattern of outer container is used
     * @returns null if entity is not represented on diagram.
     */

    private Pattern getParentPattern(BpelEntity e) {

        BpelEntity realParent = e;
        if (e == null) {
            //special case for brocken BPEL model
            realParent = null;
        } else if (e.getElementType() == ActivityHolder.class) {
            //workaround for problem when Otherwise is not represented as pattern on diagramm
            realParent = e.getParent();
        }

        return (realParent != null)
                ? model.getPattern(realParent)
                : null;
    }

    private Pattern getChildPattern(BpelEntity e) {

        if (e == null) {
            return null;
        }

        Pattern result = model.getPattern(e);

        if (result == null) {
            result = model.createPattern(e);
        }
        return result;
    }

    public void notifyPropertyRemoved(PropertyRemoveEvent event) {
        /**
         * try to dispatch this event with empty runnable, because it can hold
         * "lastInAtomic" flag ON, so it should trigger diagram repaint
         **/
        executeInAWTThread(null, event);
    }

    public void notifyArrayUpdated(ArrayUpdateEvent event) {
        /**
         * try to dispatch this event with empty runnable, because it can hold
         * "lastInAtomic" flag ON, so it should trigger diagram repaint
         **/

        executeInAWTThread(null, event);
    }
}
