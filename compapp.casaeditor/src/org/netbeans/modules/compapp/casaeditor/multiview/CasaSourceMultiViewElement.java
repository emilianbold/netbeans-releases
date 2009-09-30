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

package org.netbeans.modules.compapp.casaeditor.multiview;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import org.openide.util.lookup.ProxyLookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.Lookup;
import org.openide.text.NbDocument;
import org.openide.text.CloneableEditor;
import org.netbeans.modules.compapp.casaeditor.CasaDataObject;
import org.netbeans.modules.compapp.casaeditor.CasaDataEditorSupport;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.core.spi.multiview.CloseOperationState;

import javax.swing.*;
import javax.swing.text.Document;
import org.openide.awt.UndoRedo;
import org.openide.nodes.Node;

/**
 *
 * @author Jeri Lockhart
 */
public class CasaSourceMultiViewElement extends CloneableEditor implements MultiViewElement {

    static final long serialVersionUID = -2074288729483750516L;
    
    transient private  JComponent toolbar;
    transient private  MultiViewElementCallback multiViewObserver;
    private CasaDataObject mDataObject;


    // Do NOT remove. Only for externalization //
    public CasaSourceMultiViewElement() {
        super();
    }

    public CasaSourceMultiViewElement(CasaDataObject obj) {
        super(obj.getEditorSupport());
        this.mDataObject = obj;

        // XXX: Please explain why this is being done.
        setActivatedNodes(new Node[] {mDataObject.getNodeDelegate()});

        // Initialize the editor support properly, which only needs to be
        // done when the editor is created (deserialization is working
        // due to CloneableEditor.readResolve() initializing the editor).
        // Note that this relies on the source view being the first in the
        // array of MultiViewDescription instances in WSDLMultiViewFactory,
        // since that results in the source view being created and opened
        // by default, only to be hidden when the DataObject default action
        // makes the tree view appear.
        // This initialization fixes CR 6380287 by ensuring that the Node
        // listener is registered with the DataObject Node delegate.
        mDataObject.getEditorSupport().initializeCloneableEditor(this);
        
        initialize();
    }

    public JComponent getToolbarRepresentation() {
        if (getEditorPane() != null) {
            Document doc = getEditorPane().getDocument();
            if (doc instanceof NbDocument.CustomToolbar) {
                if (toolbar == null) {
                    toolbar = ((NbDocument.CustomToolbar) doc).createToolbar(getEditorPane());
                }
                return toolbar;
            }
        }
        return null;
    }

    public JComponent getVisualRepresentation() {
        return this;
    }

    public void setMultiViewCallback(MultiViewElementCallback callback) {
        multiViewObserver = callback;
    }

    @Override
    public void requestVisible() {
        if (multiViewObserver != null)
            multiViewObserver.requestVisible();
        else
            super.requestVisible();
    }

    @Override
    public void requestActive() {
        if (multiViewObserver != null)
            multiViewObserver.requestActive();
        else
            super.requestActive();
    }

    @Override
    protected String preferredID() {
        return "CasaSourceMultiViewElementTC";  // NOI18N
    }

    @Override
    public UndoRedo getUndoRedo() {
        return null;//mDataObject.getEditorSupport().getUndoManager();
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
    @Override
    protected boolean closeLast() {
        CasaDataEditorSupport support = mDataObject.getEditorSupport();
        JEditorPane[] editors = support.getOpenedPanes();
        if (editors == null || editors.length == 0) {
            return support.silentClose();
        }
        return false;
    }

    public CloseOperationState canCloseElement() {
        // if this is not the last cloned xml editor component, closing is OK
        if (!CasaDataEditorSupport.isLastView(multiViewObserver.getTopComponent())) {
            return CloseOperationState.STATE_OK;
        }
        // return a placeholder state - to be sure our CloseHandler is called
        return MultiViewFactory.createUnsafeCloseState(
                "ID_TEXT_CLOSING", // dummy ID // NOI18N
                MultiViewFactory.NOOP_CLOSE_ACTION,
                MultiViewFactory.NOOP_CLOSE_ACTION);
    }

    @Override
    public void componentDeactivated() {
        super.componentDeactivated();
        CasaDataEditorSupport editor = mDataObject.getEditorSupport();
        // Sync model before having undo manager listen to the model,
        // lest we get redundant undoable edits added to the queue.
        if (mDataObject.isModified()) {
            editor.syncModel();
        }
        CasaMultiViewFactory.updateGroupVisibility(CasaSourceMultiViewDesc.PREFERRED_ID);
//        editor.removeUndoManagerFromDocument();
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
        CasaMultiViewFactory.updateGroupVisibility(CasaSourceMultiViewDesc.PREFERRED_ID);
//        CasaDataEditorSupport editor = mDataObject.getEditorSupport();
//        editor.addUndoManagerToDocument();
    }

    @Override
    public void componentClosed() {
        super.componentClosed();
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
//        CasaDataEditorSupport editor = mDataObject.getEditorSupport();
//        editor.addUndoManagerToDocument();
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
        CasaDataEditorSupport editor = mDataObject.getEditorSupport();
        // Sync model before having undo manager listen to the model,
        // lest we get redundant undoable edits added to the queue.
        if (mDataObject.isModified()) {
            editor.syncModel();
        }
//        editor.removeUndoManagerFromDocument();
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(mDataObject);
    }
    
    @Override
    public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
        super.readExternal(in);
        Object firstObject = in.readObject();
        if (firstObject instanceof CasaDataObject) {
            mDataObject = (CasaDataObject) firstObject;
            initialize();
        }
    }
    
    private void initialize() {
        // create and associate lookup
        ProxyLookup lookup = new ProxyLookup(new Lookup[] {
            Lookups.fixed(new Object[] {
                // Need ActionMap in lookup so editor actions work.
                getActionMap(),
                // Need the data object registered in the lookup so that the
                // projectui code will close our open editor windows when the
                // project is closed.
                mDataObject
            }),
            mDataObject.getNodeDelegate().getLookup(),
        });
        associateLookup(lookup);
    }

    protected boolean isActiveTC() {
        return getRegistry().getActivated() == multiViewObserver.getTopComponent();
    }
}
