/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.ddloaders.multiview;

import org.netbeans.core.spi.multiview.CloseOperationState;
import org.netbeans.core.spi.multiview.MultiViewFactory;
import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.xml.multiview.Error;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.ui.PanelView;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;

/**
 * @author pfiala
 */
public class EjbMultiViewElement extends ToolBarMultiViewElement {
    private PanelView view;
    private ToolBarDesignEditor comp;
    private EjbJarMultiViewDataObject dataObject;
    EnterpriseBeans enterpriseBeans;

    /**
     * Creates a new instance of DDMultiViewElement
     */
    public EjbMultiViewElement(EjbJarMultiViewDataObject dataObject) {
        super();
        this.dataObject = dataObject;
        comp = new ToolBarDesignEditor();
        setVisualEditor(comp);
    }

    public void componentShowing() {
        view = new EjbJarView(dataObject);
        comp.setContentView(view);
        Error error = view.validateView();
        if (error != null) {
            comp.getErrorPanel().setError(error);
        } else {
            comp.getErrorPanel().clearError();
        }
        super.componentShowing();
        Object lastActive = comp.getLastActive();
        final SectionNode node;
        final SectionNodeView sectionNodeView = ((SectionNodeView) view);
        if (lastActive instanceof SectionNode) {
            node = (SectionNode) lastActive;
        } else {
            node = sectionNodeView.getRootNode();
        }
        sectionNodeView.openPanel(node);
    }

    public CloseOperationState canCloseElement() {
        if (dataObject.isModified()) {
            return MultiViewFactory.createUnsafeCloseState("Data object modified", null, null);
        } else {
            return CloseOperationState.STATE_OK;
        }
    }
}
