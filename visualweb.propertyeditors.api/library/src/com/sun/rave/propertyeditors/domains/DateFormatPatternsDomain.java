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
package com.sun.rave.propertyeditors.domains;

/**
 * Editable domain of common date format patterns. Only the more commonly
 * used types are provided by default, but the user may add more. Edits of this
 * domain are available Project-wide.
 */
// TODO - When DesignContext.getProject().getGlobalData() fixed, make this domain IDE-scoped
public class DateFormatPatternsDomain extends EditableDomain {

    public DateFormatPatternsDomain() {
        super(EditableDomain.PROJECT_STORAGE, String.class);
        this.elements.add(new Element("yyyy-MM-dd"));
        this.elements.add(new Element("MM-dd-yyyy"));
        this.elements.add(new Element("dd-MM-yyyy"));
        this.elements.add(new Element("yyyy/MM/dd"));
        this.elements.add(new Element("MM/dd/yyyy"));
        this.elements.add(new Element("dd/MM/yyyy"));
        this.elements.add(new Element("yyyy.MM.dd"));
        this.elements.add(new Element("MM.dd.yyyy"));
        this.elements.add(new Element("dd.MM.yyyy"));
    }
}
