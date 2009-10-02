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
 * License. When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP. Sun designates this
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
package org.netbeans.modules.bpel.core.multiview;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;

//import org.netbeans.modules.soa.validation.core.Controller;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.bpel.core.BPELDataEditorSupport;
import org.netbeans.modules.bpel.core.BPELDataObject;
import org.netbeans.modules.bpel.editors.api.nodes.FactoryAccess;
import org.netbeans.modules.bpel.editors.api.nodes.NodeType;
import org.netbeans.modules.bpel.editors.api.EditorUtil;
import org.netbeans.modules.bpel.model.api.BpelEntity;
import org.netbeans.modules.bpel.model.api.BpelModel;
import org.netbeans.modules.bpel.model.api.events.ArrayUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEvent;
import org.netbeans.modules.bpel.model.api.events.ChangeEventListener;
import org.netbeans.modules.bpel.model.api.events.EntityInsertEvent;
import org.netbeans.modules.bpel.model.api.events.EntityRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.EntityUpdateEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyRemoveEvent;
import org.netbeans.modules.bpel.model.api.events.PropertyUpdateEvent;
import org.netbeans.modules.soa.ui.nodes.InstanceRef;
import org.netbeans.modules.soa.ui.nodes.NodeFactory;
import org.netbeans.modules.xml.xam.Nameable;
import org.netbeans.modules.xml.xam.ui.multiview.CookieProxyLookup;
import org.netbeans.modules.xml.xam.ui.undo.QuietUndoManager;
import org.openide.awt.UndoRedo;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

/**
 * @author ads
 */
public class BPELSourceMultiViewElement extends CloneableEditor implements MultiViewElement {
    
    private static final int CARET_CHANGE_TASK_DELAY = 500;
    private static final long serialVersionUID = 1L;
    static final String PREFERED_ID="BpelSourceView";   // NOI18N
    
    // for deserialization
    private BPELSourceMultiViewElement() {
        super();
    }
    
    public BPELSourceMultiViewElement( BPELDataObject dataObject ) {
        super(dataObject.getEditorSupport());
        myDataObject = dataObject;
        // ================================================================
        // Initialize the editor support properly, which only needs to be
        // done when the editor is created (deserialization is working
        // due to CloneableEditor.readResolve() initializing the editor).
        // Note that this relies on the source view being the first in the
        // array of MultiViewDescription instances in BpelMultiViewSupport,
        // since that results in the source view being created and opened
        // by default, only to be hidden when the DataObject default action
        // makes the columns view appear.
        // This initialization fixes CR 6349089 by ensuring that the Node
        // listener is registered with the DataObject Node delegate.
        getDataObject().getEditorSupport().initializeCloneableEditor(this);
        
        initialize();
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(myDataObject);
    }

    /**
     * we are using Externalization semantics so that we can get a hook to call
     * initialize() upon deserialization
     */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal(in);
        Object obj = in.readObject();
        if ( obj instanceof BPELDataObject) {
            myDataObject = (BPELDataObject) obj;
        }
        initialize();
    }    
    
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }
    
    public CloseOperationState canCloseElement() {
        boolean lastView = isLastView();
        if(!lastView) {
            return CloseOperationState.STATE_OK;
        }

        //
        // not sure if we need to be intelligent here; other MV examples suggest
        // that you can just return dummy UnSafeCloseState() and delegate to the
        // closeHandler - for now we will be more intelligent and redundant here
        //
        boolean modified = cloneableEditorSupport().isModified();
        if(!modified) {
            return CloseOperationState.STATE_OK;
        }
        else {
            return MultiViewFactory.createUnsafeCloseState(
                "Data Object Modified", // NOI18N
                MultiViewFactory.NOOP_CLOSE_ACTION,
                MultiViewFactory.NOOP_CLOSE_ACTION);            
        }
    }

    @Override
    public Action[] getActions() {
        Action[] retAction;
        
        if (myMultiViewObserver != null) {
            Action[] defActions = myMultiViewObserver.createDefaultActions();
            Action[] nodeActions = getNodeActions();
            if ( nodeActions != null && nodeActions.length > 0) {
                List<Action> actionsList = new ArrayList<Action>();
                actionsList.addAll(Arrays.asList(defActions));
                actionsList.addAll(Arrays.asList(nodeActions));
                
                retAction = new Action[actionsList.size()];
                retAction = actionsList.toArray(retAction);
            } else {
                retAction =  defActions;
            }
        } else {
            retAction = super.getActions();
        }
        return retAction;
    }
    
    public void componentActivated() {
        super.componentActivated();
        setCaretAssocActiveNodes();
        BPELDataEditorSupport editor = getDataObject().getEditorSupport();
        editor.addUndoManagerToDocument();
//      getValidationController().triggerValidation();
    }
    
