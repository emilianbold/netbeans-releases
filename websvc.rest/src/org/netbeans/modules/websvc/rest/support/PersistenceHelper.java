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

package org.netbeans.modules.websvc.rest.support;

import java.io.IOException;
import java.util.Properties;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.common.api.Datasource;
import org.netbeans.modules.websvc.rest.RestUtils;
import org.netbeans.modules.websvc.rest.spi.RestSupport;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 *
 * @author PeterLiu
 */
public class PersistenceHelper {
    private static final String PERSISTENCE_UNIT_TAG = "persistence-unit";      //NOI18N
    private static final String PROPERTIES_TAG = "properties";      //NOI18N
    private static final String NAME_ATTR = "name";                 //NOI18N
    private static final String EXCLUDE_UNLISTED_CLASSES_TAG = "exclude-unlisted-classes";      //NOI18N
    private static final String TRANSACTION_TYPE_ATTR = "transaction-type";         //NOI18N
    private static final String RESOURCE_LOCAL_VALUE = "RESOURCE_LOCAL";        //NOI18N
    private static final String JTA_DATA_SOURCE_TAG = "jta-data-source";        //NOI18N
    private static final String NON_JTA_DATA_SOURCE_TAG = "non-jta-data-source";        //NOI18N

   
    public static PersistenceUnit getPersistenceUnit(Project project) {
        FileObject fobj = getPersistenceXML(project);
        
        if (fobj != null) {
            DOMHelper helper = new DOMHelper(fobj);
        
            Element puElement = helper.findElement(PERSISTENCE_UNIT_TAG);  
            
            if (puElement != null) {
                String puName = puElement.getAttribute(NAME_ATTR);
                Datasource datasource = null;
                
                NodeList nodeList = puElement.getElementsByTagName(JTA_DATA_SOURCE_TAG);
                if (nodeList.getLength() > 0) {
                    Element dsElement = (Element) nodeList.item(0);
                    String jndiName = helper.getValue(dsElement);      
                    datasource = RestUtils.getDatasource(project, jndiName);
                }
                
                return new PersistenceUnit(puName, datasource);
            }
        }
        
        return null;
    }
    
    
    public static void modifyPersistenceXml(Project project, boolean useResourceLocalTx) throws IOException {
        FileObject fobj = getPersistenceXML(project);
        DOMHelper helper = new DOMHelper(fobj);
       
        // Need to do this for Tomcat
        unsetExcludeEnlistedClasses(helper);
        
        if (useResourceLocalTx)
            switchToResourceLocalTransaction(helper);
       
        helper.save();
    }
    
    public static void unsetExcludeEnlistedClasses(Project project) throws IOException{
        FileObject fobj = getPersistenceXML(project);
        unsetExcludeEnlistedClasses(new DOMHelper(fobj));
    }
    
    private static void unsetExcludeEnlistedClasses(DOMHelper helper) throws IOException {
        Element puElement = helper.findElement(PERSISTENCE_UNIT_TAG);
        NodeList nodes = puElement.getElementsByTagName(EXCLUDE_UNLISTED_CLASSES_TAG);
    
        if (nodes.getLength() > 0) {
            helper.setValue((Element) nodes.item(0), "false");  //NOI18N
        } else {
            puElement.insertBefore(helper.createElement(EXCLUDE_UNLISTED_CLASSES_TAG, "false"),  //NOI18N
                    helper.findElement(PROPERTIES_TAG));
        }
    }
     
    private static void switchToResourceLocalTransaction(DOMHelper helper)  throws IOException {
        Element puElement = helper.findElement(PERSISTENCE_UNIT_TAG);
        puElement.setAttribute(TRANSACTION_TYPE_ATTR, RESOURCE_LOCAL_VALUE);
        
        NodeList nodes = puElement.getElementsByTagName(JTA_DATA_SOURCE_TAG);
        String dataSource = null;
        
        if (nodes.getLength() > 0) {
            Element oldElement = (Element) nodes.item(0);
            dataSource = helper.getValue(oldElement);
            Element newElement = helper.createElement(NON_JTA_DATA_SOURCE_TAG, dataSource);
            puElement.replaceChild(newElement, oldElement);
        }
    }
    
   
    public static Element getPropertiesElement(Document document) {
       NodeList nodeList = document.getElementsByTagName(PROPERTIES_TAG);
        
        if (nodeList.getLength() > 0) {
            return (Element) nodeList.item(0);
        }
        
        return null;
    }      
    
    private static FileObject getPersistenceXML(Project project) {
        RestSupport rs = RestUtils.getRestSupport(project);
        if (rs != null) {
            return rs.getPersistenceXml();
        }
        return null;
    }
    
    public static class PersistenceUnit {
        private String name;
        private Datasource datasource;
        
        public PersistenceUnit(String name, Datasource datasource) {
            this.name = name;
            this.datasource = datasource;
        }
        
        public String getName() {
            return name;
        }
        
        public Datasource getDatasource() {
            return datasource;
        }
    }
}
