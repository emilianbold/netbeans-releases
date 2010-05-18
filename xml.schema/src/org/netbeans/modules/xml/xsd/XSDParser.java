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

package org.netbeans.modules.xml.xsd;

import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.XMLReader;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;

import org.netbeans.api.xml.services.UserCatalog;

/**
 *
 * @author  anovak
 */
public class XSDParser {

    /** Creates a new instance of XSDParser */
    public XSDParser() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.err.println("Error: missing file parameter required or too many args");
        }
        
        java.io.FileInputStream fistr = new java.io.FileInputStream(args[0]);
        
        SAXParserFactory factory = SAXParserFactory.newInstance();
        XMLReader reader = factory.newSAXParser().getXMLReader();
        XSDContentHandler handler = new XSDContentHandler(System.out);
        reader.setContentHandler(handler);
        reader.parse(new InputSource(fistr));
    }

    public XSDGrammar parse(InputSource in) {

        XSDContentHandler handler = new XSDContentHandler(System.out);
        
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            XMLReader reader = factory.newSAXParser().getXMLReader();

            UserCatalog catalog = UserCatalog.getDefault();
            EntityResolver res = (catalog == null ? null : catalog.getEntityResolver());

            if (res != null) { 
                reader.setEntityResolver(res);
            }

            reader.setContentHandler(handler);
            reader.parse(in);
            return handler.getGrammar();
        } catch (org.xml.sax.SAXException ex) {
            if (Boolean.getBoolean("netbeans.debug.xml") ||  Boolean.getBoolean("netbeans.debug.exceptions")) {  //NOI18N
                ex.printStackTrace();            
                if (ex.getException() instanceof RuntimeException) {
                    ex.getException().printStackTrace();  //???
                }            
            }
            return handler.getGrammar();  // better partial result than nothing
        } catch (java.io.IOException ex) {
            if (Boolean.getBoolean("netbeans.debug.xml")) {  // NOI18N
                ex.printStackTrace();
            }
            return handler.getGrammar();  // better partial result than nothing
        } catch (javax.xml.parsers.ParserConfigurationException e) {
            if (Boolean.getBoolean("netbeans.debug.xml")) {  // NOI18N
                e.printStackTrace();
            }
            return handler.getGrammar();  // better partial result than nothing
        }            
    }
}
