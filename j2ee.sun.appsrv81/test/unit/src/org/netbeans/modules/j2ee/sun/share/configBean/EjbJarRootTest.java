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
/*
 * EjbJarRootTest.java
 * JUnit based test
 *
 * Created on September 11, 2003, 10:18 AM
 */

package org.netbeans.modules.j2ee.sun.share.configbean;

import java.util.Map;
import java.util.HashMap;

import javax.enterprise.deploy.spi.DConfigBean;
import javax.enterprise.deploy.spi.DConfigBeanRoot;
import javax.enterprise.deploy.spi.DeploymentConfiguration;
import javax.enterprise.deploy.spi.exceptions.ConfigurationException;
import javax.enterprise.deploy.model.DDBean;
import javax.enterprise.deploy.model.DDBeanRoot;
import junit.framework.*;
import org.netbeans.modules.schema2beans.BaseBean;

import org.netbeans.modules.j2ee.sun.share.configbean.customizers.ejbmodule.*;

/**
 *
 * @author vkraemer
 */
public class EjbJarRootTest extends TestCase {
	
	/** Test of getDConfigBean method, of class org.netbeans.modules.j2ee.sun.share.configbean.BaseRoot. */
	public void testGetDConfigBean() {
		EjbJarRoot ejr = new EjbJarRoot();
                MockDDBeanRoot t = new MockDDBeanRoot();
                t.setXpath("/ejb-jar");
                t.setRoot(t);
                SunONEDeploymentConfiguration dc =
                    new SunONEDeploymentConfiguration(null,null);
                try {
                    ejr.init(t, dc, null);
                }
                catch (ConfigurationException ce) {
                    fail("why did I get I get a ConfigurationException here");
                }
		MockDDBean mock = new MockDDBean();
		DConfigBean dcb = null;
		try {
			dcb = ejr.getDConfigBean(mock);
			fail("bean with no xpath accepted");
		}
		catch (ConfigurationException ce) {
//			assertEquals(ce.getMessage(), "No factory for xpath: \"null\"");
		}
		try {
            //MockDDBeanRoot ddroot = new MockDDBeanRoot();
            //ddroot.setXpath("/ejb-jar");
			mock.setXpath(EjbJarRoot.SECURITY_ROLE_XPATH);
            mock.setRoot(t);
			DConfigBean dcb1 = ejr.getDConfigBean(mock);
			assertNotNull(dcb1);
                        assertTrue( dcb1 != dcb );
			SecurityRoleMapping srm = (SecurityRoleMapping) dcb;
                        mock = new MockDDBean();
			mock.setXpath(EjbJarRoot.ENTITY_XPATH);
            mock.setRoot(t);
			Map pairs = new HashMap();
			String retVal[] = new String[1];
			retVal[0] = EntityEjbDCBFactory.BEAN;
			pairs.put(EntityEjbDCBFactory.PERSISTENCE_TYPE_KEY, retVal);
			mock.setText(pairs);
			DConfigBean dcb2 = ejr.getDConfigBean(mock);
			assertNotNull(dcb2);
                        assertTrue(dcb2!=dcb1);
			EntityEjb ent = (EntityEjb) dcb2;
            EntityEjbCustomizer entCust = new EntityEjbCustomizer();
            try {
                ent.setRefreshPeriodInSeconds("20");
                ent.setCommitOption("foo");
                ent.setIsReadOnlyBean(Boolean.FALSE);
                ent.setJndiName("ejb/testJNDIName");
                ent.setPassByReference(Boolean.FALSE);
                ent.setPrincipalName("PrincipalOne");
            } catch (java.beans.PropertyVetoException pve) {
                pve.printStackTrace();
                fail("got an exception");
            }
            entCust.setObject(ent);
            entCust.validateEntries();
            entCust.getErrors();
            
                        mock = new MockDDBean();
			mock.setXpath(EjbJarRoot.SESSION_XPATH);
            mock.setRoot(t);
			pairs = new HashMap();
			retVal[0] = SessionEjbDCBFactory.STATELESS;
			pairs.put(SessionEjbDCBFactory.SESSION_TYPE_KEY, retVal);
			mock.setText(pairs);			
			DConfigBean dcb3 = ejr.getDConfigBean(mock);
			assertNotNull(dcb3);
                        assertTrue(dcb3!=dcb2);
			StatelessEjb less = (StatelessEjb) dcb;
            StatelessEjbCustomizer lessCust = new StatelessEjbCustomizer();
            lessCust.setObject(less);
            lessCust.getErrors();
                        mock = new MockDDBean();
			mock.setXpath(EjbJarRoot.MD_XPATH);
            mock.setRoot(t);                        
			DConfigBean dcb4 = ejr.getDConfigBean(mock);
			assertNotNull(dcb4);
                        assertTrue(dcb4!=dcb3);
			MDEjb mdb = (MDEjb) dcb4;
            MDEjbCustomizer mdbCust = new MDEjbCustomizer();
            mdbCust.setObject(mdb);
                        mock = new MockDDBean();
			mock.setXpath(EjbJarRoot.ENTITY_XPATH);
            mock.setRoot(t);
			pairs = new HashMap();
			retVal[0] = EntityEjbDCBFactory.CONTAINER;
			pairs.put(EntityEjbDCBFactory.PERSISTENCE_TYPE_KEY, retVal);
			mock.setText(pairs);
			DConfigBean dcb5 = ejr.getDConfigBean(mock);
			assertNotNull(dcb5);
                        assertTrue(dcb2!=dcb5);
            CmpEntityEjb cmp = (CmpEntityEjb) dcb5;
            try {
                cmp.setConsistency("foo");  
                cmp.setSchema("bar");
                cmp.setTableName("baz");
            } catch (java.beans.PropertyVetoException pve) {
                pve.printStackTrace();
                fail("got an exception");
            }
            CmpEntityEjbCustomizer cmpCust = new CmpEntityEjbCustomizer();
            cmpCust.setObject(dcb5);
            cmpCust.getErrors();
                        mock = new MockDDBean();
			mock.setXpath(EjbJarRoot.SESSION_XPATH);
            mock.setRoot(t);
			pairs = new HashMap();
			retVal[0] = SessionEjbDCBFactory.STATEFUL;
			pairs.put(SessionEjbDCBFactory.SESSION_TYPE_KEY, retVal);
			mock.setText(pairs);			
			DConfigBean dcb6 = ejr.getDConfigBean(mock);
			assertNotNull(dcb6);
                        assertTrue(dcb3!=dcb6);
			StatefulEjb ful = (StatefulEjb) dcb6; 
            StatefulEjbCustomizer fulCust = new StatefulEjbCustomizer();
            fulCust.setObject(ful);
                        mock = new MockDDBean();
			mock.setXpath(EjbJarRoot.SESSION_XPATH);
            mock.setRoot(t);
			pairs = new HashMap();
			retVal[0] = "statefull";
			pairs.put(SessionEjbDCBFactory.SESSION_TYPE_KEY, retVal);
			mock.setText(pairs);	
                        try {
                            DConfigBean dcb7 = ejr.getDConfigBean(mock);
                            fail("illegal session-type accepted");
                        }
                        catch (ConfigurationException ce) {
                            assertEquals("","Unknown session-type (Value: 'statefull')",ce.getMessage());
                        }
                        mock = new MockDDBean();
			mock.setXpath(EjbJarRoot.ENTITY_XPATH);
            mock.setRoot(t);
			pairs = new HashMap();
			retVal[0] = "statefull";
			pairs.put(EntityEjbDCBFactory.PERSISTENCE_TYPE_KEY, retVal);
			mock.setText(pairs);	
                        try {
                            DConfigBean dcb8 = ejr.getDConfigBean(mock);
                            fail("illegal session-type accepted");
                        }
                        catch (ConfigurationException ce) {
                            assertEquals("","Unknown persistence-type (Value: 'statefull')",ce.getMessage());
                        }
                }
		catch (ConfigurationException ce) {
            ce.printStackTrace();
			fail(ce.getMessage());
		}
			
	}	
	
	public EjbJarRootTest(java.lang.String testName) {
		super(testName);
	}
	
	public static Test suite() {
		TestSuite suite = new TestSuite(EjbJarRootTest.class);
		return suite;
	}
	
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
	
}
