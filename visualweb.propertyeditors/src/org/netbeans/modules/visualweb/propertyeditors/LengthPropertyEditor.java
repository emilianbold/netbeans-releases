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

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * A property editor for strings that represent length measurements, as
 * defined by HTML 4.01. The value may be either a non-negative integer,
 * which represents an absolute length measured in pixels; or a percentage,
 * which represents a percentage of the available screen dimension. The
 * editor will also normalize the submitted string before it is set, e.g.
 * "<code>000123</code>" will be submitted as "<code>123</code>", and
 * "<code> 50 % </code>" will be submitted as "<code>50%</code>".
 *
 * @author gjmurphy
 */
public class LengthPropertyEditor extends PropertyEditorBase implements
        com.sun.rave.propertyeditors.LengthPropertyEditor {

    static ResourceBundle bundle =
            ResourceBundle.getBundle(LengthPropertyEditor.class.getPackage().getName() + ".Bundle"); //NOI18N

    public String getAsText() {
        if (this.getValue() == null && super.unsetValue == null)
            return "";
        return super.getAsText();
    }

    public void setAsText(String text) throws IllegalArgumentException {
        text = text == null ? "" : text.trim();
        if (text.length() > 0) {
            try {
                int value;
                if (text.endsWith("%")) {
                    value = Integer.parseInt(text.substring(0, text.length()-1).trim());
                    if (value < 0 || value > 100)
                        throw new IllegalArgumentException();
                    text = Integer.toString(value) + "%";
                } else {
                    value = Integer.parseInt(text);
                    text = Integer.toString(value);
                }
            } catch( NumberFormatException e ) {
                throw new IllegalTextArgumentException(
                        MessageFormat.format(bundle.getString("LengthPropertyEditor.formatErrorMessage"),
                        new String[]{text}), e);
            }
            super.setValue(text);
        } else {
            super.setValue(null);
        }
    }

}