//    private Controller getValidationController() {
//      return (Controller) getDataObject().getLookup().lookup(Controller.class);
//    }
    
    public void componentClosed() {
        super.componentClosed();
        removeCaretPositionListener();
        setMultiViewCallback( null );

        if ( getParent()!= null ){
            getParent().remove( this );
        }
    }
    
    public void componentDeactivated() {
        super.componentDeactivated();
        BPELDataEditorSupport editor = getDataObject().getEditorSupport();
        editor.syncModel();
        editor.removeUndoManagerFromDocument();
        
    }
    
    public void componentHidden() {
        super.componentHidden();
        BPELDataEditorSupport editor = getDataObject().getEditorSupport();
        editor.syncModel();
        editor.removeUndoManagerFromDocument();
    }
    
    public void componentOpened() {
        super.componentOpened();
        addCaretPositionListener();
    }
    
    public void componentShowing() {
        super.componentShowing();
        BPELDataEditorSupport editor = getDataObject().getEditorSupport();
        editor.addUndoManagerToDocument();
    }
    
    public JComponent getToolbarRepresentation() {
        Document doc = getEditorPane().getDocument();
        if (doc instanceof NbDocument.CustomToolbar) {
            if (myToolBar == null) {
                myToolBar = ((NbDocument.CustomToolbar) doc).
                    createToolbar(getEditorPane());
            }
            return myToolBar;
        }
        return null;
    }

    public JComponent getVisualRepresentation() {
        return this; 
    }
    
    public void setMultiViewCallback( final MultiViewElementCallback callback) {
        myMultiViewObserver = callback;
    }
    
    public void requestVisible() {
        if (myMultiViewObserver != null) {
            myMultiViewObserver.requestVisible();
        }
        else {
            super.requestVisible();
        }
    }

    public void requestActive() {
        if (myMultiViewObserver != null) {
            myMultiViewObserver.requestActive();
        }
        else {
            super.requestActive();
        }
    }
    
    public UndoRedo getUndoRedo() {
        BPELDataEditorSupport editor = myDataObject.getEditorSupport();
        return editor.getUndoManager();
    }
    
    /**
     * The close last method should be called only for the last clone. 
     * If there are still existing clones this method must return false. The
     * implementation from the FormEditor always returns true but this is 
     * not the expected behavior. The intention is to close the editor support
     * once the last editor has been closed, using the silent close to avoid
     * displaying a new dialog which is already being displayed via the 
     * close handler. 
     */ 
    protected boolean closeLast() {
        BPELDataEditorSupport editor = getDataObject().getEditorSupport();
        JEditorPane[] editors = editor.getOpenedPanes();
        if (editors == null || editors.length == 0) {
            return editor.silentClose();
        }
        return false;
    }
    
    protected String preferredID() {
        return PREFERED_ID;  
    }
    
    private BPELDataObject getDataObject() {
        return myDataObject;
    }
    
    private void initialize() {
        // create and associate lookup
        Node delegate = myDataObject.getNodeDelegate();
        SourceCookieProxyLookup lookup = new SourceCookieProxyLookup(new Lookup[] {
            Lookups.fixed(new Object[] {
                // Need ActionMap in lookup so editor actions work.
                getActionMap(),
                // Need the data object registered in the lookup so that the
                // projectui code will close our open editor windows when the
                // project is closed.
                myDataObject
            }),getDataObject().getLookup(),
        }, delegate);
        associateLookup(lookup);
        addPropertyChangeListener("activatedNodes", lookup);
    }
    
    /**
     * Adds the undo/redo manager to the document as an undoable
     * edit listener, so it receives the edits onto the queue.
     */
    private void addUndoManager() {
        BPELDataEditorSupport editor = getDataObject().getEditorSupport();
        QuietUndoManager undo = editor.getUndoManager();
        StyledDocument doc = editor.getDocument();
        // Unlikely to be null, but could be if the cloned views are not
        // behaving correctly.
        if (doc != null) {
            // Ensure the listener is not added twice.
            doc.removeUndoableEditListener(undo);
            doc.addUndoableEditListener(undo);
            // Start the compound mode of the undo manager, such that when
            // we are hidden, we will treat all of the edits as a single
            // compound edit. This avoids having the user invoke undo
            // numerous times when in the model view.
            undo.beginCompound();
        }
    }

    /**
     * Removes the undo/redo manager undoable edit listener from the
     * document, to stop receiving undoable edits.
     */
    private void removeUndoManager() {
        BPELDataEditorSupport editor = getDataObject().getEditorSupport();
        StyledDocument doc = editor.getDocument();
        // May be null when closing the editor.
        if (doc != null) {
            QuietUndoManager undo = editor.getUndoManager();
            doc.removeUndoableEditListener(undo);
            undo.endCompound();
        }
    }

    private boolean isLastView() {
        boolean oneOrLess = true;
        Enumeration en = 
            ((CloneableTopComponent)myMultiViewObserver.getTopComponent()).
            getReference().getComponents();
        if (en.hasMoreElements()) {
            en.nextElement();
            if (en.hasMoreElements()) {
                oneOrLess = false;
            }
        }

        return oneOrLess;
    }
    
    private Action[] getNodeActions() {
        if (myMultiViewObserver == null) {
            return null;
        }
        Node[] activeNodes = myMultiViewObserver.getTopComponent().getActivatedNodes();;
        if (activeNodes != null && activeNodes.length > 0) {
            return activeNodes[0].getActions(true);
        }
        return null;
    }
    
    private void addCaretPositionListener() {
//        BPELDataEditorSupport editor = getDataObject().getEditorSupport();
//        JEditorPane[] editors = editor.getOpenedPanes();
//        if (editors == null || editors.length == 0) {
//            return;
//        }
        JEditorPane editorPane = getEditorPane();
        
        if (myCaretPositionListener != null && editorPane != null) {
            editorPane.removeCaretListener(myCaretPositionListener);
        }
        
        if (myCaretPositionListener == null) {
            myCaretPositionListener = new CaretListener() {
                public void caretUpdate(final CaretEvent e) {
                    selectElement();
                }
            };
        }
        editorPane.addCaretListener(myCaretPositionListener);
        
        
        BPELDataEditorSupport editorSupport = getDataObject().getEditorSupport();
        BpelModel model = editorSupport != null ? editorSupport.getBpelModel() : null;
        if (model != null) {
            if (myBpelModelListener != null) {
                model.removeEntityChangeListener(myBpelModelListener);
            }

            if (myBpelModelListener == null) {
                myBpelModelListener = new ChangeEventListener() {

                private void handleEvent(ChangeEvent event) {
                    if (event == null) {
                        return;
                    }
                    if (event.isLastInAtomic()) {
                        if (!SwingUtilities.isEventDispatchThread()) {
                            SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        selectElement(0);
                                    }
                                });
                        } else {
                            selectElement(0);
                        }
                    }
                }

                public void notifyPropertyRemoved(PropertyRemoveEvent event) {
                    handleEvent(event);
                }

                public void notifyEntityInserted(EntityInsertEvent event) {
                    handleEvent(event);
                }

                public void notifyPropertyUpdated(PropertyUpdateEvent event) {
                    handleEvent(event);
                }

                public void notifyEntityRemoved(EntityRemoveEvent event) {
                    handleEvent(event);
                }

                public void notifyEntityUpdated(EntityUpdateEvent event) {
                    handleEvent(event);
                }

                public void notifyArrayUpdated(ArrayUpdateEvent event) {
                    handleEvent(event);
                }
            };
            }

            model.addEntityChangeListener(myBpelModelListener);
        }
    }
    
    private void setCaretAssocActiveNodes() {
//        BPELDataEditorSupport editor = getDataObject().getEditorSupport();
//        JEditorPane[] editors = editor.getOpenedPanes();
//        if (editors == null || editors.length == 0) {
//            return;
//        }

        selectElement();
////        JEditorPane editorPane = getEditorPane();
////        if (editorPane != null) {
////            setActivatedNodes(editorPane.getCaretPosition());
////        }
    }
    
    private void setActivatedNodes(final int cursor) {
        BPELDataEditorSupport editorSupport = getDataObject().getEditorSupport();
        if (editorSupport == null) {
            return;
        }
        
        final BpelModel model = editorSupport.getBpelModel();
        if (model == null) {
            return;
        }
        
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                BpelEntity foundedEntity = model.findElement( cursor );
                if (foundedEntity == null) {
                    return;
                }
                NodeFactory nodeFactory = FactoryAccess.getPropertyNodeFactory();
                assert nodeFactory != null;

                NodeType nodeType = EditorUtil.getBasicNodeType(foundedEntity);
                if (nodeType == null) {
                    return;
                }

                nodeType = NodeType.UNKNOWN_TYPE.equals(nodeType) 
                        ? NodeType.DEFAULT_BPEL_ENTITY_NODE 
                        : nodeType;
                final Node node = nodeFactory.createNode(
                        nodeType,
                        foundedEntity,
                        getDataObject().getLookup());
                if (node == null) {
                    return;
                }
                final TopComponent tc = myMultiViewObserver == null 
                        ? null 
                        : myMultiViewObserver.getTopComponent();
                if (tc != null) {
                    setActivatedNodes(new Node[] {node});
                }
            }
        });
    }
    
    private void removeCaretPositionListener() {

        JEditorPane editorPane = getEditorPane();
        if (editorPane != null && myCaretPositionListener != null) {
            editorPane.removeCaretListener(myCaretPositionListener);
        }
        myCaretPositionListener = null;
        
        BPELDataEditorSupport editorSupport = getDataObject().getEditorSupport();
        BpelModel model = editorSupport != null ? editorSupport.getBpelModel() : null;
                
        if (myBpelModelListener != null && model != null) {
            model.removeEntityChangeListener(myBpelModelListener);
        }
        
        myBpelModelListener = null;
    }

    // TODO m
    private void selectElement(int delay) {
        assert SwingUtilities.isEventDispatchThread();
        if (myPreviousTask != null) {
            myPreviousTask.cancel();
        }
        // issue 125 439
        if (myPreviousTask != null && !myPreviousTask.isFinished() && RequestProcessor.getDefault().isRequestProcessorThread()) {
            myPreviousTask.waitFinished();
            myPreviousTask = null;
        }

        final JEditorPane curEditorPane = getEditorPane();
        if (curEditorPane != null && ! curEditorPane.isShowing()) {
            return;
        }
        
        if (delay <= 0) {
            setActivatedNodes(curEditorPane.getCaret().getDot());
        } else {
            myPreviousTask = RequestProcessor.getDefault().post(
                    new Runnable() {
                public void run() {
                    setActivatedNodes(curEditorPane.getCaret().getDot());
                }
            }, delay);
        }
    }
    
    private void selectElement() {
        selectElement(CARET_CHANGE_TASK_DELAY);
    }
    
    private transient MultiViewElementCallback myMultiViewObserver;
    private BPELDataObject myDataObject;
    private transient JToolBar myToolBar; 
    private CaretListener myCaretPositionListener;
    private ChangeEventListener myBpelModelListener;
    private transient RequestProcessor.Task myPreviousTask;
}
