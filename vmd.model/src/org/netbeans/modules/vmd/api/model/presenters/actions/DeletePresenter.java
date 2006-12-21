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

import org.netbeans.modules.vmd.api.model.Debug;
import org.netbeans.modules.vmd.api.model.Presenter;

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
    DeletableState canDelete () {
        return DeletableState.ALLOWED;
    }

    /**
     * Returns whether the related component could be listed in the delete dialog.
     * @return true, if could be listed; false, if it should be deleted silently
     */
    boolean isSilent () {
        return false;
    }

    /**
     * Perform the deletion logic for the relation component.
     * You should also invoke deletion of related components that cannot live without this component.
     * For deletion invocation, use <code>DeletePresenter.invokeDeletion</code> method.
     */
    protected abstract void delete ();

    /**
     * Creates a DeletePresentet that disallows to list the component in the confirm-deletion dialog.
     * @return the delete presenter
     */
    public static Presenter createSilentDeletionPresenter () {
        return new DeletePresenter () {
            boolean isSilent () {
                return true;
            }
            protected void delete () {
            }
        };
    }

    /**
     * Creates a DeletePresenter that disallows to delete related component.
     * @return the delete presenter
     */
    public static Presenter createIndeliblePresenter () {
        return new DeletePresenter () {

            DeletableState canDelete () {
                return DeletableState.DISALLOWED;
            }

            protected void delete () {
                throw Debug.illegalState ();
            }

        };
    }

    /**
     * Creates a DeletePresenter that disallows to delete related component by user only.
     * The component could be deleted by indirect deletion still.
     * @return the delete presenter
     */
    public static Presenter createUserIndeliblePresenter () {
        return new DeletePresenter() {

            DeletableState canDelete () {
                return DeletableState.DISALLOWED_FOR_USER_ONLY;
            }

            protected void delete () {
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
