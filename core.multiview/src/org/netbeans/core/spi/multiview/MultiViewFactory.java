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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.core.spi.multiview;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.netbeans.core.multiview.MultiViewCloneableTopComponent;
import org.netbeans.core.multiview.MultiViewTopComponent;
import org.openide.util.Lookup;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

/** Factory class for creating top components handling multi views.
 *
 * @author  Dafe Simonek, Milos Kleint
 */
public final class MultiViewFactory {
    
    /**
     * A utility singleton instance of MultiViewElement that does nothing.
     */
    
    public final static MultiViewElement BLANK_ELEMENT = new Blank();
    /**
     * a utility noop action instance to be used when no special handling is
     * required in createUnsafeCloseState() method.
     */
    public final static Action NOOP_CLOSE_ACTION = new NoopAction();
    
    

    /** Factory class, no instances. */
    private MultiViewFactory () {
    }

    /** Creates and returns new instance of top component with
     * multi views.
     * PLEASE NOTE: a non-cloneable TopComponent is not able to embed editors aka subclasses of CloneableEditor correctly.
     * Use createCloneableMultiView() method in such a case.
     * @param descriptions array of descriptions of tabs in the multiview.
     * @param defaultDesc the initial selection, one of the descriptions array values.
     */
    public static TopComponent createMultiView (MultiViewDescription[] descriptions, MultiViewDescription defaultDesc) {
        return createMultiView(descriptions, defaultDesc, createDefaultCloseOpHandler());
    }

    /** Creates and returns new instance of top component with
     * multi views.
     * PLEASE NOTE: a non-cloneable TopComponent is not able to embed editors aka subclasses of CloneableEditor correctly.
     * Use createCloneableMultiView() method in such a case.
     * @param descriptions  array of descriptions of tabs in the multiview.
     * @param defaultDesc  the initial selection, one of the descriptions array values.
     * @param closeHandler handles closing of the multiview component, useful when any of the embedded elements can be in modified state and closing would cause a dataloss..
     */
    public static TopComponent createMultiView (MultiViewDescription[] descriptions, MultiViewDescription defaultDesc,
                                                CloseOperationHandler closeHandler) {
        if (descriptions == null) return null;
        if (closeHandler == null) closeHandler = createDefaultCloseOpHandler();
        MultiViewTopComponent tc = new MultiViewTopComponent();
        tc.setMultiViewDescriptions(descriptions, defaultDesc);
        tc.setCloseOperationHandler(closeHandler);
        return tc;
    }
    
   /** Creates and returns new instance of cloneable top component with
     * multi views 
     * @param descriptions  array of descriptions of tabs in the multiview.
     * @param defaultDesc  the initial selection, one of the descriptions array values.
    */
    public static CloneableTopComponent createCloneableMultiView (MultiViewDescription[] descriptions, MultiViewDescription defaultDesc) {
        return createCloneableMultiView(descriptions, defaultDesc, createDefaultCloseOpHandler());
    }

    /** Creates and returns new instance of cloneable top component with
     * multi views.
     * @param descriptions  array of descriptions of tabs in the multiview.
     * @param defaultDesc  the initial selection, one of the descriptions array values.
     * @param closeHandler handles closing of the multiview component, useful when any of the embedded elements can be in modified state and closing would cause a dataloss..
     */
    public static CloneableTopComponent createCloneableMultiView (MultiViewDescription[] descriptions, MultiViewDescription defaultDesc,
                                                CloseOperationHandler closeHandler) {
        if (descriptions == null) return null;
        if (closeHandler == null) closeHandler = createDefaultCloseOpHandler();
        MultiViewCloneableTopComponent tc = new MultiViewCloneableTopComponent();
        tc.setMultiViewDescriptions(descriptions, defaultDesc);
        tc.setCloseOperationHandler(closeHandler);
        return tc;
    }    
    
    /**
     * Utility method for MultiViewElements to create a CloseOperationState instance that
     * informs the environment that the MVElement is ok to be closed.
     */
    
    static CloseOperationState createSafeCloseState() {
        return new CloseOperationState(true, "ID_CLOSE_OK", NOOP_CLOSE_ACTION, NOOP_CLOSE_ACTION);
    }

    /**
     * Utility method for MultiViewElements to create a CloseOperationState instance 
     * that warns about possible data loss. Corrective actions can be defined.
     * @param warningId an id that idenfifies the problem, 
     *     the CloseOperationHandler used in the component should know about the warning's meaning and handle appropriately
     * @param proceedAction will be performed when the CloseOperationHandler decides that closing the component is ok and changes are to be saved.
     * @param discardAction will be performed when the CloseOperationHandler decides that the nonsaved data shall be discarded
     */
    
