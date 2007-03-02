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

import java.text.DateFormat;
import java.text.SimpleDateFormat;

/**
 * An in-line property editor for java.util.DateFormat objects.
 *
 * @author Edwin Goei
 */
public class DateFormatPropertyEditor extends PropertyEditorBase {

    private static String[] formats = { "MM/dd/yyyy", "MM-dd-yyyy",
            "yyyy-MM-dd", "dd/MM/yyyy", "dd-MM-yyyy", "dd-MMM-yyyy",
            "MMM dd, yyyy", "E, MMM dd, yyyy" };

    public String getAsText() {
        DateFormat df = (DateFormat) getValue();
        if (df instanceof SimpleDateFormat) {
            SimpleDateFormat sdf = (SimpleDateFormat) df;
            return sdf.toPattern();
        } else {
            return ""; //NOI18N
        }
    }

    public void setAsText(String text) {
        if (text.trim().length() == 0) {
            setValue(null);
        } else {
            SimpleDateFormat df = new SimpleDateFormat(text);
            setValue(df);
        }
    }

    public String getJavaInitializationString() {
        DateFormat df = (DateFormat) getValue();
        if (df instanceof SimpleDateFormat) {
            SimpleDateFormat sdf = (SimpleDateFormat) df;
            return "new java.text.SimpleDateFormat(\"" + sdf.toPattern()
                    + "\")"; //NOI18N
        } else {
            // TODO What should we do here??
            return "java.util.DateFormat.getDateInstance(java.util.DateFormat.SHORT)"; //NOI18N
        }
    }

    public String[] getTags() {
        return formats;
    }
}
