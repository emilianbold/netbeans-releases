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
package org.netbeans.modules.visualweb.faces.dt.converter;

import com.sun.rave.propertyeditors.domains.Domain;
import com.sun.rave.propertyeditors.domains.Element;
import java.util.ResourceBundle;

/**
 * Domain for link types defined by HTML 4.01. If not specified, the default
 * behavior is undefined.
 *
 * @author gjmurphy
 */
public class DateTimeTypesDomain extends Domain {
    
    private static ResourceBundle bundle = 
            ResourceBundle.getBundle("org.netbeans.modules.visualweb.faces.dt.converter.Bundle");

    private static Element[] elements = new Element[] {
        new Element("date", bundle.getString("date")),
        new Element("time", bundle.getString("time")),
        new Element("both", bundle.getString("both"))
    };

    public Element[] getElements() {
        return DateTimeTypesDomain.elements;
    }

}
