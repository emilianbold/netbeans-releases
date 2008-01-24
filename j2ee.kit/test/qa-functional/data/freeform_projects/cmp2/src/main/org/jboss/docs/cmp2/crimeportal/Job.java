package org.jboss.docs.cmp2.crimeportal;

import java.util.Set;
import javax.ejb.EJBLocalObject;

public interface Job extends EJBLocalObject
{
	String getName();

   double getScore();
   void setScore(double score);

   double getSetupCost();
   void setSetupCost(double setupCost);

   Set getGangsters();
}
