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

package org.netbeans.modules.xml.xam.ui.search;

import org.netbeans.modules.xml.xam.Component;
import org.netbeans.modules.xml.xam.ui.category.Category;
import org.netbeans.modules.xml.xam.ui.category.CategoryPane;
import org.openide.util.Lookup;

/**
 * Concrete implementation of the search control panel.
 *
 * @author  Nathan Fiedler
 */
public class DefaultSearchControlPanel extends SearchControlPanel {
    /** silence compiler warnings */
    private static final long serialVersionUID = 1L;
    /** Template for finding SearchProvider instances. */
    private transient Lookup.Template providerTemplate;
    /** Parent CategoryPane that provides current Category. */
    private transient CategoryPane categoryPane;

    /**
     * Creates a new instance of DefaultSearchControlPanel.
     *
     * @param  categoryPane  associated category pane.
     */
    public DefaultSearchControlPanel(CategoryPane categoryPane) {
        this.categoryPane = categoryPane;
        providerTemplate = new Lookup.Template(SearchProvider.class);
    }

    public void addNotify() {
        super.addNotify();
        Category cat = categoryPane.getCategory();
        Lookup.Result result = cat.getLookup().lookup(providerTemplate);
        setProviders(result.allInstances());
    }

    protected void showSearchResult(Object result) {
        if (result instanceof Component) {
            Component comp = (Component) result;
            categoryPane.getCategory().showComponent(comp);
        }
    }

    /**
     * Use the associated CategoryPane to get the search providers from
     * the currently selected Category. Updates the set of available
     * search providers in the user interface.
     */
    public void updateSearchProviders() {
        Category cat = categoryPane.getCategory();
        Lookup.Result result = cat.getLookup().lookup(providerTemplate);
        setProviders(result.allInstances());
    }
}
