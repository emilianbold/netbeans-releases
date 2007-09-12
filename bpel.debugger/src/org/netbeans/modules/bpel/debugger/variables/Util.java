/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.bpel.debugger.variables;

import java.io.IOException;
import java.io.StringReader;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author Alexander Zgursky
 */
public final class Util {
    
    /** Creates a new instance of Util */
    private Util() {
    }
    
    public static Element parseXmlElement(String xml) {
        Document doc = null;
        if (xml != null && xml.length() > 0) {
            InputSource is = new InputSource(new StringReader(xml));
            try {
                doc = getDocumentBuilder().parse(is);
            } catch (ParserConfigurationException e) {
                //TODO:handle this properly
                //e.printStackTrace();
            } catch (IOException e) {
                //TODO:handle this properly
                //e.printStackTrace();
            } catch (SAXException e) {
                //TODO:handle this properly
                //e.printStackTrace();
            }
        }

        if (doc != null) {
            return doc.getDocumentElement();
        } else {
            return null;
        }
    }
    
    private static DocumentBuilder cDocumentBuilder;
    
    private static DocumentBuilder getDocumentBuilder() throws ParserConfigurationException {
        if (cDocumentBuilder == null) {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setIgnoringComments(false);
            factory.setNamespaceAware(true);
            factory.setIgnoringElementContentWhitespace(false);
            cDocumentBuilder = factory.newDocumentBuilder();
        }
        return cDocumentBuilder;
    }
}
