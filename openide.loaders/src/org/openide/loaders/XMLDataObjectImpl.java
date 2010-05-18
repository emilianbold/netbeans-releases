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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.openide.loaders;


import java.io.IOException;
import java.util.Iterator;
import javax.xml.parsers.*;
import org.netbeans.modules.openide.loaders.RuntimeCatalog;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.xml.*;
import org.xml.sax.*;

/**
 * Class that hide implementations details of deprecated utility
 * methods provided at XMLDataObject.
 *
 * @author  Petr Kuzel
 */
class XMLDataObjectImpl extends Object {


    /** Create DOM builder using JAXP libraries. */
    static DocumentBuilder makeBuilder(boolean validate) throws IOException, SAXException {
        
        DocumentBuilder builder;
        DocumentBuilderFactory factory;

        //create factory according to javax.xml.parsers.SAXParserFactory property 
        //or platform default (i.e. com.sun...)
        try {
            factory = DocumentBuilderFactory.newInstance();
            factory.setValidating(validate);
            factory.setNamespaceAware(false);
        } catch (FactoryConfigurationError err) {
            notifyFactoryErr(err, "javax.xml.parsers.DocumentBuilderFactory"); //NOI18N
            throw err;
        }

        try {
            builder = factory.newDocumentBuilder();                
        } catch (ParserConfigurationException ex) {
            SAXException sex = new SAXException("Configuration exception."); // NOI18N
            sex.initCause(ex);
            Exceptions.attachLocalizedMessage(sex,
                                              "Can not create a DOM builder!\nCheck javax.xml.parsers.DocumentBuilderFactory property and the builder library presence on classpath."); // NOI18N
            throw sex;
        }
        
        return builder;
    }
    
    @Deprecated
    static Parser makeParser(boolean validate) {
        
        try {
            return new org.xml.sax.helpers.XMLReaderAdapter (XMLUtil.createXMLReader(validate));
        } catch (SAXException ex) {
            notifyNewSAXParserEx(ex);
            return null;
        }
        
    }

    /** Return XML reader or null if no provider exists. */
    static XMLReader makeXMLReader(boolean validating, boolean namespaces) {

        try {
            return XMLUtil.createXMLReader(validating,namespaces);
        } catch (SAXException ex) {
            notifyNewSAXParserEx(ex);
            return null;
        }
        
    }
    
    /** Annotate & notify the exception. */
    private static void notifyNewSAXParserEx (Exception ex) {
        Exceptions.attachLocalizedMessage(ex,
                                          "Can not create a SAX parser!\nCheck javax.xml.parsers.SAXParserFactory property features and the parser library presence on classpath."); // NOI18N
        Exceptions.printStackTrace(ex);
    }

    /** Annotate & notify the error. */
    private static void notifyFactoryErr(Error err, String property) {
        Exceptions.attachLocalizedMessage(err,
                                          "Can not create a factory!\nCheck " +
                                          property +
                                          "  property and the factory library presence on classpath."); // NOI18N
        Exceptions.printStackTrace(err);
    }

    // warning back compatability code!!!    
    static synchronized void registerCatalogEntry(String publicId, String uri) {
        Iterator it = Lookup.getDefault().lookupAll(EntityCatalog.class).iterator();
        while (it.hasNext()) {
            Object o = it.next();
            if (o instanceof RuntimeCatalog) {
                ((RuntimeCatalog) o).registerCatalogEntry(publicId, uri);
                return;
            }
        }
        assert false;
    }
    
}
