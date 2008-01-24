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

public class CrimePortalTearDown extends EJBTestCase {
   public static Test suite() {
      TestSuite testSuite = new TestSuite("CrimePortalTearDown");
      testSuite.addTestSuite(CrimePortalTearDown.class);
      return testSuite;
   }   

   public CrimePortalTearDown(String name) {
      super(name);
   }

   public void testTearDown() throws Exception {
      InitialContext jndi = new InitialContext();

      // delete all organizations
      OrganizationHome organizationHome =
            (OrganizationHome) jndi.lookup("crimeportal/Organization"); 
      Iterator organizations = organizationHome.findAll().iterator();
      while(organizations.hasNext()) {
         EJBLocalObject ejb = (EJBLocalObject)organizations.next();
         ejb.remove();
      }   

      // delete all gangsters (should be cascade-deleted, but be safe)
      GangsterHome gangsterHome = 
            (GangsterHome) jndi.lookup("crimeportal/Gangster"); 
      Iterator gangsters = gangsterHome.findAll().iterator();
      while(gangsters.hasNext()) {
         EJBLocalObject ejb = (EJBLocalObject)gangsters.next();
         ejb.remove();
      }   

      // delete all jobs
      JobHome jobHome = (JobHome) jndi.lookup("crimeportal/Job"); 
      Iterator jobs = jobHome.findAll().iterator();
      while(jobs.hasNext()) {
         EJBLocalObject ejb = (EJBLocalObject)jobs.next();
         ejb.remove();
      }   
   }
}
