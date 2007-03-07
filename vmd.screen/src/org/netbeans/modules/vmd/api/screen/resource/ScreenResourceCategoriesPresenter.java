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
package org.netbeans.modules.vmd.api.screen.resource;

import org.netbeans.modules.vmd.api.model.Presenter;

import java.util.Collection;

/**
 * Descriptor for the categories for the resource used by the screen
 * designer
 * 
 * @author breh
 */
public abstract class ScreenResourceCategoriesPresenter extends Presenter {

    /**
     * Returns category descriptor. This method cannot return null.
     * @return category descriptor
     */
    public abstract Collection<ScreenResourceCategoryDescriptor> getCategoryDescriptors();
      
}
