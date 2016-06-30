/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 */
/**
 * AnagramsMBeanTest.java
 *
 * Created on Wed May 11 09:37:26 MEST 2005
 */
package com.toy.anagrams.mbeans;
import junit.framework.*;
import javax.management.*;
import java.lang.management.ManagementFactory;
import javax.management.NotCompliantMBeanException;

public class AnagramsMBeanTest extends TestCase {
   public AnagramsMBeanTest(String testName) {
       super(testName);
   }

   /**
    * Test of MaxThinkingTime Attribute
    */
   public void testMaxThinkingTimeAttribute() throws Exception {
       System.out.println("testMaxThinkingTimeAttribute");

       // Get JMX MBean Attribute.
       Integer val = (Integer) getAttribute("MaxThinkingTime");

       assertTrue("MaxThinkingTime is equals to 0", val == 0);
   }

   /**
    * Test of MinThinkingTime Attribute
    */
   public void testMinThinkingTimeAttribute() throws Exception {
       System.out.println("testMinThinkingTimeAttribute");

       // Get JMX MBean Attribute.
       Integer val = (Integer) getAttribute("MinThinkingTime");
       
       assertTrue("MinThinkingTime is equals to 0", val == 0);
   }

   /**
    * Test of CurrentAnagram Attribute
    */
   public void testCurrentAnagramAttribute() throws Exception {
       System.out.println("testCurrentAnagramAttribute");

       // Get JMX MBean Attribute.
       String val = (String) getAttribute("CurrentAnagram");
       
       assertTrue("CurrentAnagram is null", val == null);
   }

   /**
    * Test of resetThinkingTimes() Operation
    */
   public void testResetThinkingTimesOperation() throws Exception {
       System.out.println("testresetThinkingTimesOperation");

       // Operation signature.
       String[] signature = new String[] {
       };

       Object[] params = new Object[] {
       };

       // Invoke JMX MBean Operation.
       Object val = (Object) invoke("resetThinkingTimes",params,signature);

       assertTrue("reset returns value is void", val == null);
   }

   public Object createMBean() throws NotCompliantMBeanException {
       return new AnagramsStats();
   }

   protected void setUp() throws Exception {
       //JMX initialization
       jmxInit();
       //TODO add your own init code here
   }

   protected void tearDown() throws Exception {
       //JMX termination
       jmxTerminate();
       //TODO add your own terminate code here
   }

   public static Test suite() {
       TestSuite suite = new TestSuite(AnagramsMBeanTest.class);
       return suite;
   }

   /** JMX Initialization methods.
    * WARNING: Do NOT modify this code.
    */
   // <editor-fold defaultstate="collapsed" desc=" Generated Code ">
//GEN-BEGIN:generatedCode 
   private Object getAttribute(String attName) throws Exception {
       return getMBeanServer().getAttribute(mbeanName, attName);
   }

   private void setAttribute(Attribute att) throws Exception {
       getMBeanServer().setAttribute(mbeanName, att);
   }

   private Object invoke(String opName, Object params[],
                         String signature[]) throws Exception {
       return getMBeanServer().invoke(mbeanName, opName, params, signature);
   }

   private void jmxInit() throws Exception {
       mbeanName = new ObjectName(":type=AnagramsMBean");
       Object mbean = createMBean();
       server = ManagementFactory.getPlatformMBeanServer();
       server.registerMBean(mbean, mbeanName);
   }

   private void jmxTerminate() throws Exception {
       server.unregisterMBean(mbeanName);
   }

   private MBeanServer getMBeanServer() {
       return server;
   }

   private MBeanServer server;
   private ObjectName mbeanName;
   // </editor-fold> 
//GEN-END:generatedCode 
}
