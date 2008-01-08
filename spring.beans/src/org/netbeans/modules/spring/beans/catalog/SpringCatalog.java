/*
 * Copyright 2007 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.netbeans.modules.spring.beans.catalog;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.xml.catalog.spi.CatalogDescriptor;
import org.netbeans.modules.xml.catalog.spi.CatalogListener;
import org.netbeans.modules.xml.catalog.spi.CatalogReader;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Catalog for Spring Framework Schema files
 * 
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public class SpringCatalog implements CatalogReader, CatalogDescriptor, EntityResolver {

    private static final String SPRING_AOP_2_0_XSD = "spring-aop-2.0.xsd";
    private static final String SPRING_AOP_2_0 = "http://www.springframework.org/schema/aop/spring-aop-2.0.xsd";
    private static final String SPRING_AOP_2_0_URL = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-aop-2.0.xsd";
    private static final String SPRING_AOP_2_0_ID = "SCHEMA:" + SPRING_AOP_2_0;
    
    private static final String SPRING_AOP_2_1_XSD = "spring-aop-2.1.xsd";
    private static final String SPRING_AOP_2_1 = "http://www.springframework.org/schema/aop/spring-aop-2.1.xsd";
    private static final String SPRING_AOP_2_1_URL = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-aop-2.1.xsd";
    private static final String SPRING_AOP_2_1_ID = "SCHEMA:" + SPRING_AOP_2_1;

    private static final String SPRING_AOP_2_5_XSD = "spring-aop-2.5.xsd";
    private static final String SPRING_AOP_2_5 = "http://www.springframework.org/schema/aop/spring-aop-2.5.xsd";
    private static final String SPRING_AOP_2_5_URL = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-aop-2.5.xsd";
    private static final String SPRING_AOP_2_5_ID = "SCHEMA:" + SPRING_AOP_2_5;
    
    private static final String SPRING_BEANS_2_0_XSD = "spring-beans-2.0.xsd";
    private static final String SPRING_BEANS_2_0 = "http://www.springframework.org/schema/beans/spring-beans-2.0.xsd";
    private static final String SPRING_BEANS_2_0_URL = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-beans-2.0.xsd";
    private static final String SPRING_BEANS_2_0_ID = "SCHEMA:" + SPRING_BEANS_2_0;
    
    private static final String SPRING_BEANS_2_5_XSD = "spring-beans-2.5.xsd";
    private static final String SPRING_BEANS_2_5 = "http://www.springframework.org/schema/beans/spring-beans-2.5.xsd";
    private static final String SPRING_BEANS_2_5_URL = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-beans-2.5.xsd";
    private static final String SPRING_BEANS_2_5_ID = "SCHEMA:" + SPRING_BEANS_2_5;
    
    private static final String SPRING_CONTEXT_2_5_XSD = "spring-context-2.5.xsd";
    private static final String SPRING_CONTEXT_2_5 = "http://www.springframework.org/schema/context/spring-context-2.5.xsd";
    private static final String SPRING_CONTEXT_2_5_URL = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-context-2.5.xsd";
    private static final String SPRING_CONTEXT_2_5_ID = "SCHEMA:" + SPRING_CONTEXT_2_5;
    
    private static final String SPRING_JEE_2_0_XSD = "spring-jee-2.0.xsd";
    private static final String SPRING_JEE_2_0 = "http://www.springframework.org/schema/jee/spring-jee-2.0.xsd";
    private static final String SPRING_JEE_2_0_URL = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-jee-2.0.xsd";
    private static final String SPRING_JEE_2_0_ID = "SCHEMA:" + SPRING_JEE_2_0;
    
    private static final String SPRING_JEE_2_5_XSD = "spring-jee-2.5.xsd";
    private static final String SPRING_JEE_2_5 = "http://www.springframework.org/schema/jee/spring-jee-2.5.xsd";
    private static final String SPRING_JEE_2_5_URL = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-jee-2.5.xsd";
    private static final String SPRING_JEE_2_5_ID = "SCHEMA:" + SPRING_JEE_2_5;
    
    private static final String SPRING_JMS_2_5_XSD = "spring-jms-2.5.xsd";
    private static final String SPRING_JMS_2_5 = "http://www.springframework.org/schema/jms/spring-jms-2.5.xsd";
    private static final String SPRING_JMS_2_5_URL = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-jms-2.5.xsd";
    private static final String SPRING_JMS_2_5_ID = "SCHEMA:" + SPRING_JMS_2_5;
    
    private static final String SPRING_LANG_2_0_XSD = "spring-lang-2.0.xsd";
    private static final String SPRING_LANG_2_0 = "http://www.springframework.org/schema/lang/spring-lang-2.0.xsd";
    private static final String SPRING_LANG_2_0_URL = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-lang-2.0.xsd";
    private static final String SPRING_LANG_2_0_ID = "SCHEMA:" + SPRING_LANG_2_0;
    
    private static final String SPRING_LANG_2_5_XSD = "spring-lang-2.5.xsd";
    private static final String SPRING_LANG_2_5 = "http://www.springframework.org/schema/lang/spring-lang-2.5.xsd";
    private static final String SPRING_LANG_2_5_URL = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-lang-2.5.xsd";
    private static final String SPRING_LANG_2_5_ID = "SCHEMA:" + SPRING_LANG_2_5;
    
    private static final String SPRING_OSGI_XSD = "spring-osgi.xsd";
    private static final String SPRING_OSGI = "http://www.springframework.org/schema/osgi/spring-osgi.xsd";
    private static final String SPRING_OSGI_URL = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-osgi.xsd";
    private static final String SPRING_OSGI_ID = "SCHEMA:" + SPRING_OSGI;
    
    private static final String SPRING_TOOL_2_0_XSD = "spring-tool-2.0.xsd";
    private static final String SPRING_TOOL_2_0 = "http://www.springframework.org/schema/lang/spring-tool-2.0.xsd";
    private static final String SPRING_TOOL_2_0_URL = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-tool-2.0.xsd";
    private static final String SPRING_TOOL_2_0_ID = "SCHEMA:" + SPRING_TOOL_2_0;
    
    private static final String SPRING_TOOL_2_5_XSD = "spring-tool-2.5.xsd";
    private static final String SPRING_TOOL_2_5 = "http://www.springframework.org/schema/lang/spring-tool-2.5.xsd";
    private static final String SPRING_TOOL_2_5_URL = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-tool-2.5.xsd";
    private static final String SPRING_TOOL_2_5_ID = "SCHEMA:" + SPRING_TOOL_2_5;

    private static final String SPRING_TX_2_0_XSD = "spring-tx-2.0.xsd";
    private static final String SPRING_TX_2_0 = "http://www.springframework.org/schema/tx/spring-tx-2.0.xsd";
    private static final String SPRING_TX_2_0_URL = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-tx-2.0.xsd";
    private static final String SPRING_TX_2_0_ID = "SCHEMA:" + SPRING_TX_2_0;
    
    private static final String SPRING_TX_2_1_XSD = "spring-tx-2.1.xsd";
    private static final String SPRING_TX_2_1 = "http://www.springframework.org/schema/tx/spring-tx-2.1.xsd";
    private static final String SPRING_TX_2_1_URL = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-tx-2.1.xsd";
    private static final String SPRING_TX_2_1_ID = "SCHEMA:" + SPRING_TX_2_1;
    
    private static final String SPRING_TX_2_5_XSD = "spring-tx-2.5.xsd";
    private static final String SPRING_TX_2_5 = "http://www.springframework.org/schema/tx/spring-tx-2.5.xsd";
    private static final String SPRING_TX_2_5_URL = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-tx-2.5.xsd";
    private static final String SPRING_TX_2_5_ID = "SCHEMA:" + SPRING_TX_2_5;
    
    private static final String SPRING_UTIL_2_0_XSD = "spring-util-2.0.xsd";
    private static final String SPRING_UTIL_2_0 = "http://www.springframework.org/schema/util/spring-util-2.0.xsd";
    private static final String SPRING_UTIL_2_0_URL = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-util-2.0.xsd";
    private static final String SPRING_UTIL_2_0_ID = "SCHEMA:" + SPRING_UTIL_2_0;
    
    private static final String SPRING_UTIL_2_5_XSD = "spring-util-2.5.xsd";
    private static final String SPRING_UTIL_2_5 = "http://www.springframework.org/schema/util/spring-util-2.5.xsd";
    private static final String SPRING_UTIL_2_5_URL = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-util-2.5.xsd";
    private static final String SPRING_UTIL_2_5_ID = "SCHEMA:" + SPRING_UTIL_2_5;
    
    private static final String SPRING_WEBFLOW_CONFIG_1_0_XSD = "spring-webflow-config-1.0.xsd";
    private static final String SPRING_WEBFLOW_CONFIG_1_0 = "http://www.springframework.org/schema/webflow-config/spring-webflow-config-1.0.xsd";
    private static final String SPRING_WEBFLOW_CONFIG_1_0_URL = "nbres:/org/netbeans/modules/spring/beans/catalog/resources/spring-webflow-config-1.0.xsd";
    private static final String SPRING_WEBFLOW_CONFIG_1_0_ID = "SCHEMA:" + SPRING_WEBFLOW_CONFIG_1_0;
    
    public Iterator getPublicIDs() {
        List<String> list = new ArrayList<String>();
        list.add(SPRING_AOP_2_0_ID);
        list.add(SPRING_AOP_2_1_ID);
        list.add(SPRING_AOP_2_5_ID);
        list.add(SPRING_BEANS_2_0_ID);
        list.add(SPRING_BEANS_2_5_ID);
        list.add(SPRING_CONTEXT_2_5_ID);
        list.add(SPRING_JEE_2_0_ID);
        list.add(SPRING_JEE_2_5_ID);
        list.add(SPRING_JMS_2_5_ID);
        list.add(SPRING_LANG_2_0_ID);
        list.add(SPRING_LANG_2_5_ID);
        list.add(SPRING_OSGI_ID);
        list.add(SPRING_TOOL_2_0_ID);
        list.add(SPRING_TOOL_2_5_ID);
        list.add(SPRING_TX_2_0_ID);
        list.add(SPRING_TX_2_1_ID);
        list.add(SPRING_TX_2_5_ID);
        list.add(SPRING_UTIL_2_0_ID);
        list.add(SPRING_UTIL_2_5_ID);
        list.add(SPRING_WEBFLOW_CONFIG_1_0_ID);
        
        return list.listIterator();
    }

    public void refresh() {
        
    }

    public String getSystemID(String publicId) {
        if(publicId.equals(SPRING_AOP_2_0_ID)) {
            return SPRING_AOP_2_0_URL;
        } else if(publicId.equals(SPRING_AOP_2_1_ID)) {
            return SPRING_AOP_2_1_URL;
        } else if(publicId.equals(SPRING_AOP_2_5_ID)) {
            return SPRING_AOP_2_5_URL;
        } else if(publicId.equals(SPRING_BEANS_2_0_ID)) {
            return SPRING_BEANS_2_0_URL;
        } else if(publicId.equals(SPRING_BEANS_2_5_ID)) {
            return SPRING_BEANS_2_5_URL;
        } else if(publicId.equals(SPRING_CONTEXT_2_5_ID)) {
            return SPRING_CONTEXT_2_5_URL;
        } else if(publicId.equals(SPRING_JEE_2_0_ID)) {
            return SPRING_JEE_2_0_URL;
        } else if(publicId.equals(SPRING_JEE_2_5_ID)) {
            return SPRING_JEE_2_5_URL;
        } else if(publicId.equals(SPRING_JMS_2_5_ID)) {
            return SPRING_JMS_2_5_URL;
        } else if(publicId.equals(SPRING_LANG_2_0_ID)) {
            return SPRING_LANG_2_0_URL;
        } else if(publicId.equals(SPRING_LANG_2_5_ID)) {
            return SPRING_LANG_2_5_URL;
        } else if(publicId.equals(SPRING_OSGI_ID)) {
            return SPRING_OSGI_URL;
        } else if(publicId.equals(SPRING_TOOL_2_0_ID)) {
            return SPRING_TOOL_2_0_URL;
        } else if(publicId.equals(SPRING_TOOL_2_5_ID)) {
            return SPRING_TOOL_2_5_URL;
        } else if(publicId.equals(SPRING_TX_2_0_ID)) {
            return SPRING_TX_2_0_URL;
        } else if(publicId.equals(SPRING_TX_2_1_ID)) {
            return SPRING_TX_2_1_URL;
        } else if(publicId.equals(SPRING_TX_2_5_ID)) {
            return SPRING_TX_2_5_URL;
        } else if(publicId.equals(SPRING_UTIL_2_0_ID)) {
            return SPRING_UTIL_2_0_URL;
        } else if(publicId.equals(SPRING_UTIL_2_5_ID)) {
            return SPRING_UTIL_2_5_URL;
        } else if(publicId.equals(SPRING_WEBFLOW_CONFIG_1_0_ID)) {
            return SPRING_WEBFLOW_CONFIG_1_0_URL;
        } 
        
        return null;
    }

    public String resolveURI(String arg0) {
        return null;
    }

    public String resolvePublic(String arg0) {
        return null;
    }

    public void addCatalogListener(CatalogListener catalogListener) {
        
    }

    public void removeCatalogListener(CatalogListener catalogListener) {
        
    }

    public Image getIcon(int i) {
        return Utilities.loadImage("org/netbeans/modules/spring/beans/resources/spring.png");
    }

    public String getDisplayName() {
        return NbBundle.getMessage(SpringCatalog.class, "LBL_SpringCatalog");
    }

    public String getShortDescription() {
        return NbBundle.getMessage(SpringCatalog.class, "DESC_SpringCatalog");
    }

    public void addPropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        
    }

    public void removePropertyChangeListener(PropertyChangeListener propertyChangeListener) {
        
    }

    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if (SPRING_AOP_2_0.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_AOP_2_0_URL);
        }
        if (systemId != null && systemId.endsWith(SPRING_AOP_2_0_XSD)){
            return new org.xml.sax.InputSource(SPRING_AOP_2_0_URL);
        }
        
        if (SPRING_AOP_2_1.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_AOP_2_1_URL);
        }
        if (systemId != null && systemId.endsWith(SPRING_AOP_2_1_XSD)){
            return new org.xml.sax.InputSource(SPRING_AOP_2_1_URL);
        }
        
        if (SPRING_AOP_2_5.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_AOP_2_5_URL);
        }
        if (systemId != null && systemId.endsWith(SPRING_AOP_2_5_XSD)){
            return new org.xml.sax.InputSource(SPRING_AOP_2_5_URL);
        }
        
        if (SPRING_BEANS_2_0.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_BEANS_2_0_URL);
        }
        if (systemId != null && systemId.endsWith(SPRING_BEANS_2_0_XSD)){
            return new org.xml.sax.InputSource(SPRING_BEANS_2_0_URL);
        }
        
        if (SPRING_BEANS_2_5.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_BEANS_2_5_URL);
        }
        if (systemId != null && systemId.endsWith(SPRING_BEANS_2_5_XSD)){
            return new org.xml.sax.InputSource(SPRING_BEANS_2_5_URL);
        }
        
        if (SPRING_CONTEXT_2_5.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_CONTEXT_2_5_URL);
        }
        if (systemId != null && systemId.endsWith(SPRING_CONTEXT_2_5_XSD)){
            return new org.xml.sax.InputSource(SPRING_CONTEXT_2_5_URL);
        }
        
        if (SPRING_JEE_2_0.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_JEE_2_0_URL);
        }
        if (systemId != null && systemId.endsWith(SPRING_JEE_2_0_XSD)){
            return new org.xml.sax.InputSource(SPRING_JEE_2_0_URL);
        }

        if (SPRING_JEE_2_5.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_JEE_2_5_URL);
        }
        if (systemId != null && systemId.endsWith(SPRING_JEE_2_5_XSD)){
            return new org.xml.sax.InputSource(SPRING_JEE_2_5_URL);
        }
        
        if (SPRING_JMS_2_5.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_JMS_2_5_URL);
        }
        if (systemId != null && systemId.endsWith(SPRING_JMS_2_5_XSD)){
            return new org.xml.sax.InputSource(SPRING_JMS_2_5_URL);
        }
        
        if (SPRING_LANG_2_0.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_LANG_2_0_URL);
        }
        if (systemId != null && systemId.endsWith(SPRING_LANG_2_0_XSD)){
            return new org.xml.sax.InputSource(SPRING_LANG_2_0_URL);
        }
        
        if (SPRING_LANG_2_5.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_LANG_2_5_URL);
        }
        if (systemId != null && systemId.endsWith(SPRING_LANG_2_5_XSD)){
            return new org.xml.sax.InputSource(SPRING_LANG_2_5_URL);
        }
        
        if (SPRING_OSGI.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_OSGI_URL);
        }
        if (systemId != null && systemId.endsWith(SPRING_OSGI_XSD)){
            return new org.xml.sax.InputSource(SPRING_OSGI_URL);
        }
        
        if (SPRING_TOOL_2_0.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_TOOL_2_0_URL);
        }
        if (systemId != null && systemId.endsWith(SPRING_TOOL_2_0_XSD)){
            return new org.xml.sax.InputSource(SPRING_TOOL_2_0_URL);
        }
        
        if (SPRING_TOOL_2_5.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_TOOL_2_5_URL);
        }
        if (systemId != null && systemId.endsWith(SPRING_TOOL_2_5_XSD)){
            return new org.xml.sax.InputSource(SPRING_TOOL_2_5_URL);
        }
        
        if (SPRING_TX_2_0.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_TX_2_0_URL);
        }
        if (systemId != null && systemId.endsWith(SPRING_TX_2_0_XSD)){
            return new org.xml.sax.InputSource(SPRING_TX_2_0_URL);
        }
        
        if (SPRING_TX_2_1.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_TX_2_1_URL);
        }
        if (systemId != null && systemId.endsWith(SPRING_TX_2_1_XSD)){
            return new org.xml.sax.InputSource(SPRING_TX_2_1_URL);
        }
        
        if (SPRING_TX_2_5.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_TX_2_5_URL);
        }
        if (systemId != null && systemId.endsWith(SPRING_TX_2_5_XSD)){
            return new org.xml.sax.InputSource(SPRING_TX_2_5_URL);
        }
        
        if (SPRING_UTIL_2_0.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_UTIL_2_0_URL);
        }
        if (systemId != null && systemId.endsWith(SPRING_UTIL_2_0_XSD)){
            return new org.xml.sax.InputSource(SPRING_UTIL_2_0_URL);
        }
        
        if (SPRING_UTIL_2_5.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_UTIL_2_5_URL);
        }
        if (systemId != null && systemId.endsWith(SPRING_UTIL_2_5_XSD)){
            return new org.xml.sax.InputSource(SPRING_UTIL_2_5_URL);
        }
        
        if (SPRING_WEBFLOW_CONFIG_1_0.equals(systemId)){
            return new org.xml.sax.InputSource(SPRING_WEBFLOW_CONFIG_1_0_URL);
        }
        if (systemId != null && systemId.endsWith(SPRING_WEBFLOW_CONFIG_1_0_XSD)){
            return new org.xml.sax.InputSource(SPRING_WEBFLOW_CONFIG_1_0_URL);
        }
        
        return null;
    }

}
