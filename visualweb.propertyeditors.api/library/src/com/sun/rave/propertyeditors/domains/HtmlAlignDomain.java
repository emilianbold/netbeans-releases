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
 * Domain for general alignment in all directions.
 *
 */
public class HtmlAlignDomain extends Domain {

    private static Element[] elements = new Element[] {
        new Element("top", bundle.getMessage("HtmlAlign.top")), //NOI18N
        new Element("middle", bundle.getMessage("HtmlAlign.middle")), //NOI18N
        new Element("bottom", bundle.getMessage("HtmlAlign.bottom")), //NOI18N
        new Element("left", bundle.getMessage("HtmlAlign.left")), //NOI18N
        new Element("right", bundle.getMessage("HtmlAlign.right")) //NOI18N
    };

    public Element[] getElements() {
        return HtmlAlignDomain.elements;
    }

}
