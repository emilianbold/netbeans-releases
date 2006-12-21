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
package org.netbeans.modules.vmd.api.model.presenters.actions;

import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;

import java.util.Collection;

/**
 * @author David Kaspar
 */
public abstract class DeleteDependencyPresenter extends Presenter {

    /**
     * Returns whether the related component requires any of specified components to live.
     * @param componentsToDelete the collection of components
     * @return true, if the related component requires any of specified components to live
     */
    protected abstract boolean requiresToLive (Collection<DesignComponent> componentsToDelete);

    /**
     * Notifies about deleted components. Components are deleted
     * @param componentsToDelete
     */
    protected abstract void componentsDeleting (Collection<DesignComponent> componentsToDelete);

    public static Presenter createNullableComponentReferencePresenter (final String propertyName) {
        return new DeleteDependencyPresenter() {

            protected boolean requiresToLive (Collection<DesignComponent> componentsToDelete) {
                return false;
            }

            protected void componentsDeleting (Collection<DesignComponent> componentsToDelete) {
                DesignComponent component = getComponent ().readProperty (propertyName).getComponent ();
                if (component != null  &&  componentsToDelete.contains (component))
                    getComponent ().writeProperty (propertyName, PropertyValue.createNull ());
            }

        };
    }

    public static Presenter createDependentOnParentComponentPresenter () {
        return new DeleteDependencyPresenter() {
            protected boolean requiresToLive (Collection<DesignComponent> componentsToDelete) {
                DesignComponent parentComponent = getComponent ().getParentComponent ();
                return parentComponent != null  &&  componentsToDelete.contains (parentComponent);
            }

            protected void componentsDeleting (Collection<DesignComponent> componentsToDelete) {
            }
        };
    }

    public static Presenter createDependentOnPropertyPresenter (final String propertyName) {
        return new DeleteDependencyPresenter() {
            protected boolean requiresToLive (Collection<DesignComponent> componentsToDelete) {
                DesignComponent component = getComponent ().readProperty (propertyName).getComponent ();
                return component != null && componentsToDelete.contains (component);
            }

            protected void componentsDeleting (Collection<DesignComponent> componentsToDelete) {
            }
        };
    }

}
