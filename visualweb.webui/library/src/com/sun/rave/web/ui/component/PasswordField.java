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
package com.sun.rave.web.ui.component;
import javax.faces.context.FacesContext;
import com.sun.rave.web.ui.util.ConversionUtilities;

public class PasswordField extends PasswordFieldBase {
   /**
     * <p>Return the value to be rendered as a string when the
     * component is readOnly. The value will be
    * represented using asterisks.</p>
     * @param context FacesContext for the current request
     * @return A String value of the component
     */
    public String getReadOnlyValueString(FacesContext context) {

        String value = ConversionUtilities.convertValueToString(this, getValue());
        if(value == null) {
            return new String();
        }
        char[] chars = value.toCharArray();
        for(int i=0; i< chars.length; ++i) {
            chars[i] = '*';
        }
        return new String(chars);
    }
}
