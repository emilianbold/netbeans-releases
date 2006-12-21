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
import org.netbeans.modules.vmd.api.model.Debug;

/**
 * TODO - Split to API/SPI
 * @author David Kaspar
 */
public abstract class CodeReferencePresenter extends CodePresenter {

    protected abstract String generateAccessCode ();

    protected abstract String generateDirectAccessCode ();

    protected abstract String generateTypeCode ();

    public static String generateAccessCode (DesignComponent component) {
        if (component == null)
            return "null"; // NOI18N
        CodeReferencePresenter presenter = component.getPresenter (CodeReferencePresenter.class);
        if (presenter == null)
            throw Debug.illegalArgument ("Missing CodeReferencePresenter for component", component); // NOI18N
        return presenter.generateAccessCode ();
    }

    public static String generateDirectAccessCode (DesignComponent component) {
        if (component == null)
            return "null"; // NOI18N
        CodeReferencePresenter presenter = component.getPresenter (CodeReferencePresenter.class);
        if (presenter == null)
            throw Debug.illegalArgument ("Missing CodeReferencePresenter for component", component); // NOI18N
        return presenter.generateDirectAccessCode ();
    }

    public static String generateTypeCode (DesignComponent component) {
        if (component == null)
            throw Debug.illegalArgument ("CodeReferencePresenter.generateTypeCode: null component"); // NOI18N
        CodeReferencePresenter presenter = component.getPresenter (CodeReferencePresenter.class);
        if (presenter == null)
            throw Debug.illegalArgument ("Missing CodeReferencePresenter for component", component); // NOI18N
        return presenter.generateTypeCode ();
    }

}
