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

import java.awt.Component;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JToolBar;
import javax.swing.border.EmptyBorder;

import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewElement;
import org.netbeans.core.spi.multiview.MultiViewElementCallback;
import org.netbeans.modules.visualweb.api.designer.Designer;
import org.netbeans.modules.visualweb.designer.jsf.JsfForm;
import org.netbeans.modules.visualweb.extension.openide.loaders.SystemFileSystemSupport;
import org.openide.awt.Actions;
import org.openide.awt.UndoRedo;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.actions.Presenter;

/**
 * Implemenation of JSF multiview element.
 *
 * @author Peter Zavadsky
 */
public class JsfMultiViewElement implements MultiViewElement {

    private static final String PATH_TOOLBAR_FOLDER = "Designer/application/x-designer/Toolbars/Default"; // NOI18N
    
//    private final Designer designer;
    private final JsfTopComponent jsfTopComponent;
    
    private JToolBar toolbar;

    
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
        if (toolbar == null) {
            // TODO -- Look at NbEditorToolBar in the editor - it does stuff
            // with the UI to get better Aqua and Linux toolbar
            toolbar = new JToolBar();
            toolbar.setFloatable(false);
            toolbar.setRollover(true);
            toolbar.setBorder(new EmptyBorder(0, 0, 0, 0));

//            ToolbarListener listener = new ToolbarListener();

            toolbar.addSeparator();
//            previewButton =
//                new JButton(new ImageIcon(Utilities.loadImage("org/netbeans/modules/visualweb/designer/preview.png"))); // NOI18N
//            previewButton.addActionListener(listener);
//            previewButton.setToolTipText(NbBundle.getMessage(DesignerTopComp.class, "PreviewAction"));
//            toolbar.add(previewButton);
            // XXX TODO For now adding only BrowserPreviewAction, but later all of them.
//            Component[] comps = ToolBarInstancesProvider.getDefault().getToolbarComponentsForDesignerComponent(this);
            Action[] actions = SystemFileSystemSupport.getActions(PATH_TOOLBAR_FOLDER);
            Lookup context = getLookup();
            for (int i = 0; i < actions.length; i++) {
                Action action = actions[i];
                if (action == null) {
                    toolbar.addSeparator();
                } else {
                    if (action instanceof ContextAwareAction) {
                        Action contextAwareAction = ((ContextAwareAction)action).createContextAwareInstance(context);
                        if (contextAwareAction != null) {
                            action = contextAwareAction;
                        }
                    }
                    if (action instanceof Presenter.Toolbar) {
                        Component tbp = ((Presenter.Toolbar)action).getToolbarPresenter();
                        toolbar.add(tbp);
                    } else {
//                        toolbar.add(new Actions.ToolbarButton((Action)action));
                        JButton toolbarButton = new JButton();
                        Actions.connect(toolbarButton, (Action)action);
                        toolbar.add(toolbarButton);
                    }
                }
            }
        }
            
        return toolbar;
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

}
