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
package org.netbeans.modules.vmd.io.editor;

import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.providers.IOSupport;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

import javax.swing.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author David Kaspar
 */
public class EditorViewElement implements MultiViewElement, Serializable {
    
    private static final long serialVersionUID = -1;
    
    private static final String CLOSING_ID = "ID_JAVA_CLOSING"; // NOI18N
    
    private DataObjectContext context;
    private DataEditorView view;
    private transient DataEditorView.Kind kind;
    private transient Lookup lookup;
    private transient EditorTopComponent topComponent;
    private transient MultiViewElementCallback callback;

    public EditorViewElement() {
    }
    
    public EditorViewElement(DataObjectContext context, DataEditorView view) {
        this.context = context;
        this.view = view;
        init();
    }
    
    private void init() {
        kind = view.getKind();
        ArrayList<Object> lookupObjects = DataEditorViewLookupFactoryRegistry.getLookupObjects(context, view);
        ArrayList<Lookup> lookups = DataEditorViewLookupFactoryRegistry.getLookups(context, view);
        lookupObjects.add(view);
        lookups.add(Lookups.fixed(lookupObjects.toArray()));
        lookup = new ProxyLookup(lookups.toArray(new Lookup[lookups.size()]));
        IOSupport.getDocumentSerializer(context.getDataObject()).startLoadingDocument();
    }
    
    public JComponent getVisualRepresentation() {
        if (topComponent == null) {
            JComponent visualRepresentation = view.getVisualRepresentation();
            if (visualRepresentation != null) {
                topComponent = kind == DataEditorView.Kind.CODE ? new CodeEditorTopComponent(context, lookup, visualRepresentation) : new EditorTopComponent(context, lookup, visualRepresentation);
            }
        }
        return topComponent;
    }
    
    public JComponent getToolbarRepresentation() {
        return view.getToolbarRepresentation();
    }
    
    public Action[] getActions() {
        return callback != null ? callback.createDefaultActions() : new Action[0];
    }
    
    public Lookup getLookup() {
        getVisualRepresentation();
        return topComponent.getLookup();
    }
    
    public void componentOpened() {
        view.componentOpened();
    }
    
    public void componentClosed() {
        view.componentClosed();
    }
    
    public void componentShowing() {
        view.componentShowing();
    }
    
    public void componentHidden() {
        view.componentHidden();
    }
    
    public void componentActivated() {
        IOSupport.notifyDataEditorViewActivated(view);
        view.componentActivated();
    }
    
    public void componentDeactivated() {
        view.componentDeactivated();
    }
    
    public UndoRedo getUndoRedo() {
        UndoRedo undoRedo = view.getUndoRedo();
        if (undoRedo != null)
            return undoRedo;
        if (kind != DataEditorView.Kind.MODEL)
            return null;
        return IOSupport.getDocumentSerializer(context.getDataObject()).getUndoRedoManager();
    }
    
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        this.callback = callback;
        IOSupport.getDataObjectInteface(context.getDataObject()).setMVTC(callback.getTopComponent());
    }
    
    public CloseOperationState canCloseElement() {
        return MultiViewFactory.createUnsafeCloseState(CLOSING_ID, null, null);
    }
    
    private void writeObject(java.io.ObjectOutputStream out) throws IOException {
        out.writeObject(context);
        out.writeObject(view);
    }
    
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        Object object = in.readObject();
        if (! (object instanceof DataObjectContext))
            throw new ClassNotFoundException("DataObjectContext expected but not found"); // NOI18N
        context = (DataObjectContext) object;
        object = in.readObject();
        if (! (object instanceof DataEditorView))
            throw new ClassNotFoundException("DataEditorView expected but not found"); // NOI18N
        view = (DataEditorView) object;
        init();
    }
    
}
