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

import org.netbeans.modules.j2ee.dd.api.ejb.EnterpriseBeans;
import org.netbeans.modules.xml.multiview.SectionNode;
import org.netbeans.modules.xml.multiview.ToolBarMultiViewElement;
import org.netbeans.modules.xml.multiview.ui.SectionView;
import org.netbeans.modules.xml.multiview.ui.SectionNodeView;
import org.netbeans.modules.xml.multiview.ui.ToolBarDesignEditor;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

/**
 * @author pfiala
 */
public abstract class EjbJarMultiviewElement extends ToolBarMultiViewElement {

    private SectionView view;
    protected ToolBarDesignEditor comp;
    protected EjbJarMultiViewDataObject dataObject;
    EnterpriseBeans enterpriseBeans;

    public EjbJarMultiviewElement(EjbJarMultiViewDataObject dataObject) {
        super(dataObject);
        this.dataObject = dataObject;
        comp = new ToolBarDesignEditor();
        setVisualEditor(comp);
    }

    public void componentShowing() {
        if (view == null) {
            view = createView();
            if (view instanceof SectionNodeView) {
                dataObject.getEjbJar().addPropertyChangeListener(new PropertyChangeListener() {
                    public void propertyChange(PropertyChangeEvent evt) {
                        ((SectionNodeView) view).dataModelPropertyChange(evt.getSource(), evt.getPropertyName(),
                                evt.getOldValue(), evt.getNewValue());
                    }
                });
            }
        }
        comp.setContentView(view);
        if (view instanceof SectionNodeView) {
            ((SectionNodeView) view).refreshView();
        }
        view.checkValidity();
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
        dataObject.checkParseable();
    }

    protected abstract SectionView createView();
    
    public SectionView getSectionView() {
        return view;
    }
}
