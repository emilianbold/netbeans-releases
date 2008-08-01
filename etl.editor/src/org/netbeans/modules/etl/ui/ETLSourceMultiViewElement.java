/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.

If you wish your version of this file to be governed by only the CDDL
or only the GPL Version 2, indicate your decision by adding
"[Contributor] elects to include this software in this distribution
under the [CDDL or GPL Version 2] license." If you do not indicate a
single choice of license, a recipient has the option to distribute
your version of this file under either the CDDL, the GPL Version 2 or
to extend the choice of license to its licensees as provided above.
However, if you add GPL Version 2 code and therefore, elected the GPL
Version 2 license, then the option applies only if the new code is
made subject to such option by the copyright holder.
 */

/*
 * ETLSourceMultiViewElement.java
 *
 * Created on October 13, 2005, 2:36 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.etl.ui;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import java.util.ArrayList;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.text.Document;

import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.openide.actions.FileSystemAction;
import org.openide.awt.UndoRedo;
import org.openide.cookies.SaveCookie;
import org.openide.nodes.Node;
import org.openide.text.CloneableEditor;
import org.openide.text.NbDocument;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
/**
 *
 * @author Jeri Lockhart
 */

public class ETLSourceMultiViewElement extends CloneableEditor implements MultiViewElement {
    
    static final long serialVersionUID = 4403502726950453345L;
    
    transient private  JComponent toolbar;
    transient private  MultiViewElementCallback multiViewObserver;
    private ETLDataObject etlDataObject;
    
    
    // Do NOT remove. Only for externalization //
    public ETLSourceMultiViewElement() {
        super();
    }
    
    // Creates new editor //
    public ETLSourceMultiViewElement(ETLDataObject etlDataObject) {
        super(etlDataObject.getETLEditorSupport());
        this.etlDataObject = etlDataObject;
        initialize();
        
        setActivatedNodes(new Node[] {etlDataObject.getNodeDelegate()});
    }
    
    private void initialize() {
        /**
         * only thing which works to make the XmlNav show for this MVElement see
         * (http://www.netbeans.org/issues/show_bug.cgi?id=67257)
         */
        associateLookup(new ProxyLookup(new Lookup[] {
            Lookups.fixed(new Object[] {
                getActionMap(),
                etlDataObject,
                etlDataObject.getNodeDelegate() }) }));
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
        ETLEditorSupport editor = etlDataObject.getETLEditorSupport();
        editor.setTopComponent(callback.getTopComponent());
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
    protected boolean closeLast() {
        return true;
    }
    
    
    @Override
    public org.openide.awt.UndoRedo getUndoRedo() {
        ETLEditorSupport editor = etlDataObject.getETLEditorSupport();
        return editor.getUndoManager();
    }
    
    public CloseOperationState canCloseElement() {
        // if this is not the last cloned xml editor component, closing is OK
        if (!ETLEditorSupport.isLastView(multiViewObserver.getTopComponent()))
            return CloseOperationState.STATE_OK;
        
        // return a placeholder state - to be sure our CloseHandler is called
        return MultiViewFactory.createUnsafeCloseState(
                "ID_TEXT_CLOSING", // dummy ID // NOI18N
                MultiViewFactory.NOOP_CLOSE_ACTION,
                MultiViewFactory.NOOP_CLOSE_ACTION);
    }
    
    // org.openide.text.CloneableEditor  - default PERSISTENCE_ONLY_OPENED
    @Override
    public int getPersistenceType() {
        return PERSISTENCE_ONLY_OPENED;
    }
    
    @Override
    public void componentDeactivated() {
        SaveCookie cookie = (SaveCookie)etlDataObject.getCookie(SaveCookie.class);
        if(cookie != null){
            etlDataObject.getETLEditorSupport().syncModel();
        }
        super.componentDeactivated();
        // When the user is switching from the source view sync the text
        // with the etl model (treat it as a focus change).
        
    }
    
    @Override
    public void componentActivated() {
        super.componentActivated();
        setActivatedNodes(new Node[0]);
        setActivatedNodes(new Node[] { etlDataObject.getNodeDelegate() });        
        DataObjectProvider.activeDataObject = etlDataObject;
    }
    
    @Override
    public void componentClosed() {
        super.canClose(null, true);
        super.componentClosed();
    }
    
    @Override
    public void componentShowing() {
        super.componentShowing();
        ETLEditorSupport editor = etlDataObject.getETLEditorSupport();
        SaveCookie cookie = (SaveCookie)etlDataObject.getCookie(SaveCookie.class);
        if(cookie != null){
            editor.synchDocument();
        }
        UndoRedo.Manager undoRedo = editor.getUndoManager();
        Document document = editor.getDocument();
        document.addUndoableEditListener(undoRedo);
        DataObjectProvider.activeDataObject = etlDataObject;
    }
    
    @Override
    public void componentHidden() {
        super.componentHidden();
        // When the user is switching from the source view sync the text
        // with the wsdl model (treat it as a focus change).
        etlDataObject.getETLEditorSupport().syncModel();
        etlDataObject.getETLEditorSupport().removeUndoManagerFromDocument();
    }
    
    @Override
    public void componentOpened() {
        super.componentOpened();
        DataObjectProvider.activeDataObject = etlDataObject;
    }
    
    @Override
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal(out);
        out.writeObject(etlDataObject);
    }
    
    @Override
    public void readExternal(ObjectInput in)
    throws IOException, ClassNotFoundException {
        super.readExternal(in);
        Object firstObject = in.readObject();
        if (firstObject instanceof ETLDataObject) {
            etlDataObject = (ETLDataObject) firstObject;
        }
    }

   @Override
    public Action[] getActions() {
        ArrayList<Action> actionsList = new ArrayList<Action>();
        for (Action action : super.getActions()) {            
            //FileSystemAction gets added from addFromLayers().commenting this will make Local History option appear twice
            if(!(action instanceof FileSystemAction))
            actionsList.add(action);
        }
        actionsList.addAll(Utilities.actionsForPath("Projects/Actions"));
        Action[] actions = new Action[actionsList.size()];
        actionsList.toArray(actions);
        return actions;
    }

}
