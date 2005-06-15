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
