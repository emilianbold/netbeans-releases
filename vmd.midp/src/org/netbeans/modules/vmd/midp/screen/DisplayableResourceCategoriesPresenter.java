/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.midp.screen;

import java.util.ArrayList;
import java.util.Collection;

import org.netbeans.modules.vmd.api.screen.resources.ResourceCategoryDescriptor;
import org.netbeans.modules.vmd.api.screen.resources.ScreenResourceCategoriesPresenter;

/**
 * @author breh
 *
 */
public class DisplayableResourceCategoriesPresenter extends ScreenResourceCategoriesPresenter {

    
    private static final ArrayList<ResourceCategoryDescriptor> CATEGORIES = new ArrayList<ResourceCategoryDescriptor>();
    static {
        CATEGORIES.add(ResourceCategoryDescriptors.ASSIGNED_COMMANDS);
        CATEGORIES.add(ResourceCategoryDescriptors.OTHER_DESIGN_RESOURCES);
    }
    
    public DisplayableResourceCategoriesPresenter() {
    }
    
    
    @Override
    public Collection<ResourceCategoryDescriptor> getCategoryDescriptors() {
        return CATEGORIES;
    }

}
