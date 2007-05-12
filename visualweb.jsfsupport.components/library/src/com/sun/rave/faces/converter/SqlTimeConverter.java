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
