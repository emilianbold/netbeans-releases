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
package org.netbeans.modules.vmd.api.codegen;

import org.netbeans.modules.vmd.api.model.DesignComponent;

/**
 * TODO - Split to API/SPI
 * @author David Kaspar
 */
public abstract class CodeMultiGuardedLevelPresenter extends CodePresenter {

    /**
     * Note: The method has to leave the section in state: section-is-switched-to-guarded, writer-is-not-committed
     * @param section
     */
    protected abstract void generateMultiGuardedSectionCode (MultiGuardedSection section);

    public static void generateMultiGuardedSectionCode (MultiGuardedSection section, DesignComponent component) {
        if (component == null)
            return;
        for (CodeMultiGuardedLevelPresenter presenter : component.getPresenters (CodeMultiGuardedLevelPresenter.class)) {
            presenter.generateMultiGuardedSectionCode (section);
            assert section.isGuarded ();
        }
    }

}
