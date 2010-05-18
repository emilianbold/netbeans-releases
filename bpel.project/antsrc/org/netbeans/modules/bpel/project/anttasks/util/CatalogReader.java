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
 * This class helps Bpel project to Read the Catalog XML file
 * 
 * @author Sreenivasan Genipudi
 * @author Kirill Sorokin
 */
public class CatalogReader {
    
    private MyContentHandler mContentHandler = new MyContentHandler();
    private Stack<File> nextCatalogs = new Stack<File>();
    private File rootCatalog;
    private File currentCatalog;
    
    private List<String> systemIds = new ArrayList<String>();
    private List<String> locations = new ArrayList<String>();
    
    public CatalogReader(String catalogXML) throws SAXException, IOException {
//System.out.println("        1");
        CatalogManager manager = new CatalogManager("#171444/");
//System.out.println("        1.1");
        manager.setUseStaticCatalog(false);
        manager.setPreferPublic(false);
        manager.setIgnoreMissingProperties(true);
//System.out.println("        2: " + catalogXML);
        
        ResolvingXMLReader saxParser = new ResolvingXMLReader(manager);
        saxParser.setContentHandler(mContentHandler);
///System.out.println("        3");
        rootCatalog = new File(catalogXML);
        nextCatalogs.push(rootCatalog);
//System.out.println("        4");
        do {
            currentCatalog = nextCatalogs.pop();

            if (currentCatalog.exists() && (currentCatalog.length() > 0)) {
                saxParser.parse(new InputSource(new FileReader(currentCatalog)));
            }
        }
        while (nextCatalogs.size() > 0);
//System.out.println("        5");
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
