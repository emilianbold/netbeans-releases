package org.jboss.docs.cmp2.crimeportal;

import java.util.Collection;
import java.util.Set;
import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;
import javax.ejb.FinderException;

public interface OrganizationHome extends javax.ejb.EJBLocalHome
{

   Organization create(String name, String description) 
         throws CreateException;

   Collection findAll() throws FinderException;

   Organization findByPrimaryKey(String pk) throws FinderException;
}
