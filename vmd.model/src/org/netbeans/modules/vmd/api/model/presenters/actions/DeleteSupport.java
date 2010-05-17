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
package org.netbeans.modules.vmd.api.model.presenters.actions;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.DesignDocument;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

/**
 * Support class for component deletion.
 * <p>
 * DeleteSupport.canDeleteAsUser:
 * <ul>
 * <li>DeletePresenter.canDelete == ALLOWED on selected components
 * <li>Gather all components (within the tree) with DeleteDependencyPresenter.requiresToLive (selectedComponent) == true or is as a selected component
 * <li>DeletePresenter.canDelete != DISALLOWED on all gathered and selected components
 * </ul>
 * <p>
 * DeleteSupport.deleteAsUser:
 * <ul>
 * <li>DeletePresenter.canDelete == ALLOWED on selected components
 * <li>Gather all components (within the tree) with DeleteDependencyPresenter.requiresToLive (selectedComponent) == true or is as a selected component
 * <li>DeletePresenter.canDelete != DISALLOWED on all gathered and selected components
 * <li>DeleteDependencyPresenter.componentsDeleting on all components in tree
 * <li>DeletePresenter.delete on all gathered and selected components
 * <li>DesignDocument.removeComponent on all gathered and selected components
 * </ul>
 *
 * @author David Kaspar
 */
public final class DeleteSupport {

    private DeleteSupport () {
    }

    private static DeletableState canDelete (Collection<DesignComponent> components) {
        boolean disallowedForUserOnly = false;
        for (DesignComponent component : components) {
            for (DeletePresenter presenter : component.getPresenters (DeletePresenter.class)) {
                DeletableState state = presenter.canDelete ();
                switch (state) {
                    case DISALLOWED:
                        return DeletableState.DISALLOWED;
                    case DISALLOWED_FOR_USER_ONLY:
                        disallowedForUserOnly = true;
                }
            }
        }

        return disallowedForUserOnly ? DeletableState.DISALLOWED_FOR_USER_ONLY : DeletableState.ALLOWED;
    }

    static boolean isSilent (DesignComponent component) {
        for (DeletePresenter presenter : component.getPresenters (DeletePresenter.class))
            if (presenter.isSilent ())
                return true;
        return false;
    }

    private static Collection<DesignComponent> gatherAllComponentsToDelete (DesignDocument document, Collection<DesignComponent> baseComponents) {
        HashSet<DesignComponent> componentsToDelete = new HashSet<DesignComponent> (baseComponents);
        Collection<DesignComponent> componentsToDeleteUm = Collections.unmodifiableCollection (componentsToDelete);

        while (findNewlyRequired (document.getRootComponent (), componentsToDelete, componentsToDeleteUm))
            ;

        return componentsToDelete;
    }

    private static boolean findNewlyRequired (DesignComponent component, Collection<DesignComponent> componentsToDelete, Collection<DesignComponent> componentsToDeleteUm) {
        boolean changed = false;
        if (! componentsToDelete.contains (component))
            if (requiresToLive (component, componentsToDeleteUm)) {
                componentsToDelete.add (component);
                changed = true;
            }

        for (DesignComponent child : component.getComponents ())
            if (findNewlyRequired (child, componentsToDelete, componentsToDeleteUm))
                changed = true;

        return changed;
    }

    private static boolean requiresToLive (DesignComponent component, Collection<DesignComponent> componentsToDelete) {
        for (DeleteDependencyPresenter presenter : component.getPresenters (DeleteDependencyPresenter.class)) {
            if (presenter.requiresToLive (componentsToDelete))
                return true;
        }
        return false;
    }

    private static void notifyComponentsDeleting (DesignComponent component, Collection<DesignComponent> componentsToDeleteUm) {
        for (DeleteDependencyPresenter presenter : component.getPresenters (DeleteDependencyPresenter.class))
            presenter.componentsDeleting (componentsToDeleteUm);
        for (DesignComponent child : component.getComponents ())
            notifyComponentsDeleting (child, componentsToDeleteUm);
    }

    public static boolean canDeleteAsUser (DesignDocument document, Collection<DesignComponent> componentsToDelete) {
        if (canDelete (componentsToDelete) != DeletableState.ALLOWED)
            return false;

        Collection<DesignComponent> allComponentsToDelete = gatherAllComponentsToDelete (document, componentsToDelete);
        return canDelete (allComponentsToDelete) != DeletableState.DISALLOWED;
    }

    public static void invokeDirectUserDeletion (DesignDocument document, Collection<DesignComponent> componentsToDelete, boolean showConfirmation) {
        if (canDelete (componentsToDelete) != DeletableState.ALLOWED)
            return;

        Collection<DesignComponent> allComponentsToDelete = gatherAllComponentsToDelete (document, componentsToDelete);
        if (canDelete (allComponentsToDelete) == DeletableState.DISALLOWED)
            return;

        if (showConfirmation  &&  ! ConfirmDeletionPanel.show (componentsToDelete, allComponentsToDelete))
            return;

        notifyComponentsDeleting (document.getRootComponent (), Collections.unmodifiableCollection (allComponentsToDelete));

        for (DesignComponent component : allComponentsToDelete)
            for (DeletePresenter presenter : component.getPresenters (DeletePresenter.class))
                presenter.delete ();

        document.deleteComponents (allComponentsToDelete);
    }

}
