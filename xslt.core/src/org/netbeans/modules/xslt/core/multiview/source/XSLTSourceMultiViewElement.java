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
package org.netbeans.modules.xslt.core.multiview.source;

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

//import org.netbeans.modules.soa.validation.core.Controller;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.xml.xam.ui.undo.QuietUndoManager;
import org.netbeans.modules.xslt.core.XSLTDataEditorSupport;
import org.netbeans.modules.xslt.core.XSLTDataObject;
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
 * @author Vitaly Bychkov
 */
public class XSLTSourceMultiViewElement extends CloneableEditor implements MultiViewElement {
    
    private static final long serialVersionUID = 1L;
    static final String PREFERED_ID="XsltSourceView";   // NOI18N
    private transient MultiViewElementCallback myMultiViewObserver;
    private XSLTDataObject myDataObject;
    private transient JToolBar myToolBar;
    
    // for deserialization
    private XSLTSourceMultiViewElement() {
        super();
    }
    
    public XSLTSourceMultiViewElement( XSLTDataObject dataObject ) {
        super(dataObject.getEditorSupport());
        myDataObject = dataObject;
        // ================================================================
        // Initialize the editor support properly, which only needs to be
        // done when the editor is created (deserialization is working
        // due to CloneableEditor.readResolve() initializing the editor).
        // Note that this relies on the source view being the first in the
        // array of MultiViewDescription instances in XsltMultiViewSupport,
        // since that results in the source view being created and opened
        // by default, only to be hidden when the DataObject default action
        // makes the columns view appear.
        // This initialization fixes CR 6349089 by ensuring that the Node
        // listener is registered with the DataObject Node delegate.
        getDataObject().getEditorSupport().initializeCloneableEditor(this);
        
        initialize();
    }
    
    private XSLTDataObject getDataObject() {
        return myDataObject;
    }
    
    private void initialize() {
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
        if ( obj instanceof XSLTDataObject) {
            myDataObject = (XSLTDataObject) obj;
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
        XSLTDataEditorSupport editor = getDataObject().getEditorSupport();
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
        XSLTDataEditorSupport editor = getDataObject().getEditorSupport();
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
        setActivatedNodes(new Node[0]);
        setActivatedNodes(new Node[] { getDataObject().getNodeDelegate() });
        addUndoManager();
//      getValidationController().triggerValidation(); // todo r
    }

// todo r 
//    private Controller getValidationController() {
//      return (Controller) getDataObject().getLookup().lookup(Controller.class);
//    }

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
        addUndoManager();
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
        XSLTDataEditorSupport editor = myDataObject.getEditorSupport();
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
        XSLTDataEditorSupport editor = getDataObject().getEditorSupport();
        JEditorPane[] editors = editor.getOpenedPanes();
        if (editors == null || editors.length == 0) {
            return editor.silentClose();
        }
        return false;
    }
}
