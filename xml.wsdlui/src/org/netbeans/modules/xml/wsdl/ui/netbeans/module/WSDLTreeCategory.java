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

package org.netbeans.modules.xml.wsdl.ui.netbeans.module;

import java.awt.Image;

import javax.swing.Icon;
import javax.swing.ImageIcon;

import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLComponent;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.ui.netbeans.module.WSDLSettings.ViewMode;
import org.netbeans.modules.xml.wsdl.ui.search.AttributeNameSearchProvider;
import org.netbeans.modules.xml.wsdl.ui.search.AttributeValueSearchProvider;
import org.netbeans.modules.xml.wsdl.ui.search.ComponentNameSearchProvider;
import org.netbeans.modules.xml.wsdl.ui.search.ComponentTypeSearchProvider;
import org.netbeans.modules.xml.wsdl.ui.view.treeeditor.TreeEditorView;
import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ui.category.AbstractCategory;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;

/**
 * Displays the tree form of the WSDL editor.
 *
 * @author Nathan Fiedler
 */
public class WSDLTreeCategory extends AbstractCategory {
    /** Associated WSDL model. */
    private WSDLModel wsdlModel;
    /** Our visual component. */
    private TreeEditorView component;
    /** Our lookup. */
    private Lookup lookup;

    /**
     * Creates a new instance of WSDLTreeCategory.
     *
     * @param  model   schema model to display.
     * @param  lookup  associated Lookup instance.
     */
    public WSDLTreeCategory(WSDLModel model, Lookup lookup) {
        wsdlModel = model;
        Object[] searchers = new Object[] {
            new ComponentNameSearchProvider(model, this),
            new ComponentTypeSearchProvider(model, this),
            new AttributeNameSearchProvider(model, this),
            new AttributeValueSearchProvider(model, this),
        };
        this.lookup = new ProxyLookup(new Lookup[] {
            lookup,
            Lookups.fixed(searchers)
        });
    }

    public void componentHidden() {
    }

    public void componentShown() {
        initComponents();
        WSDLSettings.getDefault().setViewMode(ViewMode.TREE);
    }

    public java.awt.Component getComponent() {
        initComponents();
        return component;
    }

    public String getDescription() {
        return NbBundle.getMessage(WSDLTreeCategory.class,
                "HINT_WsdlCategory_Tree");
    }

    public Icon getIcon() {
        String url = NbBundle.getMessage(WSDLTreeCategory.class,
                "IMG_WsdlCategory_Tree");
        Image img = Utilities.loadImage(url);
        return new ImageIcon(img);
    }

    public Lookup getLookup() {
        return lookup;
    }

    public String getTitle() {
        return NbBundle.getMessage(WSDLTreeCategory.class,
                "LBL_WsdlCategory_Tree");
    }

    /**
     * Construct our visual components, if they have not been already.
     */
    private void initComponents() {
        if (component == null) {
            component = new TreeEditorView(wsdlModel);
        }
    }

    public void showComponent(Component comp) {
        if (comp instanceof WSDLComponent) {
            component.showComponent((WSDLComponent) comp);
        } else if (comp instanceof SchemaComponent) {
            component.showComponent((SchemaComponent) comp);
        }
    }
}
