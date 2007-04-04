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

package org.netbeans.modules.visualweb.designer.jsf.ui;

import javax.swing.Action;
import javax.swing.JComponent;

import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.visualweb.api.designer.Designer;
import org.netbeans.modules.visualweb.designer.jsf.JsfForm;
import org.openide.awt.UndoRedo;
import org.openide.util.Lookup;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Implemenation of JSF multiview element.
 *
 * @author Peter Zavadsky
 */
public class JsfMultiViewElement implements MultiViewElement {

    private static final String PATH_TOOLBAR_FOLDER = "Designer/application/x-designer/Toolbars/Default"; // NOI18N
    
//    private final Designer designer;
    private final JsfTopComponent jsfTopComponent;

    
    /** Creates a new instance of DesignerMultiViewElement */
    public JsfMultiViewElement(JsfForm jsfForm, Designer designer) {
//        if (designer == null) {
//            throw new NullPointerException("The designer parameter is null!"); // NOI18N
//        }
//        this.designer = designer;
        jsfTopComponent = new JsfTopComponent(jsfForm, designer);
    }

    
    public JsfTopComponent getJsfTopComponent() {
        return jsfTopComponent;
    }
    
    public JComponent getVisualRepresentation() {
//        return designer.getVisualRepresentation();
        return jsfTopComponent.getVisualRepresentation();
    }

    // XXX Moved from designer/../DesignerTopComp.
    // TODO Move it to JsfTopComponent.
    public JComponent getToolbarRepresentation() {
//        return designer.getToolbarRepresentation();
        return jsfTopComponent.getToolbarRepresentation();
    }

    public Action[] getActions() {
//        return designer.getActions();
        return jsfTopComponent.getActions();
    }

    public Lookup getLookup() {
//        return designer.getLookup();
        return jsfTopComponent.getLookup();
    }

    public void componentOpened() {
//        designer.componentOpened();
        jsfTopComponent.componentOpened();
    }

    public void componentClosed() {
//        designer.componentClosed();
        jsfTopComponent.componentClosed();
    }

    public void componentShowing() {
//        designer.componentShowing();
        jsfTopComponent.componentShowing();
    }

    public void componentHidden() {
//        designer.componentHidden();
        jsfTopComponent.componentHidden();
    }

    public void componentActivated() {
//        designer.componentActivated();
        jsfTopComponent.componentActivated();
    }

    public void componentDeactivated() {
//        designer.componentDeactivated();
        jsfTopComponent.componentDeactivated();
    }

    public UndoRedo getUndoRedo() {
//        return designer.getUndoRedo();
        return jsfTopComponent.getUndoRedo();
    }

    public void setMultiViewCallback(MultiViewElementCallback multiViewElementCallback) {
//        designer.setMultiViewCallback(multiViewElementCallback);
        jsfTopComponent.setMultiViewCallback(multiViewElementCallback);
    }

    public CloseOperationState canCloseElement() {
//        return designer.canCloseElement();
        return jsfTopComponent.canCloseElement();
    }

    
    // JSF notifications >>>
    public void modelChanged() {
        jsfTopComponent.modelChanged();
    }
    
    public void modelRefreshed() {
        jsfTopComponent.modelRefreshed();
    }
    
    public void nodeChanged(Node node, Node parent, boolean wasMove) {
        jsfTopComponent.nodeChanged(node, parent, wasMove);
    }
    
    public void nodeRemoved(Node node, Node parent) {
        jsfTopComponent.nodeRemoved(node, parent);
    }
    
    public void nodeInserted(Node node, Node parent) {
        jsfTopComponent.nodeInserted(node, parent);
    }
    
    public void updateErrors() {
        jsfTopComponent.updateErrors();
    }
    
    public void gridModeUpdated(boolean gridMode) {
        jsfTopComponent.gridModeUpdated(gridMode);
    }
    
    public void documentReplaced() {
        jsfTopComponent.documentReplaced();
    }
    
    public void showDropMatch(Element componentRootElement, Element regionElement, int dropType) {
        jsfTopComponent.showDropMatch(componentRootElement, regionElement, dropType);
    }
    
    public void clearDropMatch() {
        jsfTopComponent.clearDropMatch();
    }
    // JSF notifications <<<
            
}
