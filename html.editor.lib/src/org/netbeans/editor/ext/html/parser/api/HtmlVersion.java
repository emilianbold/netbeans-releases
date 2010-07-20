/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.editor.ext.html.parser.api;

import org.netbeans.editor.ext.html.dtd.DTD;

/**
 *
 * @author marekfukala
 */
public enum HtmlVersion {

    HTML32("-//W3C//DTD HTML 3.2 Final//EN"), //NOI18N

    HTML40_STRICT("-//W3C//DTD HTML 4.0//EN"), //NOI18N
    HTML40_TRANSATIONAL("-//W3C//DTD HTML 4.0 Transitional//EN"), //NOI18N
    HTML40_FRAMESET("-//W3C//DTD HTML 4.0 Frameset//EN"), //NOI18N

    HTML41_STRICT("-//W3C//DTD HTML 4.01//EN"), //NOI18N
    HTML41_TRANSATIONAL("-//W3C//DTD HTML 4.01 Transitional//EN"), //NOI18N
    HTML41_FRAMESET("-//W3C//DTD HTML 4.01 Frameset//EN"), //NOI18N

    HTML5(null), //no public id, just <!doctype html>

    XHTML10_STICT("-//W3C//DTD XHTML 1.0 Strict//EN", null, "http://www.w3.org/1999/xhtml", true), //NOI18N
    XHTML10_TRANSATIONAL("-//W3C//DTD XHTML 1.0 Transitional//EN", null, "http://www.w3.org/1999/xhtml", true), //NOI18N
    XHTML10_FRAMESET("-//W3C//DTD XHTML 1.0 Frameset//EN", null, "http://www.w3.org/1999/xhtml", true), //NOI18N

    //XHTML 1.1 version fallbacks to XHTML 1.0 strict since the current SGML parser
    //cannot properly parse the XHTML1.1 dtd
    XHTML11("-//W3C//DTD XHTML 1.1//EN", "-//W3C//DTD XHTML 1.0 Strict//EN", "http://www.w3.org/1999/xhtml", true); //NOI18N

    public static HtmlVersion findByPublicId(String publicId) {
        for(HtmlVersion version : HtmlVersion.values()) {
            if(publicId == null && publicId == version.publicID || publicId.equals(version.getPublicID())) {
                return version;
            }
        }
        return null;
    }

    public static HtmlVersion findByNamespace(String namespace) {
        for(HtmlVersion version : HtmlVersion.values()) {
            if(namespace.equals(version.getDefaultNamespace())) {
                return version;
            }
        }
        return null;

    }

    private final String publicID;
    private final String fallbackPublicID;
    private final String defaultNamespace;
    private boolean isXhtml;

    private HtmlVersion(String publicID) {
        this(publicID, null, null, false);
    }

    private HtmlVersion(String publicID, String fallbackPublicID, String defaultNamespace, boolean isXhtml) {
        this.publicID = publicID;
        this.defaultNamespace = defaultNamespace;
        this.isXhtml = isXhtml;
        this.fallbackPublicID = fallbackPublicID;
    }

    public String getPublicID() {
        return publicID;
    }

    public String getDefaultNamespace() {
        return this.defaultNamespace;
    }

    public boolean isXhtml() {
        return this.isXhtml;
    }

    public DTD getDTD() {
        //use the fallback public id to get the DTD if defined, otherwise
        //use the proper public id. This is needed due to the lack of parser
        //for XHTML 1.1 file. Such files are parsed according to the XHTML1.0 DTD.
        String publicid = fallbackPublicID != null ? fallbackPublicID : publicID;

        return org.netbeans.editor.ext.html.dtd.Registry.getDTD(publicid, null);
    }



}
