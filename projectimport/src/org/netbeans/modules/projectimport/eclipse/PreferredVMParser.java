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

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import org.openide.ErrorManager;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.netbeans.modules.projectimport.ProjectImporterException;
import org.openide.xml.XMLUtil;
import org.xml.sax.XMLReader;

/**
 * Parses default JRE containers from Eclipse workspace.
 *
 * @author mkrauskopf
 */
final class PreferredVMParser extends DefaultHandler {
    
    // elements names
    private static final String VM_SETTINGS = "vmSettings";
    private static final String VM_TYPE = "vmType";
    private static final String VM = "vm";
    
    // attributes names
    private static final String DEFAULT_VM_ATTR = "defaultVM";
    private static final String ID_ATTR = "id";
    private static final String NAME_ATTR = "name";
    private static final String PATH_ATTR = "path";
    
    // indicates current position in a xml document
    private static final int POSITION_NONE = 0;
    private static final int POSITION_VM_SETTINGS = 1;
    private static final int POSITION_VM_TYPE = 2;
    private static final int POSITION_VM = 3;
    
    private int position = POSITION_NONE;
    private StringBuffer chars;
    private String defaultId;
    
    private Map jdks;
    
    private PreferredVMParser() {/* emtpy constructor */}
    
    /** Returns vmMap of JDKs */
    static Map parse(String vmXML) throws ProjectImporterException {
        PreferredVMParser parser = new PreferredVMParser();
        parser.load(new InputSource(new StringReader(vmXML)));
        return parser.jdks;
    }
    
    /** Parses a given InputSource and fills up jdk vmMap */
    private void load(InputSource vmXMLIS) throws ProjectImporterException{
        try {
            XMLReader reader = XMLUtil.createXMLReader(false, true);
            reader.setContentHandler(this);
            reader.setErrorHandler(this);
            chars = new StringBuffer(); // initialization
            reader.parse(vmXMLIS); // start parsing
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
                if (localName.equals(VM_SETTINGS)) {
                    position = POSITION_VM_SETTINGS;
                    // default vm id seems to be after the last comma
                    String defaultVMAttr = attributes.getValue(DEFAULT_VM_ATTR);
                    defaultId = defaultVMAttr.substring(defaultVMAttr.lastIndexOf(',') + 1);
                    jdks = new HashMap();
                } else {
                    throw (new SAXException("First element has to be "
                            + VM_SETTINGS + ", but is " + localName));
                }
                break;
            case POSITION_VM_SETTINGS:
                if (localName.equals(VM_TYPE)) {
                    position = POSITION_VM_TYPE;
                }
                break;
            case POSITION_VM_TYPE:
                if (localName.equals(VM)) {
                    position = POSITION_VM;
                    addJDK(attributes.getValue(ID_ATTR),
                            attributes.getValue(NAME_ATTR),
                            attributes.getValue(PATH_ATTR));
                }
                break;
            default:
                throw (new SAXException("Unknown position reached: "
                        + position + " (element: " + localName + ")"));
        }
    }
    
    public void endElement(String uri, String localName, String qName) throws
            SAXException {
        switch (position) {
            case POSITION_VM_SETTINGS:
                // parsing ends
                position = POSITION_NONE;
                break;
            case POSITION_VM_TYPE:
                position = POSITION_VM_SETTINGS;
                break;
            case POSITION_VM:
                position = POSITION_VM_TYPE;
                break;
            default:
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                        "Unknown state reached in ClassPathParser, " +
                        "position: " + position);
        }
        chars.setLength(0);
    }
    
    public void error(SAXParseException e) throws SAXException {
        ErrorManager.getDefault().log(ErrorManager.WARNING, "Error occurres: " + e);
        throw e;
    }
    
    public void fatalError(SAXParseException e) throws SAXException {
        ErrorManager.getDefault().log(ErrorManager.WARNING, "Fatal error occurres: " + e);
        throw e;
    }
    
    private void addJDK(String id, String name, String value) {
        if (id.equals(defaultId)) {
            // put the default twice under two names. It seems that under some
            // circumstances there is full name in .classpath con entry even
            // if the currently used container is default one.
            jdks.put(Workspace.DEFAULT_JRE_CONTAINER, value);
        }
        jdks.put(name, value);
    }
}
