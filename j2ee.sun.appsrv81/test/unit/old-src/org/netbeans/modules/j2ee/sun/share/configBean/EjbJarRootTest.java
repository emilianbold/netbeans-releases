/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
	
}
