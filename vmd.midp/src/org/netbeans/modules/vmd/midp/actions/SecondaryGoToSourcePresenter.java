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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.midp.actions;

import org.netbeans.modules.vmd.api.model.Presenter;
import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.api.editor.guards.GuardedSection;

/**
 * @author David Kaspar
 */
public abstract class SecondaryGoToSourcePresenter extends Presenter {

    protected abstract boolean matches (GuardedSection section);

    public static GoToSourcePresenter createGoToSourceForwarderToSecondaryGoToSourceOfParent () {
        return new GoToSourcePresenter() {
            protected boolean matches (GuardedSection section) {
                DesignComponent forward = getComponent ().getParentComponent ();
                if (forward == null)
                    return false;
                SecondaryGoToSourcePresenter presenter2 = forward.getPresenter (SecondaryGoToSourcePresenter.class);
                if (presenter2 != null  &&  presenter2.matches (section))
                    return true;
                GoToSourcePresenter presenter = forward.getPresenter (GoToSourcePresenter.class);
                return presenter != null  &&  presenter.matches (section);
            }
        };
    }

}
