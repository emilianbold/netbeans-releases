package org.jboss.docs.cmp2.crimeportal;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.Iterator;
import javax.naming.InitialContext;
import javax.ejb.EJBLocalObject;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.sourceforge.junitejb.EJBTestCase;

public class CrimePortalTest extends EJBTestCase
      implements CrimePortalTestConstants {

   public static Test suite() {
      TestSuite testSuite = new TestSuite("CrimePortalTest");
      testSuite.addTestSuite(CrimePortalTest.class);
      return testSuite;
   }   

   public CrimePortalTest(String name) {
      super(name);
   }

   private OrganizationHome organizationHome;
   private GangsterHome gangsterHome;
   private JobHome jobHome;

   /**
    * Looks up all of the home interfaces and creates the initial data. 
    * Looking up objects in JNDI is expensive, so it should be done once 
    * and cached.
    * @throws Exception if a problem occures while finding the home interfaces,
    * or if an problem occures while createing the initial data
    */
   public void setUp() throws Exception {
      InitialContext jndi = new InitialContext();

      organizationHome = 
            (OrganizationHome) jndi.lookup("crimeportal/Organization"); 

      gangsterHome = (GangsterHome) jndi.lookup("crimeportal/Gangster"); 

      jobHome = (JobHome) jndi.lookup("crimeportal/Job"); 
   }

   /** Test Organization-Gangster relationship */
   public void testOrganization() throws Exception {
      Organization yakuza = organizationHome.findByPrimaryKey("Yakuza");
      Collection gangsters = yakuza.getMemberGangsters();
      assertEquals(3, gangsters.size());
      assertTrue(gangsters.contains(gangsterHome.findByPrimaryKey(YOJIMBO)));
      assertTrue(gangsters.contains(gangsterHome.findByPrimaryKey(TAKESHI)));
      assertTrue(gangsters.contains(gangsterHome.findByPrimaryKey(YURIKO)));
   }

   /** Test find bad dudes query */
   public void testFindBadDudes() throws Exception {
      Collection gangsters = gangsterHome.findBadDudes(5);
      assertEquals(5, gangsters.size());

      assertTrue(gangsters.contains(
            gangsterHome.findByPrimaryKey(TAKESHI)));
      assertTrue(gangsters.contains(
            gangsterHome.findByPrimaryKey(CHOW)));
      assertTrue(gangsters.contains(
            gangsterHome.findByPrimaryKey(SHOGI)));
      assertTrue(gangsters.contains(
            gangsterHome.findByPrimaryKey(YOJIMBO)));
      assertTrue(gangsters.contains(
            gangsterHome.findByPrimaryKey(CORLEONE)));
   }

   /** Test select boss query */
   public void testSelectBoss() throws Exception {
      Set gangsters = gangsterHome.selectBoss(" Yojimbo ");
      assertEquals(1, gangsters.size());
      assertTrue(gangsters.contains(gangsterHome.findByPrimaryKey(TAKESHI)));

      gangsters = gangsterHome.selectBoss(" Takeshi ");
      assertEquals(1, gangsters.size());
      assertTrue(gangsters.contains(gangsterHome.findByPrimaryKey(TAKESHI)));

      gangsters = gangsterHome.selectBoss("non-existant");
      assertEquals(0, gangsters.size());
   }
}
