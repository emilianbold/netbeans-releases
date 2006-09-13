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

package org.netbeans.beaninfo.editors;

import java.beans.PropertyEditorSupport;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;

/**
 * Basic property editor for dates.
 * Could be expanded; see tasklist/usertasks/src/org/netbeans/modules/tasklist/usertasks/DateEditor.java.
 * @author Jesse Glick
 */
public class DateEditor extends PropertyEditorSupport {

    private static DateFormat fmt = DateFormat.getDateTimeInstance();

    public String getAsText() {
        Date d = (Date)getValue();
        if (d != null) {
            return fmt.format(d);
        } else {
            return ""; // NOI18N
        }
    }
    
    public void setAsText(String text) throws IllegalArgumentException {
        if ("".equals(text)) { // NOI18N
            setValue(null);
        } else {
            try {
                setValue(fmt.parse(text));
            } catch (ParseException e) {
                throw (IllegalArgumentException)new IllegalArgumentException(e.toString()).initCause(e);
            }
        }
    }
    
    // #67524: Properties Editor doesn't support Date type. Replaces them with '???'
    public String getJavaInitializationString () {
        return "new java.util.Date(" + ((Date) getValue()).getTime() + "L)"; // NOI18N
    }

}
