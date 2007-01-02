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

package org.netbeans.modules.projectimport.eclipse;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import org.netbeans.modules.projectimport.ProjectImporterException;
import org.openide.ErrorManager;
import org.openide.xml.XMLUtil;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Parses a content of the .classpath file.
 *
 * @author mkrauskopf
 */
final class ClassPathParser extends DefaultHandler {
    
    // elements names
    private static final String CLASSPATH = "classpath"; // NOI18N
    private static final String CLASSPATH_ENTRY = "classpathentry"; // NOI18N
    private static final String ATTRIBUTES = "attributes"; // NOI18N
    private static final String ATTRIBUTE = "attribute"; // NOI18N
    private static final String ACCESSRULES = "accessrules"; // NOI18N
    private static final String ACCESSRULE = "accessrule"; // NOI18N
    
    // attributes names
    private static final String KIND_ATTR = "kind"; // NOI18N
    private static final String PATH_ATTR = "path"; // NOI18N
    
    // indicates current position in a xml document
    private static final int POSITION_NONE = 0;
    private static final int POSITION_CLASSPATH = 1;
    private static final int POSITION_CLASSPATH_ENTRY = 2;
    private static final int POSITION_ATTRIBUTES = 3;
    private static final int POSITION_ATTRIBUTE = 4;
    private static final int POSITION_ACCESSRULES = 5;
    private static final int POSITION_ACCESSRULE = 6;
    
    private int position = POSITION_NONE;
    private StringBuffer chars;
    
    private ClassPath classPath;
    
    private ClassPathParser() {/* emtpy constructor */}
    
    /**
     * Returns classpath content from a project's .classpath file.
     *
     * @param classPathFile the .classpath file
     */
    static ClassPath parse(File classPathFile) throws ProjectImporterException {
        
        ClassPathParser parser = new ClassPathParser();
        InputStream classPathIS = null;
        try {
            classPathIS = new BufferedInputStream(
                    new FileInputStream(classPathFile));
            parser.load(new InputSource(classPathIS));
        } catch (FileNotFoundException e) {
            throw new ProjectImporterException(e);
        } finally {
            if (classPathIS != null) {
                try {
                    classPathIS.close();
                } catch (IOException e) {
                    ErrorManager.getDefault().log(ErrorManager.WARNING,
                            "Unable to close classPathStream: " + e); // NOI18N
                }
            }
        }
        return parser.classPath;
    }
    
    /**
     * Returns classpath content as it is represented in the project's
     * .classpath file.
     *
     * @param classPath string as it is in the .classpath file
     */
    static ClassPath parse(String classPath) throws ProjectImporterException {
        ClassPathParser parser = new ClassPathParser();
        parser.load(new InputSource(new StringReader(classPath)));
        return parser.classPath;
    }
    
    /** Parses a given InputSource and fills up a EclipseProject */
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
                if (localName.equals(CLASSPATH)) {
                    position = POSITION_CLASSPATH;
                    classPath = new ClassPath();
                } else {
                    unknownElementReached(position, localName);
                }
                break;
            case POSITION_CLASSPATH:
                if (localName.equals(CLASSPATH_ENTRY)) {
                    ClassPathEntry entry = new ClassPathEntry(
                            attributes.getValue(KIND_ATTR),
                            attributes.getValue(PATH_ATTR));
                    classPath.addEntry(entry);
                    position = POSITION_CLASSPATH_ENTRY;
                } else {
                    unknownElementReached(position, localName);
                }
                break;
            case POSITION_CLASSPATH_ENTRY:
                if (localName.equals(ATTRIBUTES)) {
                    // ignored in the meantime - prepared for future
                    // (probably since elicpse 3.1M6 - see #57661)
                    position = POSITION_ATTRIBUTES;
                } else if (localName.equals(ACCESSRULES)) {
                    // ignored in the meantime - prepared for future
                    position = POSITION_ACCESSRULES;
                } else {
                    unknownElementReached(position, localName);
                }
                break;
            case POSITION_ATTRIBUTES:
                if (localName.equals(ATTRIBUTE)) {
                    // ignored in the meantime - prepared for future
                    // (probably since elicpse 3.1M6 - see #57661)
                    position = POSITION_ATTRIBUTE;
                } else {
                    unknownElementReached(position, localName);
                }
                break;
            case POSITION_ACCESSRULES:
                if (localName.equals(ACCESSRULE)) {
                    // ignored in the meantime - prepared for future
                    position = POSITION_ACCESSRULE;
                } else {
                    unknownElementReached(position, localName);
                }
                break;
            default:
                unknownElementReached(position, localName);
        }
    }
    
    private void unknownElementReached(int position, String localName) throws SAXException {
        throw new SAXException("Unknown element reached: " + // NOI18N
                "position: " + position + ", element: " + localName); // NOI18N
    }
    
    public void endElement(String uri, String localName, String qName) throws
            SAXException {
        switch (position) {
            case POSITION_CLASSPATH:
                // parsing ends
                position = POSITION_NONE;
                break;
            case POSITION_CLASSPATH_ENTRY:
                position = POSITION_CLASSPATH;
                break;
            case POSITION_ATTRIBUTES:
                position = POSITION_CLASSPATH_ENTRY;
                break;
            case POSITION_ATTRIBUTE:
                position = POSITION_ATTRIBUTES;
                break;
            case POSITION_ACCESSRULES:
                position = POSITION_CLASSPATH_ENTRY;
                break;
            case POSITION_ACCESSRULE:
                position = POSITION_ACCESSRULES;
                break;
            default:
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                        "Unknown state reached in ClassPathParser, " + // NOI18N
                        "position: " + position + ", element: " + localName); // NOI18N
        }
        chars.setLength(0);
    }
    
    public void warning(SAXParseException e) throws SAXException {
        ErrorManager.getDefault().log(ErrorManager.WARNING, "Warning occurred: " + e);
    }
    
    public void error(SAXParseException e) throws SAXException {
        ErrorManager.getDefault().log(ErrorManager.WARNING, "Error occurred: " + e);
        throw e;
    }
    
    public void fatalError(SAXParseException e) throws SAXException {
        ErrorManager.getDefault().log(ErrorManager.WARNING, "Fatal error occurred: " + e);
        throw e;
    }
    
}
