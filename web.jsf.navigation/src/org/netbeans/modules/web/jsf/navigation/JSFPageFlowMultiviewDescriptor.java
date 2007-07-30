
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
 *
 *
 * JSFTestMultiviewDescriptor.java
 *
 * Created on February 7, 2007, 6:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation;

import java.awt.Image;
import java.io.IOException;
import java.io.Serializable;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JComponent;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext;
import org.openide.DialogDescriptor;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author Joelle Lam
 */
public class JSFPageFlowMultiviewDescriptor implements MultiViewDescription, Serializable{
    static final long serialVersionUID = -3101808890387485990L;
    //    private static final long serialVersionUID = -6305897237371751567L;
    //        static final long serialVersionUID = -6305897237371751567L;
    
    
    private JSFConfigEditorContext context;
    private static final String PAGEFLOW = NbBundle.getMessage(JSFPageFlowMultiviewDescriptor.class, "LBL_PageFlow");
    
    
    /**
     * This is the multiview descripture which defines a new pane in the faces configuration xml multiview editor.
     */
    public JSFPageFlowMultiviewDescriptor() {
    }
    
    /**
     * This is the multiview descripture which defines a new pane in the faces configuration xml multiview editor.
     * @param context the JSFConfigEditorContext
     **/
    public JSFPageFlowMultiviewDescriptor(JSFConfigEditorContext context) {
        this.context = context;
    }
    
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }
    
    public String getDisplayName() {
        return PAGEFLOW;
    }
    
    private static final Image JSFConfigIcon = org.openide.util.Utilities.loadImage("org/netbeans/modules/web/jsf/resources/JSFConfigIcon.png"); // NOI18N
    public Image getIcon() {
        //        return PageFlowImage;
        return JSFConfigIcon;
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    public String preferredID() {
        return PAGEFLOW;
    }
    
    public MultiViewElement createElement() {
        return new PageFlowElement(context);
    }
    
    
    static class PageFlowElement implements MultiViewElement, Serializable {
        //        private transient JScrollPane panel;
        private transient PageFlowView tc;
        private transient JComponent toolbar;
        static final long serialVersionUID = 5454879177214643L;
        private JSFConfigEditorContext context;
        
        
        public PageFlowElement(JSFConfigEditorContext context) {
            this.context = context;
            init();
        }
        
        private void init() {
            getTopComponent().setName(context.getFacesConfigFile().getName());
        }
        
        public JComponent getVisualRepresentation() {
            return tc;
        }
        
        public JComponent getToolbarRepresentation() {
            if (toolbar == null) {
                toolbar = getTopComponent().getToolbarRepresentation();
            }
            return toolbar;
        }
        
        private PageFlowView getTopComponent() {
            if( tc == null ) {
                tc = new PageFlowView(this, context);
            }
            return tc;
        }
        
        public Action[] getActions() {
            try {
                DataObject dataObject = org.openide.loaders.DataObject.find(context.getFacesConfigFile());
                
                return dataObject.getNodeDelegate().getActions(false);
            } catch (DataObjectNotFoundException ex) {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                        ex.getMessage(),
                        ex);
            }
            return null;
            
            
        }
        
        public Lookup getLookup() {
            return tc.getLookup();
        }
        
        public void componentOpened() {
            //Add Properties Window
            WindowManager wm = WindowManager.getDefault();
            TopComponent properties = wm.findTopComponent("properties"); // NOI18N
            if (properties != null && !properties.isOpened()) {
                properties.open();
            }
            tc.registerListeners();
            tc.startBackgroundPinAddingProcess();
            LOG.finest("PageFlowEditor componentOpened");
        }
        
        public void componentClosed() {
            tc.unregstierListeners();
            FileObject storageFile = PageFlowView.getStorageFile(context.getFacesConfigFile());
            
            if ( storageFile != null && storageFile.isValid() ){
                tc.serializeNodeLocations(storageFile);
            } else {
                DialogDescriptor dialog = new DialogDescriptor(
                        NbBundle.getMessage(JSFPageFlowMultiviewDescriptor.class, "MSG_NoFileToSave", storageFile),
                        NbBundle.getMessage(JSFPageFlowMultiviewDescriptor.class, "TLE_NoFileToSave"));
                java.awt.Dialog d = org.openide.DialogDisplayer.getDefault().createDialog(dialog);
                d.setVisible(true);
            }
            LOG.finest("PageFlowEditor componentClosed");
        }
        
        public void componentShowing() {
            LOG.finest("PageFlowEditor componentShowing");
        }
        
        public void componentHidden() {
            LOG.finest("PageFlowEditor componentHidden");
        }
        
        public void componentActivated() {
            //tc.requestFocusInWindow();
            LOG.finest("PageFlowView componentActivated");
            tc.requestActive();
        }
        
        public void componentDeactivated() {
            LOG.finest("PageFlowView Deactivated");
        }
        
        private MultiViewElementCallback callback;
        public MultiViewElementCallback getMultiViewCallback() {
            return callback;
        }
        
        public void setMultiViewCallback(MultiViewElementCallback callback) {
            this.callback = callback;
            context.setMultiViewTopComponent(callback.getTopComponent());
        }
        
        public CloseOperationState canCloseElement() {
            return CloseOperationState.STATE_OK;
        }
        
        private void writeObject(java.io.ObjectOutputStream out) throws IOException {
            tc.serializeNodeLocations(PageFlowView.getStorageFile(context.getFacesConfigFile()));
            out.writeObject(context);
            LOG.finest("writeObject");
        }
        
        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
            Object object = in.readObject();
            if (! (object instanceof JSFConfigEditorContext))
                throw new ClassNotFoundException("JSFConfigEditorContext expected but not found");
            context = (JSFConfigEditorContext) object;
            /* deserialization of node locations is completed in the PageFlowView constructor (in init() ) */
            init();
            LOG.finest("readObject");
        }
        
        public UndoRedo getUndoRedo() {
            return context.getUndoRedo();
        }
        
    }
    
    private final static Logger LOG = Logger.getLogger("org.netbeans.modules.web.jsf.navigation");
    
}
