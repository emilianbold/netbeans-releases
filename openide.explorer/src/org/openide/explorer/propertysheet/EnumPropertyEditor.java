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

package org.openide.explorer.propertysheet;

import java.beans.PropertyEditorSupport;

/**
 * Property editor for enumeration types.
 * @author Jesse Glick
 */
final class EnumPropertyEditor extends PropertyEditorSupport {

    private final Class<? extends Enum> c;

    public EnumPropertyEditor(Class<? extends Enum> c) {
        this.c = c;
    }

    public String[] getTags() {
        try {
            Object[] values = (Object[]) c.getMethod("values").invoke(null); // NOI18N
            String[] tags = new String[values.length];
            for (int i = 0; i < values.length; i++) {
                tags[i] = values[i].toString();
            }
            return tags;
        } catch (Exception x) {
            throw new AssertionError(x);
        }
    }

    public String getAsText() {
        Object o = getValue();
        return o != null ? o.toString() : "";
    }

    @SuppressWarnings("unchecked")
    public void setAsText(String text) throws IllegalArgumentException {
        if (text.length() > 0) {
            setValue(Enum.valueOf(c, text));
        } else {
            setValue(null);
        }
    }

    public String getJavaInitializationString() {
        Enum e = (Enum) getValue();
        return e != null ? c.getName().replace('$', '.') + '.' + e.name() : "null"; // NOI18N
    }

}
