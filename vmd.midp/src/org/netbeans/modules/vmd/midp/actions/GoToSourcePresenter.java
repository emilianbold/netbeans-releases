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
 *
 */

package org.netbeans.modules.vmd.midp.actions;

import org.netbeans.api.editor.guards.GuardedSection;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.Presenter;

/**
 * @author David Kaspar
 */
public abstract class GoToSourcePresenter extends Presenter {

    protected abstract boolean matches (GuardedSection section);

    public static GoToSourcePresenter createForwarder (final String propertyName) {
        return new GoToSourcePresenter() {
            protected boolean matches (GuardedSection section) {
                DesignComponent forward = getComponent ().readProperty (propertyName).getComponent ();
                if (forward == null)
                    return false;
                GoToSourcePresenter presenter = forward.getPresenter (GoToSourcePresenter.class);
                return presenter != null  &&  presenter.matches (section);
            }
        };
    }

}
