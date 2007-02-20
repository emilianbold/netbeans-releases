/*
 * JSFConfigMultiviewDescriptor.java
 *
 * Created on February 7, 2007, 5:22 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.web.jsf;

import java.awt.Image;
import java.io.IOException;
import java.io.Serializable;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.Document;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext;
import org.openide.awt.UndoRedo;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditor;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author petr
 */
public class JSFConfigMultiviewDescriptor implements MultiViewDescription, Serializable{
    static final long serialVersionUID = -6305897237371751564L;
    
    private JSFConfigEditorContext context;
    
    /** Creates a new instance of StrutsConfigMultiviewDescriptor */
    public JSFConfigMultiviewDescriptor(JSFConfigEditorContext context) {
        this.context = context;
    }
    
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }
    
    public String getDisplayName() {
        return "XML";
    }
    
    public Image getIcon() {
        return null;
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    public String preferredID() {
        return "XML";
    }
    
    public MultiViewElement createElement() {
        return new JSFConfigMultiviewElement(context);
    }
    
    
    
    class JSFConfigMultiviewElement implements MultiViewElement, Serializable {
        static final long serialVersionUID = -6305897237371751564L;
        
        private transient TopComponent tc;
        private JSFConfigEditorContext context;
        private transient CloneableEditor editor;
        private transient JComponent toolbar;
        private transient JSFConfigDataObject jsfDataObject;
        
        public JSFConfigMultiviewElement(JSFConfigEditorContext context) {
            this.context = context;
            init();
        }
        
        private void init() {            
            try         {
                org.openide.loaders.DataObject dObject = org.openide.loaders.DataObject.find(context.getFacesConfigFile());

                jsfDataObject = (org.netbeans.modules.web.jsf.JSFConfigDataObject) dObject;
                editor = new org.openide.text.CloneableEditor(jsfDataObject.getEditorSupport());
                tc = new org.netbeans.modules.web.jsf.JSFConfigEditorTopComponent(context,
                                                                                  null,
                                                                                  editor);
            }
            catch (DataObjectNotFoundException ex) {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                                                                 ex.getMessage(),
                                                                 ex);
            }
}
        
        public JComponent getVisualRepresentation() {
            return tc;
        }
        
        public JComponent getToolbarRepresentation() {
            if (toolbar == null) {
                JEditorPane pane = editor.getEditorPane();
                if (pane != null) {
                    Document doc = pane.getDocument();
                    if (doc instanceof NbDocument.CustomToolbar)
                        toolbar = ((NbDocument.CustomToolbar) doc).createToolbar(pane);
                }
                if (toolbar == null)
                    toolbar = new JPanel();
            }
            return toolbar;
        }
        
        
        public Lookup getLookup() {
            return tc.getLookup();
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
        
        public UndoRedo getUndoRedo() {
            return editor.getUndoRedo();
        }
        
        public void setMultiViewCallback(MultiViewElementCallback callback) {
            context.setMultiViewTopComponent(callback.getTopComponent());
        }
        
        public CloseOperationState canCloseElement() {
            return CloseOperationState.STATE_OK;
        }
        
        public javax.swing.Action[] getActions() {
            return editor.getActions();
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
    }
    
}
