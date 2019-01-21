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
 * 
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
