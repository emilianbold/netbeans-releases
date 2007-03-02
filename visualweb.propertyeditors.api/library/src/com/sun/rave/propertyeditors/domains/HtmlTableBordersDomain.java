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
public class HtmlTableBordersDomain extends Domain {

    private static Element[] elements = new Element[] {
        new Element("above", bundle.getMessage("HtmlTableBorders.above")),
        new Element("below", bundle.getMessage("HtmlTableBorders.below")),
        new Element("box", bundle.getMessage("HtmlTableBorders.box")),
        new Element("hsides", bundle.getMessage("HtmlTableBorders.hsides")),
        new Element("lhs", bundle.getMessage("HtmlTableBorders.lhs")),
        new Element("rhs", bundle.getMessage("HtmlTableBorders.rhs")),
        new Element("void", bundle.getMessage("HtmlTableBorders.void")),
        new Element("vsides", bundle.getMessage("HtmlTableBorders.vsides"))
    };

    public Element[] getElements() {
        return HtmlTableBordersDomain.elements;
    }

    public String getDisplayName() {
        return bundle.getMessage("HtmlTableBorders.displayName");
    }

}
