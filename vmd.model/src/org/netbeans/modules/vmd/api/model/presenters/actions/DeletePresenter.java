/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.vmd.api.model.presenters.actions;

import java.util.List;
import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.PropertyDescriptor;
import org.netbeans.modules.vmd.api.model.PropertyValue;

/**
 * This presenters specifies delete ability.
 *
 * @author David Kaspar
 */
public abstract class DeletePresenter extends Presenter {

    /**
     * Returns whether the related component could be deleted.
     * @return the deletable state
     */
    DeletableState canDelete() {
        return DeletableState.ALLOWED;
    }

    /**
     * Returns whether the related component could be listed in the delete dialog.
     * @return true, if could be listed; false, if it should be deleted silently
     */
    boolean isSilent() {
        return false;
    }

    /**
     * Perform the deletion logic for the relation component.
     * You should also invoke deletion of related components that cannot live without this component.
     * For deletion invocation, use <code>DeletePresenter.invokeDeletion</code> method.
     */
    protected abstract void delete();

    /**
     * Creates a DeletePresentet that disallows to list the component in the confirm-deletion dialog.
     * @return the delete presenter
     */
    public static Presenter createSilentDeletionPresenter() {
        return new DeletePresenter() {

            @Override
            boolean isSilent() {
                return true;
            }

            protected void delete() {
            }
        };
    }

    /**
     * Creates a DeletePresenter that disallows to delete related component.
     * @return the delete presenter
     */
    public static Presenter createIndeliblePresenter() {
        return new DeletePresenter() {

            @Override
            DeletableState canDelete() {
                return DeletableState.DISALLOWED;
            }

            protected void delete() {
                throw Debug.illegalState();
            }
        };
    }

    /**
     * Creates a DeletePresenter that disallows to delete related component by user only.
     * The component could be deleted by indirect deletion still.
     * @return the delete presenter
     */
    public static Presenter createUserIndeliblePresenter() {
        return new DeletePresenter() {

            @Override
            DeletableState canDelete() {
                return DeletableState.DISALLOWED_FOR_USER_ONLY;
            }

            protected void delete() {
            }
        };
    }

    public static Presenter createRemoveComponentReferences() {
        return new DeletePresenter() {

            @Override
            DeletableState canDelete() {
                return DeletableState.ALLOWED;
            }

            protected void delete() {
                DesignComponent root = getComponent().getDocument().getRootComponent();
                search(root);

            }

            private void search(DesignComponent root) {
                for (DesignComponent component : root.getComponents()) {
                    if (component.getComponents() != null) {
                        search(component);
                    }
                    removeReferences(component);
                }
            }

            private void removeReferences(DesignComponent component) {
                List<PropertyDescriptor> descriptors = component.getComponentDescriptor().getDeclaredPropertyDescriptors();
                if (descriptors == null) {
                    return;
                }
                for (PropertyDescriptor descriptor : descriptors) {
                    PropertyValue value = component.readProperty(descriptor.getName());
                    if (value.getComponent() != null && value.getComponent() == getComponent()) {
                        component.writeProperty(descriptor.getName(), PropertyValue.createNull());
                    }
                }
            }
        };
    }

//    /**
//     * Creates a presenter which invokes deletion of referenced components by a specified property name.
//     * @param propertyName the property name of property of the related component
//     * @return the delete presenter
//     */
//    public static Presenter createReferencedComponentsPresenter (final String propertyName) {
//        return new DeletePresenter() {
//            protected DeletableState canDelete () {
//                ArrayList<DesignComponent> children = new ArrayList<DesignComponent> ();
//                Debug.collectAllComponentReferences (getComponent ().readProperty (propertyName), children);
//                return DeletePresenter.canDelete (children);
//            }
//
//            protected void delete () {
//                ArrayList<DesignComponent> children = new ArrayList<DesignComponent> ();
//                Debug.collectAllComponentReferences (getComponent ().readProperty (propertyName), children);
//                DeletePresenter.delete (children);
//            }
//        };
//    }
}
