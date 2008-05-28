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

package org.netbeans.modules.projectimport.eclipse;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import org.openide.ErrorManager;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.netbeans.modules.projectimport.ProjectImporterException;

/**
 * Parses user library xml document.
 *
 * @author mkrauskopf
 */
final class UserLibraryParser extends DefaultHandler {
    
    // elements names
    private static final String USER_LIBRARY = "userlibrary"; // NOI18N
    private static final String ARCHIVE = "archive"; // NOI18N
    private static final String ATTRIBUTES = "attributes"; // NOI18N
    private static final String ATTRIBUTE = "attribute"; // NOI18N
    
    // attributes names
    private static final String PATH_ATTR = "path"; // NOI18N
    
    // indicates current position in a xml document
    private static final int POSITION_NONE = 0;
    private static final int POSITION_USER_LIBRARY = 1;
    private static final int POSITION_ARCHIVE = 2;
    private static final int POSITION_ATTRIBUTES = 3;
    private static final int POSITION_ATTRIBUTE = 4;
    
    private int position = POSITION_NONE;
    private StringBuffer chars;
    
    private List<String> jars;
    
    private UserLibraryParser() {/* emtpy constructor */}
    
    /** Returns jars contained in the given user library. */
    static List<String> getJars(String xmlDoc) throws ProjectImporterException {
        UserLibraryParser parser = new UserLibraryParser();
        parser.load(new InputSource(new StringReader(xmlDoc)));
        return parser.jars;
    }
    
    /** Parses a given InputSource and fills up jars collection */
    private void load(InputSource projectIS) throws ProjectImporterException{
        try {
            /* parser creation */
            XMLReader reader = XMLUtil.createXMLReader(false, true);
            reader.setContentHandler(this);
            reader.setErrorHandler(this);
            chars = new StringBuffer(); // initialization
            reader.parse(projectIS); // start parsing
        } catch (IOException e) {
            throw new ProjectImporterException(e);
        } catch (SAXException e) {
            throw new ProjectImporterException(e);
        }
    }
    
    public void characters(char ch[], int offset, int length) throws SAXException {
        chars.append(ch, offset, length);
    }
    
    public void startElement(String uri, String localName,
            String qName, Attributes attributes) throws SAXException {
        
        chars.setLength(0);
        switch (position) {
            case POSITION_NONE:
                if (localName.equals(USER_LIBRARY)) {
                    position = POSITION_USER_LIBRARY;
                    jars = new ArrayList<String>();
                } else {
                    throw (new SAXException("First element has to be " // NOI18N
                            + USER_LIBRARY + ", but is " + localName)); // NOI18N
                }
                break;
            case POSITION_USER_LIBRARY:
                if (localName.equals(ARCHIVE)) {
                    jars.add(attributes.getValue(PATH_ATTR));
                    position = POSITION_ARCHIVE;
                }
                break;
            case POSITION_ARCHIVE:
                if (localName.equals(ATTRIBUTES)) {
                    // ignored in the meantime - prepared for future (see #75112)
                    position = POSITION_ATTRIBUTES;
                }
                break;
            case POSITION_ATTRIBUTES:
                if (localName.equals(ATTRIBUTE)) {
                    // ignored in the meantime - prepared for future (see #75112)
                    position = POSITION_ATTRIBUTE;
                }
                break;
            default:
                throw (new SAXException("Unknown element reached: " // NOI18N
                        + localName));
        }
    }
    
    public void endElement(String uri, String localName, String qName) throws
            SAXException {
        switch (position) {
            case POSITION_USER_LIBRARY:
                // parsing ends
                position = POSITION_NONE;
                break;
            case POSITION_ARCHIVE:
                position = POSITION_USER_LIBRARY;
                break;
            case POSITION_ATTRIBUTES:
                position = POSITION_ARCHIVE;
                break;
            case POSITION_ATTRIBUTE:
                position = POSITION_ATTRIBUTES;
                break;
            default:
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                        "Unknown state reached in UserLibraryParser, " + // NOI18N
                        "position: " + position); // NOI18N
        }
        chars.setLength(0);
    }
    
    public void warning(SAXParseException e) throws SAXException {
        ErrorManager.getDefault().log(ErrorManager.WARNING, "Warning occurred: " + e);
    }
    
    public void error(SAXParseException e) throws SAXException {
        ErrorManager.getDefault().log(ErrorManager.WARNING, "Error occurres: " + e);
        throw e;
    }
    
    public void fatalError(SAXParseException e) throws SAXException {
        ErrorManager.getDefault().log(ErrorManager.WARNING, "Fatal error occurres: " + e);
        throw e;
    }
}
