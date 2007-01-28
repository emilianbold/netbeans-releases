/*
 * {START_JAVA_COPYRIGHT_NOTICE
 * Copyright 2006 Sun Microsystems, Inc. All rights reserved.
 * Use is subject to license terms.
 * END_COPYRIGHT_NOTICE}
 */

package com.sun.rave.faces.converter;


import java.util.TimeZone;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.DateTimeConverter;


/**
 * <p>Custom by-type converter for <code>java.sql.Timestamp</code>
 * instances.</p>
 */

public class SqlTimestampConverter extends DateTimeConverter {

    public SqlTimestampConverter() {
        setTimeZone(TimeZone.getDefault());
        setType("both");
    }


    // ------------------------------------------------------- Converter Methods


    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) {

        java.util.Date converted =
            (java.util.Date) super.getAsObject(context, component, value);
        if (converted != null) {
            return new java.sql.Timestamp(converted.getTime());
        } else {
            return null;
        }

    }


}
