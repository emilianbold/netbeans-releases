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
package org.netbeans.modules.vmd.api.model;

/**
 * This class is designed for a privilege access to component. Each component could have a set of presenters attached to it.
 * The presenters are created by ComponentDescriptor.createPresenters method on behalf of a component. The createPresenters
 * method is called when a descriptor is reassigned to a component or a new component is created. The creation of a presenter
 * basically means nothing and should be as fast as possible.
 * <p>
 * If you want to have a presenter that will react on changes in document, you should use DynamicPresenter.
 *
 * @author David Kaspar
 */
public abstract class Presenter {

    private DesignComponent component;

    /**
     * Creates a new presenter
     */
    protected Presenter () {
    }

    /**
     * Returns a component where the presenter is attached to.
     * @return the component, null if not attached
     */
    protected final DesignComponent getComponent () {
        return component;
    }

    void setNotifyAttached (DesignComponent component) {
        assert Debug.isFriend (ListenerManager.class, "fireEventCore")  ||  Debug.isFriend (DynamicPresenter.class, "setNotifyAttached"); // NOI18N
        assert this.component == null && component != null;
        this.component = component;
    }

    void setNotifyDetached (DesignComponent component) {
        assert Debug.isFriend (ListenerManager.class, "fireEventCore")  ||  Debug.isFriend (DynamicPresenter.class, "setNotifyDetached"); // NOI18N
        assert this.component == component;
        this.component = null;
    }

    /**
     /**
     * Returns string identification of the presenter.
     * @return the string
     */
    public String toString () {
        return (component != null ? component.toString () : "<unassigned>") + "#" + getClass ().getSimpleName (); // NOI18N
    }

}
