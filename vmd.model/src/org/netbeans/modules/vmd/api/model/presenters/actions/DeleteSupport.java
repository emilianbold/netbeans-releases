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

    static boolean canDeleteAsUser (DesignDocument document) {
        Collection<DesignComponent> selectedComponents = document.getSelectedComponents ();
        if (canDelete (selectedComponents) != DeletableState.ALLOWED)
            return false;

        Collection<DesignComponent> componentsToDelete = gatherAllComponentsToDelete (document, selectedComponents);
        return canDelete (componentsToDelete) != DeletableState.DISALLOWED;
    }

    static void invokeDirectUserDeletion (DesignDocument document) {
        Collection<DesignComponent> selectedComponents = document.getSelectedComponents ();

        if (canDelete (selectedComponents) != DeletableState.ALLOWED)
            return;

        Collection<DesignComponent> componentsToDelete = gatherAllComponentsToDelete (document, selectedComponents);
        if (canDelete (componentsToDelete) == DeletableState.DISALLOWED)
            return;

        if (! ConfirmDeletionPanel.show (selectedComponents, componentsToDelete))
            return;

        notifyComponentsDeleting (document.getRootComponent (), Collections.unmodifiableCollection (componentsToDelete));

        for (DesignComponent component : componentsToDelete)
            for (DeletePresenter presenter : component.getPresenters (DeletePresenter.class))
                presenter.delete ();

        for (DesignComponent component : componentsToDelete)
            document.deleteComponent (component);
    }

}
