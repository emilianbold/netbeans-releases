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
package org.netbeans.modules.visualweb.propertyeditors;

import com.sun.rave.propertyeditors.util.JavaInitializer;
import java.text.MessageFormat;

/**
 * A property editor for <code>int</code> or <code>java.lang.Integer</code>. By default,
 * values that fall outside of the range <code>Integer.MIN_VALUE</code> ...
 * <code>Integer.MAX_VALUE</code> will be rejected.
 *
 * @author gjmurphy
 */
public class IntegerPropertyEditor extends NumberPropertyEditor implements
        com.sun.rave.propertyeditors.IntegerPropertyEditor {

    public static final Integer DEFAULT_MIN_VALUE = new Integer(Integer.MIN_VALUE);

    public static final Integer DEFAULT_MAX_VALUE = new Integer(Integer.MAX_VALUE);

    public IntegerPropertyEditor() {
        super(DEFAULT_MIN_VALUE, DEFAULT_MAX_VALUE);
    }

    protected Number parseString(String str) throws IllegalTextArgumentException {
        try {
            return Integer.valueOf(str);
        } catch (NumberFormatException e) {
            throw new IllegalTextArgumentException(
                    MessageFormat.format(bundle.getString("IntegerPropertyEditor.formatErrorMessage"),
                    new String[]{str}), e);
        }
    }

    public String getJavaInitializationString() {
        Class c = this.getDesignProperty().getPropertyDescriptor().getPropertyType();
        if (c.equals(Integer.class))
            return JavaInitializer.toJavaInitializationString((Integer) this.getValue());
        return this.getValue().toString();
    }
}
