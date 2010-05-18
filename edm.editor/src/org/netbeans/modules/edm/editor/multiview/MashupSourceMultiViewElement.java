/*
 * The contents of this file are subject to the terms of the Common
 * Development and Distribution License (the License). You may not use this 
 * file except in compliance with the License.  You can obtain a copy of the
 *  License at http://www.netbeans.org/cddl.html
 *
 * When distributing Covered Code, include this CDDL Header Notice in each
 * file and include the License. If applicable, add the following below the
 * CDDL Header, with the fields enclosed by brackets [] replaced by your own
 * identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Copyright 2006 Sun Microsystems, Inc. All Rights Reserved
 *
 */
package org.netbeans.modules.edm.editor.multiview;

import org.netbeans.modules.edm.editor.dataobject.MashupDataEditorSupport;
import org.netbeans.modules.edm.editor.dataobject.MashupDataObject;
import org.openide.cookies.SaveCookie;
import org.openide.text.NbDocument;
import org.openide.text.CloneableEditor;
import org.openide.nodes.Node;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.core.spi.multiview.CloseOperationState;

import javax.swing.*;
import javax.swing.text.Document;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectInput;

public class MashupSourceMultiViewElement extends CloneableEditor implements MultiViewElement {

    static final long serialVersionUID = 4403502726950453345L;
    transient private JComponent toolbar;
    transient private MultiViewElementCallback multiViewObserver;
    private MashupDataObject mObj;
    
    
    // Do NOT remove. Only for externalization //
    public MashupSourceMultiViewElement() {
        super();
    }
    // Creates new editor //
    public MashupSourceMultiViewElement(MashupDataObject obj) {
        super(obj.getMashupDataEditorSupport());
        this.mObj = obj;

        setActivatedNodes(new Node[]{obj.getNodeDelegate()});
        initialize();
    }

    public JComponent getToolbarRepresentation() {
        Document doc = getEditorPane().getDocument();
        if (doc instanceof NbDocument.CustomToolbar) {
            if (toolbar == null) {
                toolbar = ((NbDocument.CustomToolbar) doc).createToolbar(getEditorPane());
            }
            return toolbar;
        }
        return null;
    }

    public JComponent getVisualRepresentation() {
        return this;
    }

    public void setMultiViewCallback(MultiViewElementCallback callback) {
        multiViewObserver = callback;
        MashupDataEditorSupport editor = mObj.getMashupDataEditorSupport();
        editor.setTopComponent(callback.getTopComponent());
    }

    @Override
    public void requestVisible() {
        if (multiViewObserver != null) {
            multiViewObserver.requestVisible();
        } else {
            super.requestVisible();
        }
    }

    @Override
    public void requestActive() {
        if (multiViewObserver != null) {
            multiViewObserver.requestActive();
        } else {
            super.requestActive();
        }
    }

    @Override
    protected String preferredID() {
        return "MashupSourceMultiViewElementTC";  //  NOI18N
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
        MashupDataEditorSupport support = mObj.getMashupDataEditorSupport();
        JEditorPane[] editors = support.getOpenedPanes();
        if (editors == null || editors.length == 0) {
            return support.silentClose();
        }
        return false;
    }

    public CloseOperationState canCloseElement() {
        // if this is not the last cloned xml editor component, closing is OK
        if (!MashupDataEditorSupport.isLastView(multiViewObserver.getTopComponent())) {
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
        SaveCookie cookie = (SaveCookie) mObj.getCookie(SaveCookie.class);
        if (cookie != null) {
            mObj.getMashupDataEditorSupport().syncModel();
        }
    }

    @Override
    public void componentActivated() {
        super.componentActivated();
        setActivatedNodes(new Node[0]);
        setActivatedNodes(new Node[]{mObj.getNodeDelegate()});
    }

    @Override
    public void componentClosed() {
        super.canClose(null, true);
        super.componentClosed();
    }

    @Override
    public void componentShowing() {
        super.componentShowing();
        SaveCookie cookie = (SaveCookie) mObj.getCookie(SaveCookie.class);
        if (cookie != null) {
            mObj.getMashupDataEditorSupport().synchDocument();
        }
    }

    @Override
    public void componentHidden() {
        super.componentHidden();
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
    }

    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(mObj);
    }

    @Override
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        super.readExternal(in);
        Object firstObject = in.readObject();
        if (firstObject instanceof MashupDataObject) {
            mObj = (MashupDataObject) firstObject;
            initialize();
        }
    }

    private void initialize() {
    }

    protected boolean isActiveTC() {
        return getRegistry().getActivated() == multiViewObserver.getTopComponent();
    }
}