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
 * DConfigBeanBeanInfoTest.java
 *
 * Created on January 11, 2004, 1:04 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean;

import java.beans.*;

import org.netbeans.modules.j2ee.sun.share.configbean.*;

import junit.framework.*;
import junit.textui.*;


/**
 *
 * @author Peter Williams
 */
public class DConfigBeanBeanInfoTest extends TestCase {
	
	/** -----------------------------------------------------------------------
	 * Test harness code
	 */
	/** Constructor
	 */
	public DConfigBeanBeanInfoTest(String testName) {
		super(testName);
	}
	
    public static void main(java.lang.String[] args) {
        junit.textui.TestRunner.run(suite());
    }
	
    public static Test suite() {
        TestSuite suite = new TestSuite(DConfigBeanBeanInfoTest.class);
        return suite;
    }
	
	protected void setUp() throws java.lang.Exception {
		// setup for beaninfo integrity tests
	}
	
	protected void tearDown() {
		// teardown for beaninfo integrity tests.
	}
	
	/** -----------------------------------------------------------------------
	 * All tests to follow
	 */
	/** WebAppRoot
	 */ 
	public void testWebAppRootBeanInfo() {
		testBeanInfo(new WebAppRootBeanInfo());
	}
	
	/** SessionConfiguration
	 */ 
	/*public void testSessionConfigurationBeanInfo() {
		testBeanInfo(new SessionConfigurationBeanInfo());
	}*/
	
	/** ServletRef
	 */ 
	public void testServletRefBeanInfo() {
		testBeanInfo(new ServletRefBeanInfo());
	}

	/** SecurityRoleMapping
	 */ 
	public void testSecurityRoleMappingBeanInfo() {
		testBeanInfo(new SecurityRoleMappingBeanInfo());
	}

	/** EjbRef
	 */ 
	public void testEjbRefBeanInfo() {
		testBeanInfo(new EjbRefBeanInfo());
	}
	
	/** ResourceRef
	 */ 
	public void testResourceRefBeanInfo() {
		testBeanInfo(new ResourceRefBeanInfo());
	}
	
	/** ResourceEnvRef
	 */ 
	public void testResourceEnvRefBeanInfo() {
		testBeanInfo(new ResourceEnvRefBeanInfo());
	}
	
	/** ServiceRef
	 */ 
	public void testServiceRefBeanInfo() {
		testBeanInfo(new ServiceRefBeanInfo());
	}
	
	/** Test specified BeanInfo for integrity
	 */
	private void testBeanInfo(BeanInfo beanInfo) {
		String beanInfoName = beanInfo.getClass().getName();
//		String beanName = beanInfoName.substring(0, beanInfoName.length()-8);
		System.out.println("Testing integrity of " + beanInfoName);
		
		BeanDescriptor bd = beanInfo.getBeanDescriptor();
		assertNotNull(beanInfoName + " does not specify a customizer", bd.getCustomizerClass());

		PropertyDescriptor pd[] = beanInfo.getPropertyDescriptors();
		verifyDescriptorArray("PropertDescriptor", pd);
		
		MethodDescriptor md[] = beanInfo.getMethodDescriptors();
		verifyDescriptorArray("MethodDescriptor", md);
		
		EventSetDescriptor esd[] = beanInfo.getEventSetDescriptors();
		verifyDescriptorArray("EventSetDescriptor", esd);
	}
	
	private void verifyDescriptorArray(String descriptorName, FeatureDescriptor[] fd) {
		if(fd == null) {
			return;	 // null is ok
		}
		
		for(int i = 0; i < fd.length; i++) {
			if(fd[i] == null) {
				fail(descriptorName + " array null at index " + i);
			}
		}
	}
}
