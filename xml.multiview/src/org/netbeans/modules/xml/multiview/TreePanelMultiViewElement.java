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

package org.netbeans.modules.xml.multiview;

import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.xml.multiview.ui.TreePanelDesignEditor;
import org.openide.loaders.DataObject;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ProxyLookup;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;

/**
 * XmlMultiviewElement.java
 *
 * Created on October 5, 2004, 1:35 PM
 * @author  mkuchtiak
 */
public abstract class TreePanelMultiViewElement extends AbstractMultiViewElement {
    private TreePanelDesignEditor editor;

    private PropertyChangeListener listener = new PropertyChangeListener() {
        public void propertyChange(PropertyChangeEvent evt) {
            if (DataObject.PROP_MODIFIED.equals(evt.getPropertyName()) && editor != null) {
                Utils.runInAwtDispatchThread(new Runnable() {
                    public void run() {
                        callback.getTopComponent().setDisplayName(dObj.getEditorSupport().messageName());
                    }
                });
            }
        }
    };

    public TreePanelMultiViewElement(final XmlMultiViewDataObject dObj) {
        super(dObj);
        dObj.addPropertyChangeListener(WeakListeners.propertyChange(listener, dObj));
    }

    protected void setVisualEditor(TreePanelDesignEditor editor) {
        this.editor=editor;
    }
    
    public CloseOperationState canCloseElement() {
        try {
            editor.fireVetoableChange(TreePanelDesignEditor.PROPERTY_FLUSH_DATA, this, null);
        } catch (PropertyVetoException e) {
            return MultiViewFactory.createUnsafeCloseState(TreePanelDesignEditor.PROPERTY_FLUSH_DATA, null, null);
        }
        return super.canCloseElement();
    }
    
    public void componentActivated() {
       editor.componentActivated();
    }
    
    public void componentClosed() {
        editor.componentClosed();
    }
    
    public void componentDeactivated() {
        editor.componentDeactivated();
    }
    
    public void componentHidden() {
        editor.componentHidden();
        dObj.setActiveMultiViewElement(null);
    }
    
    public void componentOpened() {
        editor.componentOpened();
    }
    
    public void componentShowing() {
        editor.componentShowing();
        dObj.setActiveMultiViewElement(this);
    }
    
    public org.openide.util.Lookup getLookup() {
        return new ProxyLookup(new org.openide.util.Lookup[] {
            dObj.getNodeDelegate().getLookup()
        });
    }
    
    public javax.swing.JComponent getToolbarRepresentation() {
        //return editor.getStructureView();
        return new javax.swing.JPanel();
    }
    
    public javax.swing.JComponent getVisualRepresentation() {
        return editor;
    }

}
