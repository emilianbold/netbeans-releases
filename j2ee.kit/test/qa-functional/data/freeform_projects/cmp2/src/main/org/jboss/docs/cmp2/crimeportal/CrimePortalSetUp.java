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

public class CrimePortalSetUp extends EJBTestCase 
      implements CrimePortalTestConstants {

   public static Test suite() {
      TestSuite testSuite = new TestSuite("CrimePortalSetUp");
      testSuite.addTestSuite(CrimePortalSetUp.class);
      return testSuite;
   }   

   public CrimePortalSetUp(String name) {
      super(name);
   }


   /**
    * Looks up all of the home interfaces and creates the initial data. 
    * @throws Exception if a problem occures while finding the home interfaces,
    * or if an problem occures while createing the initial data
    */
   public void testSetUp() throws Exception {
      InitialContext jndi = new InitialContext();

      OrganizationHome organizationHome =
            (OrganizationHome) jndi.lookup("crimeportal/Organization"); 

      GangsterHome gangsterHome = 
            (GangsterHome) jndi.lookup("crimeportal/Gangster"); 

      JobHome jobHome = (JobHome) jndi.lookup("crimeportal/Job"); 

      // Create some organizations
      Organization yakuza = 
            organizationHome.create("Yakuza", "Japanese Gangsters");
      Organization mafia = 
            organizationHome.create("Mafia", "Italian Bad Guys");
      Organization triads = 
            organizationHome.create("Triads", "Kung Fu Movie Extras");

      // Create some gangsters
      Gangster yojimbo = gangsterHome.create(YOJIMBO, "Yojimbo", "Bodyguard");
      yojimbo.setBadness(7);
      yojimbo.setOrganization(yakuza);

      Gangster takeshi = gangsterHome.create(TAKESHI, "Takeshi", "Master");
      takeshi.setBadness(10);
      takeshi.setOrganization(yakuza);

      Gangster yuriko = gangsterHome.create(YURIKO, "Yuriko", "Four finger");
      yuriko.setBadness(4);
      yuriko.setOrganization(yakuza);

      Gangster chow = gangsterHome.create(CHOW, "Chow", "Killer");
      chow.setBadness(9);
      chow.setOrganization(triads);

      Gangster shogi = gangsterHome.create(SHOGI, "Shogi", "Lightning");
      shogi.setBadness(8);
      shogi.setOrganization(triads);
      
      Gangster valentino = 
            gangsterHome.create(VALENTINO, "Valentino", "Pizza-Face");
      valentino.setBadness(4);
      valentino.setOrganization(mafia);

      Gangster toni = gangsterHome.create(TONI, "Toni", "Toohless");
      toni.setBadness(2);
      toni.setOrganization(mafia);

      Gangster corleone = 
            gangsterHome.create(CORLEONE, "Corleone", "Godfather");
      corleone.setBadness(6);
      corleone.setOrganization(mafia);

      // Assign the bosses
      yakuza.setTheBoss(takeshi);
      triads.setTheBoss(chow);
      mafia.setTheBoss(corleone);

      // Create some jobs
      Job jewler = jobHome.create("10th Street Jeweler Heist");
      jewler.setScore(5000);
      jewler.setSetupCost(50);
      
      Job train = jobHome.create("The Greate Train Robbery");
      train.setScore(2000000);
      train.setSetupCost(500000);

      Job liquorStore = jobHome.create("Cheap Liquor Snatch and Grab");
      liquorStore.setScore(50);
      liquorStore.setSetupCost(0);

      // assign some gangsters to the jobs
      jewler.getGangsters().add(valentino);
      jewler.getGangsters().add(corleone);
      
      train.getGangsters().add(yojimbo);
      train.getGangsters().add(chow);

      liquorStore.getGangsters().add(chow);
   }
}
