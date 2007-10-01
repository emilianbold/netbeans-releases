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

package org.netbeans.modules.vmd.api.properties;

import java.util.List;
import org.netbeans.modules.vmd.api.model.DynamicPresenter;

/**
 *
 * @author Karol Harezlak
 */

/**
 * Major function of this class is to provide information about properties of
 * DesignComponent to display them (visualize) in the Properties Window Properties.
 * Based on this informations provided by this presenter Properties module is able to create properties sheet and
 * custom property editors.
 */
public abstract class PropertiesPresenter extends DynamicPresenter {
    
    /**
     * Returns list of DesignPropertyEditors.
     * @return list od DesignPropertyDescriptors
     */
    public abstract List<DesignPropertyDescriptor> getDesignPropertyDescriptors();
    /**
     * Returns list of categories avaiable for properties in this presenter.
     * @return list of categories
     */
    public abstract List<String> getPropertiesCategories();

}
