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
 * Editable domain of MIME types, as defined by IANA RFC 2045 and RFC 2046.
 * Only the more commonly used types are provided by default, but the user may
 * add more. Edits of this domain are available Project-wide.
 *
 */
// TODO - When DesignContext.getProject().getGlobalData() fixed, make this domain IDE-scoped
public class MimeTypesDomain extends EditableDomain {

    public MimeTypesDomain() {
        super(EditableDomain.PROJECT_STORAGE, String.class);
        this.elements.add( new Element("application/msword"));
        this.elements.add( new Element("application/pdf"));
        this.elements.add( new Element("application/postscript"));
        this.elements.add( new Element("application/rtf"));
        this.elements.add( new Element("application/soap+xml"));
        this.elements.add( new Element("application/xml"));
        this.elements.add( new Element("application/zip"));
        this.elements.add( new Element("image/jpeg"));
        this.elements.add( new Element("image/gif"));
        this.elements.add( new Element("image/tiff"));
        this.elements.add( new Element("image/png"));
        this.elements.add( new Element("text/css"));
        this.elements.add( new Element("text/html"));
        this.elements.add( new Element("text/plain"));
        this.elements.add( new Element("text/rtf"));
        this.elements.add( new Element("text/xml"));
    }

    public String getDisplayName() {
        return bundle.getMessage("MimeTypes.displayName");
    }

}
