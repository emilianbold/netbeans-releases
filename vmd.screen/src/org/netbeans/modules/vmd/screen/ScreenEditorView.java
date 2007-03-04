/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.screen;

import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.io.DataObjectContext;
import org.netbeans.modules.vmd.api.io.DesignDocumentAwareness;
import org.netbeans.modules.vmd.api.io.IOUtils;
import org.netbeans.modules.vmd.api.model.DesignDocument;
import org.netbeans.modules.vmd.screen.model.ScreenEditorModel;
import org.netbeans.modules.vmd.screen.ui.MainView;
import org.netbeans.modules.vmd.screen.ui.Toolbar;
import org.openide.awt.UndoRedo;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;

/**
 * ScrenEditorView component
 * 
 * @author breh
 */
public class ScreenEditorView implements DataEditorView, DesignDocumentAwareness {

    private static final long serialVersionUID = -1L;

    private transient DataObjectContext ctx;
        
    private transient JComponent loadingPanel;
    
    private transient JScrollPane topEditorComponent;
    private transient JPanel topEditorPanel;
    private transient ScreenEditorModel editorModel;
    
    private transient MainView screenEditorMainView;
    private transient Toolbar screenEditorToolbar;
    
    private static final String VIEW_NAME = NbBundle.getMessage(ScreenEditorView.class, "LBL_SCREEN_VIEW"); // NOI18N
    public static final String VIEW_ID = "screen"; // NOI18N
    
    
    // empty constructor for deseralization
    public ScreenEditorView() {
        
    }
    
    public ScreenEditorView (DataObjectContext context) {
        this.ctx = context;        
        init();        
        this.ctx.addDesignDocumentAwareness(this);
    }    

    // init the class
    private void init() {   
        this.editorModel = new ScreenEditorModel();
        
        topEditorPanel = new JPanel();
        topEditorComponent = new JScrollPane(topEditorPanel);
        
        topEditorPanel.setLayout(new GridBagLayout());
        screenEditorToolbar = new Toolbar(this.editorModel);
        screenEditorMainView = new MainView(this.editorModel);
        switchEditorPanels(true);
    }
    
    private void switchEditorPanels(boolean useLoadingPanel) {
        //System.err.println("Switching to loading: "+useLoadingPanel);
        topEditorPanel.removeAll();
        GridBagConstraints constraints = new GridBagConstraints();
        constraints.gridx = 0;
        constraints.gridy = 0;
        constraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        constraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        constraints.fill = java.awt.GridBagConstraints.BOTH;
        constraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        constraints.weightx = 1.0;
        constraints.weighty = 1.0;
        if (useLoadingPanel) {
            topEditorPanel.add(getLoadingPanel(),constraints);
        } else {
            topEditorPanel.add(getMainView(),constraints);
        }
        topEditorComponent.validate();
    }

    public boolean canShowSideWindows() {
        return true;
    }    

    private JComponent getLoadingPanel() {
        if (loadingPanel == null) {
            loadingPanel = IOUtils.createLoadingPanel();
        }
        return loadingPanel;
    }
    
    private JComponent getMainView() {
        return screenEditorMainView.getVisualComponent();
    }
    
    public DataObjectContext getContext() {
        return ctx;
    }

    public String getDisplayName() {
        return VIEW_NAME;
    }

    public int getEditPriority() {
        return getOrder();
    }

    public int getOpenPriority() {
        return - getOrder();
    }

    public int getOrder() {
        return 1000;
    }    
    
    public Kind getKind() {
        return Kind.MODEL;
    }

    public JComponent getToolbarRepresentation() {
        return screenEditorToolbar.getVisualComponent();
    }

    public JComponent getVisualRepresentation() {
        return topEditorComponent;
    }

    public String preferredID() {
        return VIEW_ID;
    }
    
    
    public void componentActivated () {
    }

    public void componentClosed() {
        // TODO Auto-generated method stub
    }

    public void componentDeactivated() {
        // TODO Auto-generated method stub
    }

    public void componentHidden() {
        // TODO Auto-generated method stub
    }

    public void componentOpened() {
        // TODO Auto-generated method stub
    }

    public void componentShowing() {
        // TODO Auto-generated method stub
    }

    public HelpCtx getHelpCtx() {
        // TODO Auto-generated method stub
        return null;
    }

    public UndoRedo getUndoRedo() {
        // TODO Auto-generated method stub
        return null;
    }

    private void writeObject (java.io.ObjectOutputStream out) throws IOException {
        out.writeObject (ctx);
    }

    private void readObject (java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        Object object = in.readObject ();
        if (! (object instanceof DataObjectContext))
            throw new ClassNotFoundException ("DataObjectContext expected but not found");
        ctx = (DataObjectContext) object;
        init();
        ctx.addDesignDocumentAwareness(this);
    }

    public void setDesignDocument(final DesignDocument designDocument) {
        IOUtils.runInAWTNoBlocking (new Runnable () {
            public void run() {
                System.err.println("ScreenEditorView.setDesignDocument("+designDocument+")");
                ScreenEditorView.this.editorModel.setDesign(designDocument, this);
                ScreenEditorView.this.switchEditorPanels(designDocument == null);
            }
        });
        
    }

}
