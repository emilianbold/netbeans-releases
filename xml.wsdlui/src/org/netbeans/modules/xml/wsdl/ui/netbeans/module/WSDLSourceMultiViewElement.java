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

package org.netbeans.modules.xml.wsdl.ui.netbeans.module;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.StyledDocument;

import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.xml.validation.ShowCookie;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.spi.Validator.ResultItem;
import org.openide.awt.UndoRedo;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author Jeri Lockhart
 */

public class WSDLSourceMultiViewElement extends CloneableEditor implements MultiViewElement {
    
    static final long serialVersionUID = 4403502726950453345L;
    
    transient private  JComponent toolbar;
    transient private  MultiViewElementCallback multiViewObserver;
    private WSDLDataObject wsdlDataObject;
    
    
    // Do NOT remove. Only for externalization //
    public WSDLSourceMultiViewElement() {
        super();
    }
    
    // Creates new editor //
    public WSDLSourceMultiViewElement(WSDLDataObject wsdlDataObject) {
        super(wsdlDataObject.getWSDLEditorSupport());
        this.wsdlDataObject = wsdlDataObject;
        
        // XXX: Please explain why this is being done.
        setActivatedNodes(new Node[] {wsdlDataObject.getNodeDelegate()});

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
        wsdlDataObject.getWSDLEditorSupport().initializeCloneableEditor(this);
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
        
        return "WSDLSourceMultiViewElementTC";  //  NOI18N
    }
    
    
    @Override
    public UndoRedo getUndoRedo() {
	return wsdlDataObject.getWSDLEditorSupport().getUndoManager();
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
	WSDLEditorSupport support = wsdlDataObject.getWSDLEditorSupport();
	JEditorPane[] editors = support.getOpenedPanes();
	if (editors == null || editors.length == 0) {
	    return support.silentClose();
	}
	return false;
    }

    public CloseOperationState canCloseElement() {
        // if this is not the last cloned xml editor component, closing is OK
        if (!WSDLEditorSupport.isLastView(multiViewObserver.getTopComponent())) {
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
        WSDLEditorSupport editor = wsdlDataObject.getWSDLEditorSupport();
        // Sync model before having undo manager listen to the model,
        // lest we get redundant undoable edits added to the queue.
        editor.syncModel();
        editor.removeUndoManagerFromDocument();
    }
    
    @Override
    public void componentActivated() {
        super.componentActivated();            
        WSDLEditorSupport editor = wsdlDataObject.getWSDLEditorSupport();
        editor.addUndoManagerToDocument();
    }
    
    @Override
    public void componentClosed() {
        super.componentClosed();
    }
    
    @Override
    public void componentShowing() {
        super.componentShowing();
        WSDLEditorSupport editor = wsdlDataObject.getWSDLEditorSupport();
        editor.addUndoManagerToDocument();
    }
    
    @Override
    public void componentHidden() {
        super.componentHidden();
        WSDLEditorSupport editor = wsdlDataObject.getWSDLEditorSupport();
        // Sync model before having undo manager listen to the model,
        // lest we get redundant undoable edits added to the queue.
        editor.syncModel();
        editor.removeUndoManagerFromDocument();
    }
    
    @Override
    public void componentOpened() {
        super.componentOpened();
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(wsdlDataObject);
    }
    
    @Override
    public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
        super.readExternal(in);
        Object firstObject = in.readObject();
        if (firstObject instanceof WSDLDataObject) {
            wsdlDataObject = (WSDLDataObject) firstObject;
            initialize();
        }
    }

    private void initialize()
    {
        ShowCookie showCookie = new ShowCookie()
        {
            
            public void show(ResultItem resultItem) {
                if(isActiveTC()) {
                    Component component = resultItem.getComponents();
                    if (component.getModel() == null) return; //may have been deleted.
                    
                    UIUtilities.annotateSourceView(wsdlDataObject, (DocumentComponent) component, 
                            resultItem.getDescription(), true);
                    if(component instanceof WSDLComponent) {
                        int position = ((WSDLComponent)component).findPosition();
                        getEditorPane().setCaretPosition(position);
                    } else {
                        int line = resultItem.getLineNumber();
                        try {
                            int position = NbDocument.findLineOffset(
                                    (StyledDocument)getEditorPane().getDocument(),line);
                            getEditorPane().setCaretPosition(position);
                        } catch (IndexOutOfBoundsException iob) {
                            // nothing
                        }
                    }
                }
            }
        };

        // create and associate lookup
        ProxyLookup lookup = new ProxyLookup(new Lookup[] {
            Lookups.fixed(new Object[] {
                // Need ActionMap in lookup so editor actions work.
                getActionMap(),
                // Need the data object registered in the lookup so that the
                // projectui code will close our open editor windows when the
                // project is closed.
                wsdlDataObject,
                // The Show Cookie in lookup to show schema component
                showCookie,
            }),
            wsdlDataObject.getNodeDelegate().getLookup(),
        });
        associateLookup(lookup);
    }
    
    protected boolean isActiveTC()
    {
        return getRegistry().getActivated() == multiViewObserver.getTopComponent();
    }
}
