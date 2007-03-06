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

package org.netbeans.modules.websvc.design.multiview;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.text.Document;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.awt.UndoRedo;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.DataEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.lookup.Lookups;

/**
 * The source editor element for JaxWS node.
 *
 * @author Ajit Bhate
 */
public class SourceMultiViewElement extends CloneableEditor
        implements MultiViewElement {
    private static final long serialVersionUID = 4403502726950453345L;
    private transient JComponent toolbar;
    private transient MultiViewElementCallback multiViewCallback;
    
    /**
     * Constructs a new instance of SourceMultiViewElement.
     */
    public SourceMultiViewElement() {
        // Needed for deserialization, do not remove.
        super(null);
    }
    
    /**
     * Constructs a new instance of SourceMultiViewElement.
     * 
     * @param support 
     */
    public SourceMultiViewElement(MultiViewSupport mvSupport, DataEditorSupport support) {
        super(support);
        initialize(mvSupport);
   }
    
    private void initialize(MultiViewSupport mvSupport) {
        if(mvSupport==null) {
            mvSupport = new MultiViewSupport(getDataObject().getNodeDelegate(),
                    getDataObject());
        }
        associateLookup(Lookups.fixed(
                mvSupport,
                getActionMap(),
                getDataObject(),
                getDataObject().getNodeDelegate()
                ));
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
    
    public void setMultiViewCallback(final MultiViewElementCallback callback) {
        multiViewCallback = callback;
    }
    
    public void requestVisible() {
        if (multiViewCallback != null)
            multiViewCallback.requestVisible();
        else
            super.requestVisible();
    }
    
    public void requestActive() {
        if (multiViewCallback != null)
            multiViewCallback.requestActive();
        else
            super.requestActive();
    }
    
    protected String preferredID() {
        return getClass().getName();
    }
    
    
    public UndoRedo getUndoRedo() {
        return super.getUndoRedo();
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
        return super.closeLast();
    }
    
    public CloseOperationState canCloseElement() {
        // if this is not the last cloned xml editor component, closing is OK
        if (!getEditorSupport().isModified() || 
                !MultiViewSupport.isLastView(multiViewCallback.getTopComponent())) {
            return CloseOperationState.STATE_OK;
        }
        // return a placeholder state - to be sure our CloseHandler is called
        return MultiViewFactory.createUnsafeCloseState(
                MultiViewSupport.SOURCE_UNSAFE_CLOSE,
                new AbstractAction() {
                    public void actionPerformed(ActionEvent arg0) {
                        //save changes
                        try {
                            getEditorSupport().saveDocument();
                            getEditorSupport().getDataObject().setModified(false);
                        } catch (IOException ex) {
                        }
                    }
                },
                new AbstractAction() {
                    public void actionPerformed(ActionEvent arg0) {
                        //discard changes
                    }
                });
    }
    
    public void componentActivated() {
        super.componentActivated();
        setActivatedNodes(new Node[] {getEditorSupport().getDataObject().getNodeDelegate()});
    }
    
    public void componentDeactivated() {
        super.componentDeactivated();
        setActivatedNodes(new Node[] {});
    }
    
    public void componentOpened() {
        super.componentOpened();
    }
    
    public void componentClosed() {
        super.componentClosed();
    }
    
    public void componentShowing() {
        super.componentShowing();
    }
    
    public void componentHidden() {
        super.componentHidden();
    }
    
    public void writeExternal(ObjectOutput out) throws IOException {
        // The superclass persists things such as the caret position.
        super.writeExternal(out);
        Object obj = getLookup().lookup(MultiViewSupport.class);
        if(obj!=null) {
            out.writeObject(obj);
        }
    }
    
    public void readExternal(ObjectInput in)
            throws IOException, ClassNotFoundException {
        super.readExternal(in);
	Object firstObject = in.readObject();
        MultiViewSupport mvSupport = null;
	if (firstObject instanceof MultiViewSupport ) {
	    mvSupport = (MultiViewSupport) mvSupport;
	}
	initialize(mvSupport);
    }
    
    private DataEditorSupport getEditorSupport() {
        return (DataEditorSupport) cloneableEditorSupport();
    }

    private DataObject getDataObject() {
        return getEditorSupport().getDataObject();
    }
}
