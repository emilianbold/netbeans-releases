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
package org.netbeans.modules.identity.profile.api.configurator.impl.file;

import junit.framework.*;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.Unmarshaller;
import org.netbeans.modules.identity.profile.api.configurator.ServerProperties;
import org.netbeans.modules.identity.profile.api.configurator.impl.file.jaxb.*;
import org.netbeans.modules.identity.profile.api.configurator.SecurityMechanism;
/*
 * ProviderConfigImplTest.java
 * JUnit based test
 *
 * Created on July 10, 2006, 6:39 PM
 */

/**
 *
 * @author Srividhya Narayanan
 */
public class ProviderConfigImplTest extends TestCase {
    String path = null;
    
    public ProviderConfigImplTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        URL p = ProviderConfigImplTest.class.getResource("resources/amconfig.xml");
        path = p.getPath();
        int end = path.indexOf("resources/amconfig.xml");
        path = path.substring(0, end);
    }

    protected void tearDown() throws Exception {
    }

    /**
     * Test of setResponseSignEnabled method, of class org.netbeans.modules.identity.profile.api.configurator.impl.file.ProviderConfigImpl.
     */
    public void testSetResponseSignEnabled() {
        System.out.println("setResponseSignEnabled");
        
        boolean expResult = true;
        ServerProperties prop = new ServerProperties();
        prop.setProperty(ServerProperties.PROP_ID, "server1");
        org.netbeans.modules.identity.profile.api.configurator.impl.file.ProviderConfigImpl instance =
                new ProviderConfigImpl("testprovider", "WSC", prop, path);
        
        instance.setResponseSignEnabled(expResult);
        instance.saveProvider();
        
        org.netbeans.modules.identity.profile.api.configurator.impl.file.ProviderConfigImpl instance1 =
                new ProviderConfigImpl("testprovider", "WSC", prop, path);
        
        boolean result = instance1.isResponseSignEnabled();
        assertEquals(expResult, result);
    }

    /**
     * Test of isResponseSignEnabled method, of class org.netbeans.modules.identity.profile.api.configurator.impl.file.ProviderConfigImpl.
     */
    public void testIsResponseSignEnabled() {
        System.out.println("isResponseSignEnabled");
        
        ServerProperties prop = new ServerProperties();
        prop.setProperty(ServerProperties.PROP_ID, "server1");
        org.netbeans.modules.identity.profile.api.configurator.impl.file.ProviderConfigImpl instance = 
                new ProviderConfigImpl("testprovider", "WSC", prop, path);
        
        boolean expResult = true;
        boolean result = instance.isResponseSignEnabled();
        assertEquals(expResult, result);
    }

    /**
     * Test of setKeyAlias method, of class org.netbeans.modules.identity.profile.api.configurator.impl.file.ProviderConfigImpl.
     */
    public void testSetKeyAlias() {
        System.out.println("setKeyAlias");
        
        ServerProperties prop = new ServerProperties();
        prop.setProperty(ServerProperties.PROP_ID, "server1");
        String keyAlias = "alias";
        org.netbeans.modules.identity.profile.api.configurator.impl.file.ProviderConfigImpl instance = 
                new ProviderConfigImpl("testprovider1", "WSC", prop, path);
        instance.setKeyAlias(keyAlias);
        instance.saveProvider();
        
        org.netbeans.modules.identity.profile.api.configurator.impl.file.ProviderConfigImpl instance1 = 
                new ProviderConfigImpl("testprovider1", "WSC", prop, path);
        String val = instance1.getKeyAlias();
        assertEquals(keyAlias, val);
    }

    /**
     * Test of setKeyStore method, of class org.netbeans.modules.identity.profile.api.configurator.impl.file.ProviderConfigImpl.
     */
    public void testSetKeyStore() {
        System.out.println("setKeyStore");
        
        String location = "localtionpath";
        String password = "password";
        ServerProperties prop = new ServerProperties();
        prop.setProperty(ServerProperties.PROP_ID, "server1");
        String keyAlias = "alias";
        org.netbeans.modules.identity.profile.api.configurator.impl.file.ProviderConfigImpl instance = 
                new ProviderConfigImpl("testprovider1", "WSC", prop, path);
        instance.setKeyStore(location, password);
        instance.saveProvider();
        
        org.netbeans.modules.identity.profile.api.configurator.impl.file.ProviderConfigImpl instance1 = 
                new ProviderConfigImpl("testprovider1", "WSC", prop, path);
        String loc = instance1.getKeyStoreFile();
        String pass = instance1.getKeyStorePassword();
        assertEquals(location, loc);
        assertEquals(password, pass);
    }

    /**
     * Test of setSecurityMechanisms method, of class org.netbeans.modules.identity.profile.api.configurator.impl.file.ProviderConfigImpl.
     */
    public void testSetSecurityMechanisms() {
        System.out.println("setSecurityMechanisms");
        ServerProperties prop = new ServerProperties();
        prop.setProperty(ServerProperties.PROP_ID, "server1");
        List securityMechs = new ArrayList();
        org.netbeans.modules.identity.profile.api.configurator.impl.file.ProviderConfigImpl instance = 
                new ProviderConfigImpl("testprovider1", "WSC", prop, path);
        securityMechs.add(new SecurityMechanismImpl("X509", "urn:uri:x509"));
        instance.setSecurityMechanisms(securityMechs);
        instance.saveProvider();
        
        org.netbeans.modules.identity.profile.api.configurator.impl.file.ProviderConfigImpl instance1 = 
                new ProviderConfigImpl("testprovider1", "WSC", prop, path);
        List val = instance1.getSecurityMechanisms();
        assertEquals(((SecurityMechanism)securityMechs.get(0)).getName(), 
                ((SecurityMechanism)val.get(0)).getName());
    }

    /**
     * Test of getAllSupportedSecurityMech method, of class org.netbeans.modules.identity.profile.api.configurator.impl.file.ProviderConfigImpl.
     */
    public void testGetAllSupportedSecurityMech() {
        System.out.println("getAllSupportedSecurityMech");
        
        ServerProperties prop = new ServerProperties();
        prop.setProperty(ServerProperties.PROP_ID, "server1");
        
        org.netbeans.modules.identity.profile.api.configurator.impl.file.ProviderConfigImpl instance =
                new ProviderConfigImpl("testprovider1", "WSC", prop, path);
        
        List expResult = new ArrayList();
        expResult.add(new SecurityMechanismImpl("SAML_HolderKey", "urn:uri:saml:HK"));
        expResult.add(new SecurityMechanismImpl("SAML_SenderVouches", "urn:uri:saml:SV"));
        List result = instance.getAllSupportedSecurityMech();
        assertEquals(expResult.size(), result.size());
    }

    /**
     * Test of deleteProvider method, of class org.netbeans.modules.identity.profile.api.configurator.impl.file.ProviderConfigImpl.
     */
    public void testDeleteProvider() {
        System.out.println("deleteProvider");
        
        ServerProperties prop = new ServerProperties();
        prop.setProperty(ServerProperties.PROP_ID, "server1");
        
        org.netbeans.modules.identity.profile.api.configurator.impl.file.ProviderConfigImpl instance =
                new ProviderConfigImpl("testprovider2", "WSC", prop, path);
        instance.saveProvider();
        Unmarshaller unmarshaller = null;
        JAXBElement<AMConfigType> amconfig = null;
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(
                        "org.netbeans.modules.identity.profile.api.configurator.impl.file.jaxb");
            unmarshaller = jaxbContext.createUnmarshaller();
            amconfig = (JAXBElement<AMConfigType>)
                unmarshaller.unmarshal(new File(path + "/amserver/amconfig.xml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(amconfig.getValue().getProviderConfig().size(), 3);
            
        instance.deleteProvider();
        try {
            amconfig = (JAXBElement<AMConfigType>)
                    unmarshaller.unmarshal(new File(path + "/amserver/amconfig.xml"));
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertEquals(amconfig.getValue().getProviderConfig().size(), 2);
    }
}
