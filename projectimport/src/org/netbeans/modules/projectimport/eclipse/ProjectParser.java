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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.openide.ErrorManager;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;
import org.netbeans.modules.projectimport.ProjectImporterException;
import org.openide.xml.XMLUtil;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

/**
 * Parses given project's .project file and fills up the project with found
 * data.
 *
 * @author mkrauskopf
 */
final class ProjectParser extends DefaultHandler {
    
    private EclipseProject project;
    private ClassPath.Link currentLink;
    
    private static final String JAVA_NATURE = "org.eclipse.jdt.core.javanature";
    
    // elements names
    private static final String PROJECT_DESCRIPTION = "projectDescription";
    private static final String LINKED_RESOURCES = "linkedResources";
    private static final String LINK = "link";
    private static final String NAME = "name";
    private static final String TYPE = "type";
    private static final String LOCATION = "location";
    private static final String NATURES = "natures";
    private static final String NATURE = "nature";
    
    // indicates current position in a xml document
    private static final int POSITION_NONE = 0;
    private static final int POSITION_PROJECT_DESCRIPTION = 1;
    private static final int POSITION_PROJECT_NAME = 2;
    private static final int POSITION_LINKED_RESOURCES = 3;
    private static final int POSITION_LINK = 4;
    private static final int POSITION_LINK_NAME = 5;
    private static final int POSITION_LINK_TYPE = 6;
    private static final int POSITION_LINK_LOCATION = 7;
    private static final int POSITION_NATURES = 8;
    private static final int POSITION_NATURE = 9;
    private static final int POSITION_UNUSED = 1000;
    
    private int position = POSITION_NONE;
    private int unusedInner = 0;
    private StringBuffer chars;
    
    /** Creates a new instance of ProjectParser */
    private ProjectParser(EclipseProject project) {
        this.project = project;
    }
    
    static void parse(EclipseProject project) throws ProjectImporterException {
        ProjectParser parser = new ProjectParser(project);
        parser.load();
    }
    
    /** Parses a given InputSource and fills up a EclipseProject */
    private void load() throws ProjectImporterException {
        InputStream projectIS = null;
        try {
            projectIS = new BufferedInputStream(
                    new FileInputStream(project.getProjectFile()));
            XMLReader reader = XMLUtil.createXMLReader(false, true);
            reader.setContentHandler(this);
            reader.setErrorHandler(this);
            chars = new StringBuffer(); // initialization
            reader.parse(new InputSource(projectIS)); // start parsing
        } catch (IOException e) {
            throw new ProjectImporterException(e);
        } catch (SAXException e) {
            throw new ProjectImporterException(e);
        } finally {
            if (projectIS != null) {
                try {
                    projectIS.close();
                } catch (IOException e) {
                    ErrorManager.getDefault().log(ErrorManager.WARNING,
                            "Unable to close projectInputStream: " + e);
                }
            }
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
                if (localName.equals(PROJECT_DESCRIPTION)) {
                    position = POSITION_PROJECT_DESCRIPTION;
                } else {
                    throw (new SAXException("First element has to be "
                            + PROJECT_DESCRIPTION + ", but is " + localName));
                }
                break;
            case POSITION_PROJECT_DESCRIPTION:
                if (localName.equals(NAME)) {
                    position = POSITION_PROJECT_NAME;
                } else if (localName.equals(LINKED_RESOURCES)) {
                    position = POSITION_LINKED_RESOURCES;
                } else if (localName.equals(NATURES)) {
                    position = POSITION_NATURES;
                } else {
                    position = POSITION_UNUSED;
                    unusedInner++;
                }
                break;
            case POSITION_NATURES:
                if (localName.equals(NATURE)) {
                    position = POSITION_NATURE;
                }
                break;
            case POSITION_LINKED_RESOURCES:
                if (localName.equals(LINK)) {
                    currentLink = new ClassPath.Link();
                    position = POSITION_LINK;
                }
                break;
            case POSITION_LINK:
                if (localName.equals(NAME)) {
                    position = POSITION_LINK_NAME;
                } else if (localName.equals(TYPE)) {
                    position = POSITION_LINK_TYPE;
                } else if (localName.equals(LOCATION)) {
                    position = POSITION_LINK_LOCATION;
                }
                break;
            default:
                position = POSITION_UNUSED;
                unusedInner++;
        }
    }
    
