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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.sun.share.configbean;

import java.beans.BeanDescriptor;
import java.beans.BeanInfo;
import java.beans.EventSetDescriptor;
import java.beans.FeatureDescriptor;
import java.beans.MethodDescriptor;
import java.beans.PropertyDescriptor;
import junit.framework.TestCase;

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
