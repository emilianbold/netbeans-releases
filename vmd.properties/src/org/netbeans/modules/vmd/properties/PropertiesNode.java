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


package org.netbeans.modules.vmd.properties;

import java.lang.ref.WeakReference;
import org.netbeans.modules.vmd.api.io.DataEditorView;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Sheet;

/**
 *
 * @author Karol Harezlak
 */
public class PropertiesNode extends AbstractNode{
    
    private WeakReference<DesignComponent> component;
    private WeakReference<DataEditorView> view;
    private String displayName;
    
    public PropertiesNode(DataEditorView view, DesignComponent component) {
        super(Children.LEAF, view.getContext().getDataObject().getLookup());
        this.component = new WeakReference<DesignComponent>(component);
        this.view = new WeakReference<DataEditorView>(view);
    }
    
    public DesignComponent getComponent() {
        return component.get();
    }
    
    public Sheet createSheet() {
        if(component.get() == null)
            super.createSheet();
        return PropertiesNodesManager.getDefault(view.get()).getSheet(component.get());
    }
    
    public String getDisplayName() {
        return createName();
    }
    
    public String createName() {
        if (component.get() == null)
            return super.getDisplayName();
        component.get().getDocument().getTransactionManager().readAccess(new Runnable() {
            public void run() {
                if (component.get().getParentComponent() == null && component.get().getDocument().getRootComponent() != component.get())
                    return;
                displayName = InfoPresenter.getDisplayName(component.get());
            }
        });
        return displayName;
    }
    
    public void updateNode(Sheet sheet) {
        setSheet(sheet);
        setName(createName());
    }
    
}



