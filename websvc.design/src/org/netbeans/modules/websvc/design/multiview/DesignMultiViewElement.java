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

import java.awt.BorderLayout;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import org.openide.windows.TopComponent;
import org.openide.awt.UndoRedo;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.websvc.design.navigator.WSDesignNavigatorHint;
import org.netbeans.modules.websvc.design.view.DesignView;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Ajit Bhate
 */
public class DesignMultiViewElement extends TopComponent
        implements MultiViewElement {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    private transient MultiViewElementCallback multiViewCallback;
    private transient JToolBar toolbar;
    private transient DesignView designView;
    private transient DataObject dataObject;

    /**
     * 
     * @param mvSupport 
     */
    public DesignMultiViewElement(DataObject dataObject) {
        this.dataObject = dataObject;
        initialize();
    }
    
    private void initialize() {
        associateLookup(Lookups.fixed(dataObject, new WSDesignNavigatorHint()));
    }
    
    public int getPersistenceType() {
        return PERSISTENCE_NEVER;
    }
    
    public void setMultiViewCallback(MultiViewElementCallback callback) {
        multiViewCallback = callback;
    }
    
    public CloseOperationState canCloseElement() {
        return CloseOperationState.STATE_OK;
    }
    
    
    /**
     * Initializes the UI. Here it checks for the state of the underlying
     * schema model. If valid, draws the UI, else empties the UI with proper
     * error message.
     */
    private void initUI() {
        removeAll();
        setLayout(new BorderLayout());
        MultiViewSupport mvSupport = dataObject.getCookie(MultiViewSupport.class);
        if (mvSupport!=null) {
            designView = new DesignView(mvSupport.getService(),mvSupport.getImplementationBean());
            add(designView);
        } else {
            JLabel emptyLabel = new JLabel("switch to source view.");
            add(emptyLabel,BorderLayout.CENTER);
        }
    }
    
    
    public void componentActivated() {
        super.componentActivated();
        setActivatedNodes(new Node[] {});
        setActivatedNodes(new Node[] {dataObject.getNodeDelegate()});
    }
    
    public void componentDeactivated() {
        super.componentDeactivated();
        setActivatedNodes(new Node[] {});
    }
    
    public void componentOpened() {
        super.componentOpened();
        // create UI, this will be moved to componentShowing for refresh/sync
        initUI();
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
    
    public JComponent getToolbarRepresentation() {
        // This is called every time user switches between elements.
        if (toolbar == null) {
            toolbar = new JToolBar();
            toolbar.setFloatable(false);
            designView.addToolbarActions(toolbar);
        }
        return toolbar;
    }
    
    public UndoRedo getUndoRedo() {
        return super.getUndoRedo();
    }
    
    public JComponent getVisualRepresentation() {
        return this;
    }
    
}
