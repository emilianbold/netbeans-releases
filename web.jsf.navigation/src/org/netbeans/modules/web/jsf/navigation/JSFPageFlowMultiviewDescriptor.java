/*
 * JSFTestMultiviewDescriptor.java
 *
 * Created on February 7, 2007, 6:25 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf.navigation;

import java.awt.BorderLayout;
import java.awt.Image;
import java.io.IOException;
import java.io.Serializable;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext;
import org.openide.awt.UndoRedo;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Joelle Lam
 */
public class JSFPageFlowMultiviewDescriptor implements MultiViewDescription, Serializable{
    
    private static final long serialVersionUID = -6305897237371751567L;
    
    private JSFConfigEditorContext context;
    
    
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
        return "PageFlow";
    }
    
    public Image getIcon() {
        return null;
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    public String preferredID() {
        return "pageflow";
    }
    
    public MultiViewElement createElement() {
        return new PageFlowElement(context);
    }
   
    
    static class PageFlowElement implements MultiViewElement, Serializable {
        private transient JScrollPane panel;
        private transient TopComponent tc;
        private static final long serialVersionUID = -6305897237371751567L;
        private JSFConfigEditorContext context;
        
        
        public PageFlowElement() {
        }
        
        public PageFlowElement(JSFConfigEditorContext context) {
            this.context = context;
            init();
        }
        
        private void init() {
            tc = new PageFlowView(context);
            panel = new JScrollPane(tc);
            panel.setName(context.getFacesConfigFile().getName());
//            this.setName(context.getFacesConfigFile().getName());
//            add(panel, BorderLayout.CENTER);
        }
        
        public JComponent getVisualRepresentation() {
            return panel;
        }
        
        public JComponent getToolbarRepresentation() {
            return new JLabel("");
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
            //return tc.getLookup();
            try {
                DataObject dataObject = org.openide.loaders.DataObject.find(context.getFacesConfigFile());
                
                return dataObject.getLookup();
            } catch (DataObjectNotFoundException ex) {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                        ex.getMessage(),
                        ex);
            }
            return null;
        }
        
        public void componentOpened() {
            
        }
        
        public void componentClosed() {
            
        }
        
        public void componentShowing() {
            
        }
        
        public void componentHidden() {
            
        }
        
        public void componentActivated() {
            
        }
        
        public void componentDeactivated() {
            
        }
        
        
        public void setMultiViewCallback(MultiViewElementCallback callback) {
            context.setMultiViewTopComponent(callback.getTopComponent());
        }
        
        public CloseOperationState canCloseElement() {
            return CloseOperationState.STATE_OK;
        }
        
        private void writeObject(java.io.ObjectOutputStream out) throws IOException {
            out.writeObject(context);
        }
        
        private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
            Object object = in.readObject();
            if (! (object instanceof JSFConfigEditorContext))
                throw new ClassNotFoundException("JSFConfigEditorContext expected but not found");
            context = (JSFConfigEditorContext) object;
            init();
        }
        
        public UndoRedo getUndoRedo() {
            return context.getUndoRedo();
        }
        
    }
}