    public static CloseOperationState createUnsafeCloseState(String warningId, Action proceedAction, Action discardAction) {
        return new CloseOperationState(false, 
                        (warningId == null ? "" : warningId),
                        (proceedAction == null ? NOOP_CLOSE_ACTION : proceedAction),
                        (discardAction == null ? NOOP_CLOSE_ACTION : discardAction));
    }
    
    static CloseOperationHandler createDefaultCloseOpHandler() {
        return new DefaultCloseHandler();
    }
    
    
    private static final class Blank implements MultiViewElement, Serializable {
        
        private JPanel panel;
        private JPanel bar;
        
        Blank() {
            panel = new JPanel();
            bar = new JPanel();
        }
        
        public void componentActivated() {
        }
        
        public void componentClosed() {
        }
        
        public void componentDeactivated() {
        }
        
        public void componentHidden() {
        }
        
        public void componentOpened() {
        }
        
        public void componentShowing() {
        }
        
        public Action[] getActions() {
            return new Action[0];
        }
        
        public Lookup getLookup() {
            return Lookup.EMPTY;
        }
        
        public JComponent getToolbarRepresentation() {
            return bar;
        }
        
        public javax.swing.JComponent getVisualRepresentation() {
            return panel;
        }
        
        public void setMultiViewCallback(MultiViewElementCallback callback) {
        }
        
        
        public org.openide.awt.UndoRedo getUndoRedo() {
            return null;
        }
        
        public CloseOperationState canCloseElement() {
            return CloseOperationState.STATE_OK;
        }
        
    }

/**
 * default simple implementation of the close handler.
 */    
    private static final class DefaultCloseHandler implements CloseOperationHandler, Serializable {
         private static final long serialVersionUID =-3126744916624172427L;        
       
        public boolean resolveCloseOperation(CloseOperationState[] elements) {
            if (elements != null) {
                boolean canBeClosed = true;
                Collection badOnes = new ArrayList();
                for (int i = 0; i < elements.length; i++) {
                    if (!elements[i].canClose()) {
                        badOnes.add(elements[i]);
                        canBeClosed = false;
                    }
                }
                if (!canBeClosed) {
                    //TODO SHOW dialog here.
                    throw new IllegalStateException("Cannot close component. Some of the elements require close operation handling. See MultiViewFactory.createMultiView()");
//                    Object[] options = new Object[] {
//                        new JButton("Proceed"),
//                        new JButton("Discard"),
//                        new JButton("Cancel")
//                    };
//                    NotifyDescriptor desc = new NotifyDescriptor(createPanel(badOnes), "Cannot close component.", 
//                                NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.WARNING_MESSAGE, 
//                                options, options[0]);
//                    Object retVal = DialogDisplayer.getDefault().notify(desc);
//                    if (retVal == options[0]) {
//                        // do proceed.
//                        Iterator it = badOnes.iterator();
//                        while (it.hasNext()) {
//                            Action act = ((CloseOperationState)it.next()).getProceedAction();
//                            if (act != null) {
//                                act.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "proceed"));
//                            }
//                        }
//                    } else if (retVal == options[1]) {
//                        // do discard
//                        Iterator it = badOnes.iterator();
//                        while (it.hasNext()) {
//                            Action act = ((CloseOperationState)it.next()).getDiscardAction();
//                            if (act != null) {
//                                act.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, "discard"));
//                            }
//                        }
//                    } else {
//                        // was cancel..
//                        return false;
//                    }
                }
            }
            return true;
        }
        
//        private JPanel createPanel(Collection elems) {
//            JPanel panel = new JPanel();
//            panel.setLayout(new BorderLayout());
//            JLabel lbl = new JLabel("Cannot safely close component for following reasons:");
//            panel.add(lbl, BorderLayout.NORTH);
//            JScrollPane pane = new JScrollPane();
//            String[] warnings = new String[elems.size()];
//            int index = 0;
//            Iterator it = elems.iterator();
//            while (it.hasNext()) {
//                CloseOperationState state = (CloseOperationState)it.next();
//                warnings[index] = state.getCloseWarningMessage();
//                index = index + 1;
//            }
//            JList list = new JList(warnings);
//            pane.setViewportView(list);
//            panel.add(pane);
//            return panel;
//        }
    }
    
    /**
     * just a default noon action to put into the closeoperation state.
     */
    private static final class NoopAction extends AbstractAction {
        
        public void actionPerformed(java.awt.event.ActionEvent e) {
            // do nothing
        }
        
    }
    
}
