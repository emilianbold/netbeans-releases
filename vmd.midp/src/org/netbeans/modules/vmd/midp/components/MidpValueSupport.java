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
package org.netbeans.modules.vmd.midp.components;

import org.netbeans.modules.vmd.api.model.DesignComponent;
import org.netbeans.modules.vmd.api.model.PropertyValue;
import org.netbeans.modules.vmd.api.model.presenters.InfoPresenter;

/**
 * @author David Kaspar
 */
public final class MidpValueSupport {

    public static String getHumanReadableString (PropertyValue value) {
        switch (value.getKind ()) {
            case ENUM:
            case VALUE:
                return value.getPrimitiveValue ().toString ();
            case USERCODE:
                return "<custom-code>";
            case NULL:
                return "<null>";
            case ARRAY:
                return "<array>";
            case REFERENCE:
                return "<component-to-" + value.getComponent ().getComponentID () + ">";
            default:
                throw new IllegalStateException ();
        }
    }
    
    public static String getHumanReadableString (DesignComponent component) {
        InfoPresenter infoPresenter = component.getPresenter(InfoPresenter.class);
        return infoPresenter != null ? infoPresenter.getDisplayName(InfoPresenter.NameType.PRIMARY) : null;
    }
    
}
