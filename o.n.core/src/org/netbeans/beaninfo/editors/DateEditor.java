/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
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
        if (text.equals("")) { // NOI18N
            setValue(null);
        } else {
            try {
                setValue(fmt.parse(text));
            } catch (ParseException e) {
                throw (IllegalArgumentException)new IllegalArgumentException(e.toString()).initCause(e);
            }
        }
    }
    
}
