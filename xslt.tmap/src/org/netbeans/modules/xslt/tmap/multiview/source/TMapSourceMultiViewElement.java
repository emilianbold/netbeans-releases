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
package org.netbeans.modules.xslt.tmap.multiview.source;

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
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;

import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.xml.xam.ui.undo.QuietUndoManager;
import org.netbeans.modules.xslt.tmap.TMapDataEditorSupport;
import org.netbeans.modules.xslt.tmap.TMapDataObject;
import org.openide.awt.UndoRedo;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.CloneableTopComponent;
import org.openide.windows.TopComponent;

/**
 *
 * @author Vitaly Bychkov
 * @version 1.0
 */
public class TMapSourceMultiViewElement extends CloneableEditor
        implements MultiViewElement  
{
    
    private static final long serialVersionUID = 1L;
    static final String PREFERED_ID="TMapSourceView";   // NOI18N
    private transient MultiViewElementCallback myMultiViewObserver;
    private TMapDataObject myDataObject;
    private transient JToolBar myToolBar;
    
    // for deserialization
    private TMapSourceMultiViewElement() {
        super();
    }
    
    public TMapSourceMultiViewElement( TMapDataObject dataObject ) {
        super(dataObject.getEditorSupport());
        myDataObject = dataObject;
        // ================================================================
        // Initialize the editor support properly, which only needs to be
        // done when the editor is created (deserialization is working
        // due to CloneableEditor.readResolve() initializing the editor).
        // Note that this relies on the source view being the first in the
        // array of MultiViewDescription instances in TMapMultiViewSupport,
        // since that results in the source view being created and opened
        // by default, only to be hidden when the DataObject default action
        // makes the columns view appear.
        // This initialization fixes CR 6349089 by ensuring that the Node
        // listener is registered with the DataObject Node delegate.
        getDataObject().getEditorSupport().initializeCloneableEditor(this);
        
        initialize();
    }
    
    private TMapDataObject getDataObject() {
        return myDataObject;
    }
    
    private void initialize() {
//        Node delegate = getDataObject().getNodeDelegate();
//        SourceCookieProxyLookup lookup = new SourceCookieProxyLookup(new Lookup[] {
//            Lookups.fixed(new Object[] {
//                // Need ActionMap in lookup so editor actions work.
//                getActionMap(),
//                // Need the data object registered in the lookup so that the
//                // projectui code will close our open editor windows when the
//                // project is closed.
////                myDataObject
//                getDataObject(),
//                getDataObject().getLookup()
//            }),
//        }, delegate);
//        associateLookup(lookup);
//        addPropertyChangeListener("activatedNodes", lookup);

      associateLookup(new ProxyLookup(new Lookup[] { // # 67257
        Lookups.fixed(new Object[] {
          getActionMap(), // # 85512
          getDataObject(),
          getDataObject().getNodeDelegate() }),
          getDataObject().getLookup() })); // # 117029
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(myDataObject);
    }
    
    /**
     * we are using Externalization semantics so that we can get a hook to call
     * initialize() upon deserialization
     */
    public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
        super.readExternal(in);
        Object obj = in.readObject();
        if ( obj instanceof TMapDataObject) {
            myDataObject = (TMapDataObject) obj;
        }
        initialize();
    }
    
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    /////////////////////////// MultiViewElement  //////////////////////////////
    ////////////////////////////////////////////////////////////////////////////
    
    /**
     * Adds the undo/redo manager to the document as an undoable
     * edit listener, so it receives the edits onto the queue.
     */
    private void addUndoManager() {
        TMapDataEditorSupport editor = getDataObject().getEditorSupport();
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
        TMapDataEditorSupport editor = getDataObject().getEditorSupport();
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
        } else {
            return MultiViewFactory.createUnsafeCloseState(
                    "Data Object Modified",             // NOI18N
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
        // Set our activated nodes to kick Undo/Redo into action.
        // Need to do it twice in the event we are switching from another
        // multiview element that has the same activated nodes, in which
        // case no events are fired and so the UndoAction does not
        // register for changes with our undo manager.
//        setActivatedNodes(new Node[0]);
//        setActivatedNodes(new Node[] { getDataObject().getNodeDelegate() });
//        addUndoManager();
        TMapDataEditorSupport editor = getDataObject().getEditorSupport();
        editor.addUndoManagerToDocument();
    }
    
    public void componentClosed() {
        super.componentClosed();
        
        /*
         *  Avoid memory leak. The first call is good it seems.
         *
         *  The second is like a hack. But this works and could be a problem
         *  only when this MultiviewElement will be reused after reopening.
         *  It seems this is not a case - each time when editor is opened it is
         *  instantiated.
         */
        setMultiViewCallback( null );
        if ( getParent()!= null ){
            getParent().remove( this );
        }
        
    }
    
    public JComponent getToolbarRepresentation() {
        Document doc = getEditorPane().getDocument();
        if (doc instanceof NbDocument.CustomToolbar) {
            if (myToolBar == null) {
                myToolBar = ((NbDocument.CustomToolbar) doc).createToolbar(getEditorPane());
            }
            return myToolBar;
        }
        return null;
    }
    
    public JComponent getVisualRepresentation() {
        return this;
    }
    
    public void componentDeactivated() {
        super.componentDeactivated();
//        removeUndoManager();
        TMapDataEditorSupport editor = getDataObject().getEditorSupport();
        // Sync model before having undo manager listen to the model,
        // lest we get redundant undoable edits added to the queue.
        editor.syncModel();
        editor.removeUndoManagerFromDocument();
    }
    
    
    public void componentHidden() {
        super.componentHidden();
//        removeUndoManager();
//        getDataObject().getEditorSupport().syncModel();
        TMapDataEditorSupport editor = getDataObject().getEditorSupport();
        // Sync model before having undo manager listen to the model,
        // lest we get redundant undoable edits added to the queue.
        editor.syncModel();
        editor.removeUndoManagerFromDocument();
    }
    
    public void componentOpened() {
        super.componentOpened();
    }
    
    public void componentShowing() {
        super.componentShowing();
//        addUndoManager();
        TMapDataEditorSupport editor = getDataObject().getEditorSupport();
        editor.addUndoManagerToDocument();
    }
    
    public void setMultiViewCallback( final MultiViewElementCallback callback) {
        myMultiViewObserver = callback;
    }
    
    public void requestVisible() {
        if (myMultiViewObserver != null) {
            myMultiViewObserver.requestVisible();
        } else {
            super.requestVisible();
        }
    }
    
    public void requestActive() {
        if (myMultiViewObserver != null) {
            myMultiViewObserver.requestActive();
        } else {
            super.requestActive();
        }
    }
    
    public UndoRedo getUndoRedo() {
        TMapDataEditorSupport editor = myDataObject.getEditorSupport();
        return editor.getUndoManager();
    }
    
    protected String preferredID() {
        return PREFERED_ID;
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
        TMapDataEditorSupport editor = getDataObject().getEditorSupport();
        JEditorPane[] editors = editor.getOpenedPanes();
        if (editors == null || editors.length == 0) {
            return editor.silentClose();
        }
        return false;
    }
}
