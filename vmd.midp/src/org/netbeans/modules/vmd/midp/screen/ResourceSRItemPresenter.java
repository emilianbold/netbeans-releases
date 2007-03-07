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

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.screen.resource.ScreenResourceCategoryDescriptor;
import org.netbeans.modules.vmd.api.screen.resource.ScreenResourceItemPresenter;

/**
 * @author breh
 */
public class ResourceSRItemPresenter extends ScreenResourceItemPresenter {

    public ResourceSRItemPresenter () {
    }

    @Override
    public ScreenResourceCategoryDescriptor getCategoryDescriptor() {
        return ScreenResourceCategoryDescriptorSupport.OTHER_DESIGN_RESOURCES; // TODO
    }

    @Override
    public boolean isActiveFor (DesignComponent component) {
        return true;
    }

}
