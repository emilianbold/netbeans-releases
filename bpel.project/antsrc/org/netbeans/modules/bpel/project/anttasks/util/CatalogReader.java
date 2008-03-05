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
package org.netbeans.modules.bpel.project.anttasks.util;

import java.io.FileReader;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import org.apache.xml.resolver.Catalog;
import org.apache.xml.resolver.CatalogManager;
import org.apache.xml.resolver.tools.CatalogResolver;
import org.apache.xml.resolver.tools.ResolvingXMLReader;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;


/**
 * This class helps Bpel project to Read the Catalog XML file
 * @author Sreenivasan Genipudi
 */
public class CatalogReader {
    private MyContentHandler mContentHandler = new MyContentHandler();

    /**
     * Constructor
     * @param catalogXML Location of Catalog XML
     * @throws Excepetion Exception during parsing the Catalog.xml file.
     */
    public CatalogReader(String catalogXML) throws Exception {
        CatalogResolver catalogResolver;
        Catalog apacheCatalogResolverObj;


        CatalogManager manager = new CatalogManager(null);
        manager.setUseStaticCatalog(false);
        manager.setPreferPublic(false);
        catalogResolver = new CatalogResolver(manager);
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

    @Override
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
    

    @Override
    public void endElement(String uri, String localName, String qName) {
    }

    @Override
    public void characters(char[] chars, int start, int length) {
    }


}
