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

package org.netbeans.modules.bpel.core.multiview;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Enumeration;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JToolBar;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;

import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.bpel.core.BPELDataEditorSupport;
import org.netbeans.modules.bpel.core.BPELDataObject;
import org.netbeans.modules.xml.xam.ui.undo.QuietUndoManager;
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
 * @author ads
 */
public class BPELSourceMultiViewElement extends CloneableEditor
	implements MultiViewElement  {
    
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
	
	private BPELDataObject getDataObject() {
		return myDataObject;
	}
	
	private void initialize() {
        /**
         * only thing which works to make the XmlNav show for this MVElement see
         * (http://www.netbeans.org/issues/show_bug.cgi?id=67257)
         */
        associateLookup(new ProxyLookup(new Lookup[] {
        // Lookups.singleton(getDataObject().getNodeDelegate())
                Lookups.fixed(new Object[] {
                        // Need ActionMap in lookup so editor actions work.
                        // Fix for #85512.
                        getActionMap(),
                        getDataObject(),
                        getDataObject().getNodeDelegate() }) }));
        /*
         * getDataObject() is put here because method createMultiView in 
         * BpelMultiviewSupport is create TopComponent with lookup
         * that retrieved from defaultDesc . This defaultDesc is source 
         * description. So I put into lookup dataOject.
         * It will be used later for activate Design view.
         * Second object in lookup is "hack" that described above.
         */
        
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
		throws IOException, ClassNotFoundException 
    {
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

	////////////////////////////////////////////////////////////////////////////
    /////////////////////////// MultiViewElement  //////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

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
				"Data Object Modified",             // NOI18N
				MultiViewFactory.NOOP_CLOSE_ACTION,
				MultiViewFactory.NOOP_CLOSE_ACTION);			
		}
    }
	
    public void componentActivated() {
		super.componentActivated();
        // Set our activated nodes to kick Undo/Redo into action.
        // Need to do it twice in the event we are switching from another
        // multiview element that has the same activated nodes, in which
        // case no events are fired and so the UndoAction does not
        // register for changes with our undo manager.
        setActivatedNodes(new Node[0]);
        setActivatedNodes(new Node[] { getDataObject().getNodeDelegate() });
        addUndoManager();
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
    
    public void componentDeactivated() {
		super.componentDeactivated();
        removeUndoManager();
        getDataObject().getEditorSupport().syncModel();
    }
    
    public void componentHidden() {
		super.componentHidden();
        removeUndoManager();
        getDataObject().getEditorSupport().syncModel();
    }
    
    public void componentOpened() {
		super.componentOpened();
    }
    
    public void componentShowing() {
		super.componentShowing();
        /*BPELDataEditorSupport editor = getDataObject().getEditorSupport();
        // If the bpel model is valid, discard the edits on the editor
        // support's undo queue. The idea is to keep our undo/redo model as
        // simple as can be. Otherwise, the two undo managers would need to
        // be kept in sync, making the undo/redo code vastly more complicated.

        BpelModel model = editor.getBpelModel();
        if (model.getState().equals(Model.State.VALID)) {
            editor.getUndoManager().discardAllEdits();
        }*/
        addUndoManager();

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
    
    public void setMultiViewCallback( final MultiViewElementCallback callback) {
        myMultiViewObserver = callback;
        // TODO : need to call setTopComponent for editor support like Schema?
        /*getDataObject().getEditorSupport().setTopComponent( 
                callback.getTopComponent());*/
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

    
    private transient MultiViewElementCallback myMultiViewObserver;
    
    private BPELDataObject myDataObject;
    
    private transient JToolBar myToolBar; 
}




