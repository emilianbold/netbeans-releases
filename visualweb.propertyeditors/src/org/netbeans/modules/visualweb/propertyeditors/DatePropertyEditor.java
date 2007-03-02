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
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.openide.util.NbBundle;

import com.sun.rave.designtime.DesignProperty;

/**
 * An in-line property editor for java.util.Date objects. This editor looks for
 * a "dateFormatPattern" property to use to parse and format a Date.
 *
 * @author Edwin Goei
 */
public class DatePropertyEditor extends PropertyEditorBase {

    /**
     * Key used to specify a date format pattern within a property descriptor.
     */
    public final static String DATE_FORMAT_PATTERN = "dateFormatPattern"; //NOI18N

    private DateFormat getDateFormat() {
        // Look for a "dateFormat" property on the component and use that
        DesignProperty patternProp = getDesignProperty().getDesignBean().getProperty(DATE_FORMAT_PATTERN);
        Object patternPropValue = patternProp.getValue();
        if (patternPropValue instanceof String) {
            DateFormat df = new SimpleDateFormat((String) patternPropValue);
            return df;
        } else {
            // Fallback to using the default locale DateFormat
            return DateFormat.getDateInstance(DateFormat.SHORT);
        }
    }

    public String getAsText() {
        Date d = (Date) getValue();
        if (d != null) {
            return getDateFormat().format(d);
        } else {
            return ""; // NOI18N
        }
    }

    public void setAsText(String text) {
        if (text.trim().length() == 0) {
            setValue(null);
            return;
        }

        try {
            setValue(getDateFormat().parse(text));
        } catch (ParseException e) {
            String pattern = NbBundle.getMessage(DatePropertyEditor.class,
                    "DatePropertyEditor.formatErrorMessage",
                    new String[] { text });
            throw new IllegalTextArgumentException(MessageFormat.format(
                    pattern, new String[] { text }), e);
        }
    }

    public String getJavaInitializationString() {
        Date date = (Date) getValue();
        GregorianCalendar cal = new GregorianCalendar();
        cal.setTime(date);
        // (new GregorianCalendar(year, month0, day)).getTime()
        return "(new java.util.GregorianCalendar(" + cal.get(Calendar.YEAR)
                + ", " + cal.get(Calendar.MONTH) + ", "
                + cal.get(Calendar.DAY_OF_MONTH) + ")).getTime()";
    }
}
