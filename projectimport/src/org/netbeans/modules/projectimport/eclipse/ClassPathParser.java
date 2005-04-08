/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
 * Parses .classpath file and sets the given project's classpath.
 *
 * @author mkrauskopf
 */
final class ClassPathParser extends DefaultHandler {
    
    // elements names
    private static final String CLASSPATH = "classpath";
    private static final String CLASSPATH_ENTRY = "classpathentry";
    private static final String ATTRIBUTES = "attributes";
    
    // attributes names
    private static final String KIND_ATTR = "kind";
    private static final String PATH_ATTR = "path";
    
    // indicates current position in a xml document
    private static final int POSITION_NONE = 0;
    private static final int POSITION_CLASSPATH = 1;
    private static final int POSITION_CLASSPATH_ENTRY = 2;
    private static final int POSITION_ATTRIBUTES = 3;
    
    private int position = POSITION_NONE;
    private StringBuffer chars;
    
    private ClassPath classPath;
    
    private ClassPathParser() {/* emtpy constructor */}
    
    /** Returns classpath content from project's .classpath file */
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
                            "Unable to close classPathStream: " + e);
                }
            }
        }
        return parser.classPath;
    }
    
    /** Returns classpath content from project's .classpath file */
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
                    throw (new SAXException("First element has to be "
                            + CLASSPATH + ", but is " + localName));
                }
                break;
            case POSITION_CLASSPATH:
                if (localName.equals(CLASSPATH_ENTRY)) {
                    ClassPathEntry entry = ClassPathEntry.create(
                            attributes.getValue(KIND_ATTR),
                            attributes.getValue(PATH_ATTR));
                    classPath.addEntry(entry);
                    position = POSITION_CLASSPATH_ENTRY;
                }
                break;
            case POSITION_CLASSPATH_ENTRY:
                if (localName.equals(ATTRIBUTES)) {
                    // ignored in the meantime - prepared for future 
                    // (probably since elicpse 3.1M6 - see #57661)
                    position = POSITION_ATTRIBUTES;
                }
                break;
            default:
                throw (new SAXException("Unknown element reached: "
                        + localName));
        }
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
            default:
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                        "Unknown state reached in ClassPathParser, " +
                        "position: " + position);
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
