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
 * <p>Custom by-type converter for <code>java.sql.Time</code> instances.</p>
 */

public class SqlTimeConverter extends DateTimeConverter {

    public SqlTimeConverter() {
        setTimeZone(TimeZone.getDefault());
        setType("time");
    }


    // ------------------------------------------------------- Converter Methods


    public Object getAsObject(FacesContext context, UIComponent component,
                              String value) {

        java.util.Date converted =
            (java.util.Date) super.getAsObject(context, component, value);
        if (converted != null) {
            return new java.sql.Time(converted.getTime());
        } else {
            return null;
        }

    }


}