    public void endElement(String uri, String localName, String qName) throws
            SAXException {
        switch (position) {
            case POSITION_PROJECT_DESCRIPTION:
                // parsing ends
                position = POSITION_NONE;
            case POSITION_PROJECT_NAME:
                if (unusedInner == 0) {
                    if (localName.equals(NAME)) {
                        // Project names cannot have leading/trailing whitespace
                        // as they are IResource names.
                        project.setName(chars.toString().trim());
                        position = POSITION_PROJECT_DESCRIPTION;
                    }
                    break;
                }
            case POSITION_LINKED_RESOURCES:
            case POSITION_NATURES:
                position = POSITION_PROJECT_DESCRIPTION;
                break;
            case POSITION_NATURE:
                if (localName.equals(NATURE)) {
                    String nature = chars.toString().trim();
                    if (JAVA_NATURE.equals(nature)) {
                        project.setJavaNature(true);
                    } else {
                        project.addOtherNature(nature);
                    }
                }
                position = POSITION_NATURES;
                break;
            case POSITION_LINK:
                processLink(localName);
                break;
            case POSITION_LINK_NAME:
                processLinkName(localName);
                break;
            case POSITION_LINK_TYPE:
                processLinkType(localName);
                break;
            case POSITION_LINK_LOCATION:
                processLinkLocation(localName);
                break;
            case POSITION_UNUSED:
                if (--unusedInner == 0) {
                    position = POSITION_PROJECT_DESCRIPTION;
                }
                break;
            default:
                ErrorManager.getDefault().log(ErrorManager.WARNING,
                        "Unknown state reached in ProjectParser, " +
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
    
    /**
     * If a link is OK appends it to an <code>EclipseProject</code> links.
     */
    private void processLink(String elementName) throws SAXException {
        if (elementName.equals(LINK)) {
            position = POSITION_LINKED_RESOURCES;
            // Make sure that you have something reasonable
            String name = currentLink.getName();
            int type = currentLink.getType();
            String location = currentLink.getLocation();
            try {
                if ((name == null) || name.length() == 0) {
                    throw new SAXException(
                            "Link's name cannot be empty"); //NOI18N
                }
                if (type == ClassPath.Link.TYPE_INVALID) {
                    throw new SAXException(
                            "Link's type cannot be equal to " + type); //NOI18N
                }
                if ((location == null) || location.length() == 0) {
                    throw new SAXException(
                            "Link's location cannot be empty"); //NOI18N
                }
                project.addLink(currentLink);
            } finally {
                currentLink = null;
            }
        }
    }
    
    /** Sets location for currently processed link. */
    private void processLinkLocation(String elementName) throws SAXException {
        if (elementName.equals(LOCATION)) {
            String location = chars.toString().trim();
            if (currentLink.getLocation() != null) {
                throw new SAXException(
                        "Link's location was already set. There can be only " + //NOI18N
                        "one location element inside of link element"); //NOI18N
            }
            currentLink.setLocation(location);
            position = POSITION_LINK;
        }
    }
    
    /** Sets name for currently processed link. */
    private void processLinkName(String elementName) throws SAXException {
        if (elementName.equals(NAME)) {
            String name = chars.toString().trim();
            if (currentLink.getName() != null) {
                throw new SAXException(
                        "Link's name was already set. There can be only " + //NOI18N
                        "one name element inside of link element"); //NOI18N
            }
            currentLink.setName(name);
            position = POSITION_LINK;
        }
    }
    
    /** Sets type for currently processed link. */
    private void processLinkType(String elementName) throws SAXException {
        if (elementName.equals(TYPE)) {
            // make sure that type wasn't set yet
            if (currentLink.getType() != ClassPath.Link.TYPE_INVALID) {
                throw new SAXException(
                        "Link's type was already set. There can be only " + //NOI18N
                        "one type element inside of link element"); //NOI18N
            }
            int type;
            try {
                type = Integer.parseInt(chars.toString().trim());
            } catch (NumberFormatException e) {
                throw new SAXException("Link's type has to be a " + //NOI18N
                        "number but is: " + chars.toString().trim()); //NOI18N
            }
            currentLink.setType(type);
            position = POSITION_LINK;
        }
    }
    
}
