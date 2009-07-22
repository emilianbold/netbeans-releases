/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.web.jsf.editor.tld;

import java.io.IOException;
import java.net.URL;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author marekfukala
 */
public class TldEntityResolver implements EntityResolver {

    public static final String TAGLIB_SCHEMA_21_URI = "http://java.sun.com/xml/ns/javaee/web-jsptaglibrary_2_1.xsd"; //NOI18N
    public static final String TAGLIB_SCHEMA_21_RESOURCE = "org/netbeans/modules/web/jsf/editor/tld/resources/web-jsptaglibrary_2_1.xsd"; //NOI18N

    public  static final String TAGLIB_DTD_JSP11_PUBLICID="-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.1//EN"; // NOI18N
    public static final String TAGLIB_DTD_JSP11_RESOURCE = "org/netbeans/modules/web/jsf/editor/tld/resources/web-jsptaglibrary_1_1.dtd"; //NOI18N

    public  static final String TAGLIB_DTD_JSP12_PUBLICID="-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN"; // NOI18N
    public static final String TAGLIB_DTD_JSP12_RESOURCE = "org/netbeans/modules/web/jsf/editor/tld/resources/web-jsptaglibrary_1_2.dtd"; //NOI18N


    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
//        System.out.println("resolving publicid = " + publicId + " systemid = " + systemId);

        if(systemId.equals(TAGLIB_SCHEMA_21_URI)){
            return getResource(TAGLIB_DTD_JSP11_RESOURCE);
        } else if(publicId.equals(TAGLIB_DTD_JSP11_PUBLICID)) {
            return getResource(TAGLIB_DTD_JSP11_RESOURCE);
        } else if(publicId.equals(TAGLIB_DTD_JSP12_PUBLICID)) {
            return getResource(TAGLIB_DTD_JSP12_RESOURCE);
        }
        
        return null;
    }

    private InputSource getResource(String location) {
        URL url = Thread.currentThread().getContextClassLoader().getResource(location);
        return new InputSource(url.toString());
    }

}