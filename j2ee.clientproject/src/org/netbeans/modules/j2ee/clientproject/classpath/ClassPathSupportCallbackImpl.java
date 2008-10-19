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

package org.netbeans.modules.j2ee.clientproject.classpath;

import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.j2ee.clientproject.AppClientProjectType;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport;
import org.netbeans.modules.java.api.common.classpath.ClassPathSupport.Item;
import org.netbeans.modules.j2ee.common.project.ui.J2EEProjectProperties;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

/**
 *
 * @author Petr Hrebejk
 */
public class ClassPathSupportCallbackImpl implements ClassPathSupport.Callback {
     
    public final static String ELEMENT_INCLUDED_LIBRARIES = "included-library"; // NOI18N
    public static final String INCLUDE_IN_DEPLOYMENT = "includeInDeployment";
    
    private static final String ATTR_FILES = "files"; //NOI18N
    private static final String ATTR_DIRS = "dirs"; //NOI18N
    
    private AntProjectHelper antProjectHelper;
        
    /** Creates a new instance of ClassPathSupport */
    public  ClassPathSupportCallbackImpl(AntProjectHelper antProjectHelper) {
        this.antProjectHelper = antProjectHelper;
    }
    
    
    /** 
     * Returns a list with the classpath items which are to be included 
     * in deployment.
     */
    private static List<String> getIncludedLibraries( AntProjectHelper antProjectHelper, String includedLibrariesElement ) {
        assert antProjectHelper != null;
        assert includedLibrariesElement != null;
        
        Element data = antProjectHelper.getPrimaryConfigurationData( true );
        NodeList libs = data.getElementsByTagNameNS( AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, includedLibrariesElement );
        List<String> libraries = new ArrayList<String>(libs.getLength());
        for ( int i = 0; i < libs.getLength(); i++ ) {
            Element item = (Element)libs.item( i );
            // appclient is different from other j2ee projects - it stores reference without ${ and }
            libraries.add( "${"+findText( item )+"}"); // NOI18N
        }
        return libraries;
    }
    
    /**
     * Updates the project helper with the list of classpath items which are to be
     * included in deployment.
     */
    private static void putIncludedLibraries(List<Item> classpath,
            AntProjectHelper antProjectHelper, String includedLibrariesElement) {
        assert antProjectHelper != null;
        assert includedLibrariesElement != null;
        
        Element data = antProjectHelper.getPrimaryConfigurationData( true );
        NodeList libs = data.getElementsByTagNameNS( AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, includedLibrariesElement );
        while ( libs.getLength() > 0 ) {
            Node n = libs.item( 0 );
            n.getParentNode().removeChild( n );
        }

        Document doc = data.getOwnerDocument();
        //find a correcponding classpath item for the library
        for (ClassPathSupport.Item item : classpath) {
            if("true".equals(item.getAdditionalProperty(INCLUDE_IN_DEPLOYMENT))) { // NOI18N
                data.appendChild(createLibraryElement(antProjectHelper, doc, item, 
                        includedLibrariesElement));
            }
        }
        
        antProjectHelper.putPrimaryConfigurationData( data, true );
    }
    
    private static Element createLibraryElement(AntProjectHelper antProjectHelper, Document doc, Item item, 
            String includedLibrariesElement) {
        Element libraryElement = doc.createElementNS( AppClientProjectType.PROJECT_CONFIGURATION_NAMESPACE, includedLibrariesElement );
        // appclient is different from other j2ee projects - it stores reference without ${ and }
        libraryElement.appendChild( doc.createTextNode( CommonProjectUtils.getAntPropertyName(item.getReference()) ) );
        return libraryElement;
    }
       
    /**
     * Extracts <b>the first</b> nested text from an element.
     * Currently does not handle coalescing text nodes, CDATA sections, etc.
     * @param parent a parent element
     * @return the nested text, or null if none was found
     */
    private static String findText( Element parent ) {
        NodeList l = parent.getChildNodes();
        for ( int i = 0; i < l.getLength(); i++ ) {
            if ( l.item(i).getNodeType() == Node.TEXT_NODE ) {
                Text text = (Text)l.item( i );
                return text.getNodeValue();
            }
        }
        return null;
    }

    public void readAdditionalProperties(List<Item> items, String projectXMLElement) {
        List<String> l = getIncludedLibraries(antProjectHelper, projectXMLElement);
        for (Item item : items) {
            item.setAdditionalProperty(INCLUDE_IN_DEPLOYMENT, Boolean.toString(l.contains(item.getReference())));
        }
    }

    public void storeAdditionalProperties(List<Item> items, String projectXMLElement) {
        putIncludedLibraries(items, antProjectHelper, projectXMLElement);
    }

}
