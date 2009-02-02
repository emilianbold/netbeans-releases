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

import java.io.FileReader;

import java.net.MalformedURLException;
import java.util.ArrayList;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.NbCatalogManager;
import org.apache.xml.resolver.tools.NbCatalogResolver;
import org.apache.xml.resolver.tools.ResolvingXMLReader;

import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

/**
 * This class helps Xslt project to Read the Catalog XML file
 * @author Sreenivasan Genipudi
 */
public class XsltProjectCatalogReader {
    private MyContentHandler mContentHandler = new MyContentHandler();

    /**
     * Constructor
     * @param catalogXML Location of Catalog XML
     * @throws Excepetion Exception during parsing the Catalog.xml file.
     */
    public XsltProjectCatalogReader(String catalogXML) throws Exception {
        NbCatalogResolver catalogResolver;
        Catalog apacheCatalogResolverObj;


        NbCatalogManager manager = new NbCatalogManager(null);
        manager.setUseStaticCatalog(false);
        manager.setPreferPublic(false);
        catalogResolver = new NbCatalogResolver(manager);
        apacheCatalogResolverObj = catalogResolver.getCatalog();
        try {
            apacheCatalogResolverObj.parseCatalog(catalogXML);
        } catch (MalformedURLException ex) {
            throw new RuntimeException(ex);
        }
        ResolvingXMLReader saxParser = new ResolvingXMLReader(manager);
        saxParser.setContentHandler(mContentHandler);
        FileReader finRead = 
            new FileReader(catalogXML);
        InputSource isource = new InputSource(finRead);
        saxParser.parse(isource);
    }

    /**
     * Get the list of URI's listed in Catalog.xml
     * @return Set of URIs listed in Catalog.xml
     */
    public ArrayList<String> getListOfLocalURIs() {
        return mContentHandler.listOfURI;
    }

    
    public int locateNS(String ns) {
        if (mContentHandler.listOfNS.size() <= 0) {
            return -1;
        }
        int in =0;
        for (String myNS: mContentHandler.listOfNS) {
            if (myNS.equals(ns)) {
                return in;
            }
            in++;
        }
        return -1;
    }

    /**
     * Get the Set of Namespaces listed in Catalog.xml
     * @return Set of Namespaces listed in Catalog.xml
     */
    public ArrayList<String> getListOfNamespaces() {
        return mContentHandler.listOfNS;
    }


}
class MyContentHandler extends DefaultHandler {
    private static final String SYSTEM_CONST = "system";
    private static final String SYSTEM_ID_CONST = "systemId";
    private static final String URI_CONST = "uri";
    boolean isSystem = false;
    ArrayList<String> listOfURI = new ArrayList<String>();
    ArrayList<String> listOfNS = new ArrayList<String>();

    public List getURIs() {
        return listOfURI;
    }

    public List getNSs() {
        return listOfNS;
    }

    public void startElement(String uri, String localName, String qName, 
                             Attributes atts) {
        if (qName.equals(SYSTEM_CONST)) {
            String nameSpace = atts.getValue(SYSTEM_ID_CONST);
            String location = atts.getValue(URI_CONST);
            if (nameSpace != null) {
                listOfNS.add(nameSpace);
            }
            if (location != null) {
                listOfURI.add(location);
            }
        }

    }
    

    public void endElement(String uri, String localName, String qName) {
    }

    public void characters(char[] chars, int start, int length) {
    }


}
