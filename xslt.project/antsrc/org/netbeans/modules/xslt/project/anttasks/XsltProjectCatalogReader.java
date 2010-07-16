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
package org.netbeans.modules.xslt.project.anttasks;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.ResolvingXMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class helps Xslt project to Read the Catalog XML file
 * @author Sreenivasan Genipudi
 */
public class XsltProjectCatalogReader {
    private MyContentHandler mContentHandler = new MyContentHandler();
    private Stack<File> nextCatalogs = new Stack<File>();
    private File rootCatalog;
    private File currentCatalog;

    private List<String> systemIds = new ArrayList<String>();
    private List<String> locations = new ArrayList<String>();

    /**
     * Constructor
     * @param catalogXML Location of Catalog XML
     * @throws Excepetion Exception during parsing the Catalog.xml file.
     */
    public XsltProjectCatalogReader(String catalogXML) throws SAXException, IOException {
        final CatalogManager manager = new CatalogManager(null);
        manager.setUseStaticCatalog(false);
        manager.setPreferPublic(false);

        final ResolvingXMLReader saxParser = new ResolvingXMLReader(manager);
        saxParser.setContentHandler(mContentHandler);

        rootCatalog = new File(catalogXML);
        nextCatalogs.push(rootCatalog);

        do {
            currentCatalog = nextCatalogs.pop();
            if (currentCatalog.exists() && (currentCatalog.length() > 0)) {
                saxParser.parse(new InputSource(new FileReader(currentCatalog)));
            }
        } while (nextCatalogs.size() > 0);
    }

    public List<String> getSystemIds() {
        return systemIds;
    }

    public List<String> getLocations() {
        return locations;
    }

    private class MyContentHandler extends DefaultHandler {

        private static final String SYSTEM_CONST = "system";
        private static final String SYSTEM_ID_CONST = "systemId";
        private static final String URI_CONST = "uri";
        private static final String NEXT_CATALOG_CONST = "nextCatalog";
        private static final String CATALOG_CONST = "catalog";

        boolean isSystem = false;

        @Override
        public void startElement(
                final String uri,
                final String localName,
                final String qName,
                final Attributes atts) {

            if (qName.equals(SYSTEM_CONST)) {
                final String systemId = atts.getValue(SYSTEM_ID_CONST);
                String location = atts.getValue(URI_CONST);

                if ((systemId != null) && !systemIds.contains(systemId)) {
                    if (currentCatalog != rootCatalog) {
                        location = Util.getRelativePath(rootCatalog.getParentFile(),
                                currentCatalog.getParentFile()) + "/" + location;
                    }

                    systemIds.add(systemId);
                    locations.add(location.replace("\\", "/"));
                }
            }

            if (qName.equals(NEXT_CATALOG_CONST)) {
                final String catalog = atts.getValue(CATALOG_CONST);

                nextCatalogs.push(new File(currentCatalog.getParentFile(), catalog));
            }
        }
    }

}
