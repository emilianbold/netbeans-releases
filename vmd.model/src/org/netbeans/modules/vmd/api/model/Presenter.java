/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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

    private DesignComponent contentComponent;

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
        return contentComponent;
    }

    void setNotifyAttached (DesignComponent component) {
        assert Debug.isFriend (ListenerManager.class, "addComponentDescriptorChanged")  ||  Debug.isFriend (DynamicPresenter.class, "setNotifyAttached"); // NOI18N
        assert this.contentComponent == null && component != null;
        this.contentComponent = component;
    }

    void setNotifyDetached (DesignComponent component) {
        assert Debug.isFriend (ListenerManager.class, "fireEventCore")  ||  Debug.isFriend (DynamicPresenter.class, "setNotifyDetached"); // NOI18N
        assert this.contentComponent == component;
        this.contentComponent = null;
    }

    /**
     /**
     * Returns string identification of the presenter.
     * @return the string
     */
    @Override
    public String toString () {
        return (contentComponent != null ? contentComponent.toString () : "<unassigned>") + "#" + getClass ().getSimpleName (); // NOI18N
    }

}
