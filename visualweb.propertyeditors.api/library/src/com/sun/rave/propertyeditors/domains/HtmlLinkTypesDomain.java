/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
