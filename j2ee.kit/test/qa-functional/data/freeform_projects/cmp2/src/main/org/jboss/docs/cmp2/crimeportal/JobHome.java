package org.jboss.docs.cmp2.crimeportal;

import java.util.Collection;
import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;
import javax.ejb.FinderException;

public interface JobHome extends EJBLocalHome
{
   Job create(String name) throws CreateException;

   Job findByPrimaryKey(String name) throws FinderException;

   Collection findAll() throws FinderException;
}
