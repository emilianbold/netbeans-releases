package org.jboss.docs.cmp2.crimeportal;

import java.util.Collection;
import java.util.Set;
import javax.ejb.CreateException;
import javax.ejb.EJBLocalHome;
import javax.ejb.FinderException;

public interface GangsterHome extends EJBLocalHome
{
   Gangster create(Integer id, String name, String nickName)
         throws CreateException;

   Gangster findByPrimaryKey(Integer id) throws FinderException;

   Collection findAll() throws FinderException;
   Collection findBadDudes(int badness) throws FinderException;
   Set selectBoss(String name) throws FinderException;
}
