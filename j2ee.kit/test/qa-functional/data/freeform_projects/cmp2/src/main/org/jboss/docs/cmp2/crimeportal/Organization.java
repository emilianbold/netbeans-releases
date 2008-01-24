package org.jboss.docs.cmp2.crimeportal;

import java.util.Set;
import javax.ejb.EJBLocalObject;

public interface Organization extends javax.ejb.EJBLocalObject
{

   String getName();

   String getDescription();
   void setDescription(String description);

   Set getMemberGangsters();

   Gangster getTheBoss();
   void setTheBoss(Gangster theBoss);
}
