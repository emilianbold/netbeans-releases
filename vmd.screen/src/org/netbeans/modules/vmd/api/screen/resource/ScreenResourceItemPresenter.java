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

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.Presenter;

/**
 * Screen resource presenter
 * @author breh
 */
public abstract class ScreenResourceItemPresenter extends Presenter {

    /**
     * Returns category descriptor. This method cannot return null.
     * @return non-null category descriptor
     */
    public abstract ScreenResourceCategoryDescriptor getCategoryDescriptor();
    
    /**
     * Determines whether this resource is active in the given component (i.e. whether it makes sense to visualize it
     * when a given component is being edited
     * @param component the edited component in the device view
     * @return true, if active for the component
     */
    public abstract boolean isActiveFor (DesignComponent component);

    public final DesignComponent getRelatedComponent () {
        return getComponent ();
    }
    
}
