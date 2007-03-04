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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.io.IOException;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

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

/**
 * 
 * ScrenEditorView component
 * 
 * @author breh
 *
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
    
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.io.DataEditorView#canShowSideWindows()
     */
    public boolean canShowSideWindows() {
        // TODO Auto-generated method stub
        return false;
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
    
   
    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.io.DataEditorView#getContext()
     */
    public DataObjectContext getContext() {
        return ctx;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.io.DataEditorView#getDisplayName()
     */
    public String getDisplayName() {
        return VIEW_NAME;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.io.DataEditorView#getEditPriority()
     */
    public int getEditPriority() {
        return getOrder();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.io.DataEditorView#getOpenPriority()
     */
    public int getOpenPriority() {
        return - getOrder();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.io.DataEditorView#getOrder()
     */
    public int getOrder() {
        return 1000;
    }    
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.io.DataEditorView#getKind()
     */
    public Kind getKind() {
        return Kind.MODEL;
    }


    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.io.DataEditorView#getToolbarRepresentation()
     */
    public JComponent getToolbarRepresentation() {
        return screenEditorToolbar.getVisualComponent();
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.io.DataEditorView#getVisualRepresentation()
     */
    public JComponent getVisualRepresentation() {
        return topEditorComponent;
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.io.DataEditorView#preferredID()
     */
    public String preferredID() {
        return VIEW_ID;
    }
    
    
    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.io.DataEditorView#componentActivated()
     */
    public void componentActivated () {
    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.io.DataEditorView#componentClosed()
     */
    public void componentClosed() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.io.DataEditorView#componentDeactivated()
     */
    public void componentDeactivated() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.io.DataEditorView#componentHidden()
     */
    public void componentHidden() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.io.DataEditorView#componentOpened()
     */
    public void componentOpened() {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.io.DataEditorView#componentShowing()
     */
    public void componentShowing() {
        // TODO Auto-generated method stub

    }



    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.io.DataEditorView#getHelpCtx()
     */
    public HelpCtx getHelpCtx() {
        // TODO Auto-generated method stub
        return null;
    }



    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.io.DataEditorView#getUndoRedo()
     */
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

    /* (non-Javadoc)
     * @see org.netbeans.modules.vmd.api.io.DesignDocumentAwareness#setDesignDocument(org.netbeans.modules.vmd.api.model.DesignDocument)
     */
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
