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
 * Domain for attribute constants used to identify the which sides of an HTML
 * table border are visible.
 *
 */
public class HtmlTableRulesDomain extends Domain {

    private static Element[] elements = new Element[] {
        new Element("all", bundle.getMessage("HtmlTableRules.all")),
        new Element("cols", bundle.getMessage("HtmlTableRules.cols")),
        new Element("groups", bundle.getMessage("HtmlTableRules.groups")),
        new Element("none", bundle.getMessage("HtmlTableRules.none")),
        new Element("rows", bundle.getMessage("HtmlTableRules.rows"))
    };

    public Element[] getElements() {
        return HtmlTableRulesDomain.elements;
    }

    public String getDisplayName() {
        return bundle.getMessage("HtmlTableRules.displayName");
    }

}
