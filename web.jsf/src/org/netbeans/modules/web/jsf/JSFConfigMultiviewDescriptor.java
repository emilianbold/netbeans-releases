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
import java.io.Serializable;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.text.Document;
import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewDescription;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.web.jsf.api.editor.JSFConfigEditorContext;
import org.openide.awt.UndoRedo;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.CloneableEditor;
import org.openide.text.DataEditorSupport;
import org.openide.text.NbDocument;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.windows.TopComponent;

/*
 * @author Petr Pisl
 */

public class JSFConfigMultiviewDescriptor implements MultiViewDescription, Serializable{
    static final long serialVersionUID = -6305897237371751564L;
    private final static String XML_CONSTANT = "XML"; //NOI18N
    
    private JSFConfigEditorContext context;
    
    /** Creates a new instance of StrutsConfigMultiviewDescriptor */
    public JSFConfigMultiviewDescriptor(JSFConfigEditorContext context) {
        this.context = context;
    }
    
    public int getPersistenceType() {
        return TopComponent.PERSISTENCE_ONLY_OPENED;
    }
    
    public String getDisplayName() {
        return XML_CONSTANT;
    }
    
    private static final Image JSFConfigIcon = org.openide.util.Utilities.loadImage("org/netbeans/modules/web/jsf/resources/JSFConfigIcon.png"); // NOI18N
    public Image getIcon() {
        return JSFConfigIcon;
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    public String preferredID() {
        return XML_CONSTANT;
    }
    
    public MultiViewElement createElement() {
        MultiViewElement element = null;
        try {
            DataObject dObject = DataObject.find(context.getFacesConfigFile());
            JSFConfigDataObject jsfDataObject = (JSFConfigDataObject) dObject;
            element =  new JSFConfigMultiviewElement(context, jsfDataObject.getEditorSupport());
        } catch (DataObjectNotFoundException ex) {
            Exceptions.printStackTrace(ex);
        }
        return element;
    }
    
    
    
    class JSFConfigMultiviewElement extends CloneableEditor implements MultiViewElement, Serializable {
        static final long serialVersionUID = -6305897237371751564L;
        
        private JSFConfigEditorContext context;
        private transient JComponent toolbar;
        private transient JSFConfigDataObject jsfDataObject;
        
        public JSFConfigMultiviewElement(JSFConfigEditorContext context, DataEditorSupport support) {
            super(support);
            this.context = context;
            init();
        }
        
        private void init() {            
            try {
                DataObject dObject = DataObject.find(context.getFacesConfigFile());

                jsfDataObject = (org.netbeans.modules.web.jsf.JSFConfigDataObject) dObject;
            }
            catch (DataObjectNotFoundException ex) {
                java.util.logging.Logger.getLogger("global").log(java.util.logging.Level.SEVERE,
                                                                 ex.getMessage(),
                                                                 ex);
            }
        }
        
        public JComponent getVisualRepresentation() {
            return this;
        }
        
        public JComponent getToolbarRepresentation() {
            if (toolbar == null) {
                JEditorPane editorPane = getEditorPane();
                if (editorPane != null) {
                    Document doc = editorPane.getDocument();
                    if (doc instanceof NbDocument.CustomToolbar)
                        toolbar = ((NbDocument.CustomToolbar) doc).createToolbar(editorPane);
                }
                if (toolbar == null)
                    toolbar = new JPanel();
            }
            return toolbar;
        }
        
        
        @Override
        public Lookup getLookup() {
            return jsfDataObject.getLookup();
        }
        
        @Override
        public void componentOpened() {
            super.componentOpened();
        }
        
        @Override
        public void componentClosed() {
            super.componentClosed();
        }
        
        @Override
        public void componentShowing() {
            super.componentShowing();
        }
        
        @Override
        public void componentHidden() {
             super.componentHidden();
        }
        
        @Override
        public void componentActivated() {
            super.componentActivated();
        }
        
        @Override
        public void componentDeactivated() {
            super.componentDeactivated();
        }
        
        @Override
        public UndoRedo getUndoRedo() {
            return super.getUndoRedo();
        }
        
        public void setMultiViewCallback(MultiViewElementCallback callback) {
            context.setMultiViewTopComponent(callback.getTopComponent());
        }
        
        public CloseOperationState canCloseElement() {
            // the savin operation is handled by CloseHander form JSFCOnfigEditorSuport
            return MultiViewFactory.createUnsafeCloseState("ID_FACES_CONFIG_CLOSING", MultiViewFactory.NOOP_CLOSE_ACTION, MultiViewFactory.NOOP_CLOSE_ACTION);
        }
        
        @Override
        public javax.swing.Action[] getActions() {
            return super.getActions();
        }
    }
}
