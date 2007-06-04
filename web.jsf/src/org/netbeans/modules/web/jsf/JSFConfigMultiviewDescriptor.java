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
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditor;
import org.openide.text.NbDocument;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/**
 *
 * @author Petr Pisl
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
    
    private static final Image JSFConfigIcon = org.openide.util.Utilities.loadImage("org/netbeans/modules/web/jsf/resources/JSFConfigIcon.png"); // NOI18N
    public Image getIcon() {
        return JSFConfigIcon;
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
            return editor;
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
            return jsfDataObject.getLookup();
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
