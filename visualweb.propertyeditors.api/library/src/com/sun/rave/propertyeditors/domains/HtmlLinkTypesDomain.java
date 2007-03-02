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
 * Domain for link types defined by HTML 4.01. If not specified, the default
 * behavior is undefined.
 *
 */
public class HtmlLinkTypesDomain extends Domain {

    private static Element[] elements = new Element[] {
        new Element("alternate", bundle.getMessage("HtmlLinkTypes.alternate.label"),
                bundle.getMessage("HtmlLinkTypes.alternate.desc")),
        new Element("appendix", bundle.getMessage("HtmlLinkTypes.appendix.label"),
                bundle.getMessage("HtmlLinkTypes.appendix.desc")),
        new Element("bookmark", bundle.getMessage("HtmlLinkTypes.bookmark.label"),
                bundle.getMessage("HtmlLinkTypes.bookmark.desc")),
        new Element("chapter", bundle.getMessage("HtmlLinkTypes.chapter.label"),
                bundle.getMessage("HtmlLinkTypes.chapter.desc")),
        new Element("copyright", bundle.getMessage("HtmlLinkTypes.copyright.label"),
                bundle.getMessage("HtmlLinkTypes.copyright.desc")),
        new Element("contents", bundle.getMessage("HtmlLinkTypes.contents.label"),
                bundle.getMessage("HtmlLinkTypes.contents.desc")),
        new Element("glossary", bundle.getMessage("HtmlLinkTypes.glossary.label"),
                bundle.getMessage("HtmlLinkTypes.glossary.desc")),
        new Element("help", bundle.getMessage("HtmlLinkTypes.help.label"),
                bundle.getMessage("HtmlLinkTypes.help.desc")),
        new Element("index", bundle.getMessage("HtmlLinkTypes.index.label"),
                bundle.getMessage("HtmlLinkTypes.index.desc")),
        new Element("next", bundle.getMessage("HtmlLinkTypes.next.label"),
                bundle.getMessage("HtmlLinkTypes.next.desc")),
        new Element("prev", bundle.getMessage("HtmlLinkTypes.prev.label"),
                bundle.getMessage("HtmlLinkTypes.prev.desc")),
        new Element("section", bundle.getMessage("HtmlLinkTypes.section.label"),
                bundle.getMessage("HtmlLinkTypes.section.desc")),
        new Element("stylesheet", bundle.getMessage("HtmlLinkTypes.stylesheet.label"),
                bundle.getMessage("HtmlLinkTypes.stylesheet.desc")),
        new Element("start", bundle.getMessage("HtmlLinkTypes.start.label"),
                bundle.getMessage("HtmlLinkTypes.start.desc")),
        new Element("subsection", bundle.getMessage("HtmlLinkTypes.subsection.label"),
                bundle.getMessage("HtmlLinkTypes.subsection.desc"))
    };

    public Element[] getElements() {
        return HtmlLinkTypesDomain.elements;
    }

    public String getDisplayName() {
        return bundle.getMessage("HtmlLinkTypes.displayName");
    }

}
