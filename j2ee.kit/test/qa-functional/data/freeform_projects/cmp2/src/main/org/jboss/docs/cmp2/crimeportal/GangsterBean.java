package org.jboss.docs.cmp2.crimeportal;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import javax.ejb.CreateException;
import javax.ejb.EntityBean;
import javax.ejb.EntityContext;
import javax.ejb.FinderException;

public abstract class GangsterBean implements EntityBean
{
   private EntityContext ctx;

   public Integer ejbCreate(Integer id, String name, String nickName)
         throws CreateException
   {
      setGangsterId(id);
      setName(name);
      setNickName(nickName);
      return null;
   }

   public void ejbPostCreate(Integer id, String name, String nickName) { }

   // CMP field accessors -----------------------------------------------------
   public abstract Integer getGangsterId();
   public abstract void setGangsterId(Integer gangsterId);

   public abstract String getName();
   public abstract void setName(String name);

   public abstract String getNickName();
   public abstract void setNickName(String nickName);

   public abstract int getBadness();
   public abstract void setBadness(int badness);

   // CMR field accessors -----------------------------------------------------
   public abstract Organization getOrganization();
	public abstract void setOrganization(Organization org);

   public abstract Set getJobs();
	public abstract void setJobs(Set jobs);

   // ejbSelect methods -------------------------------------------------------
   public abstract Set ejbSelectBoss(String name) throws FinderException;

   // ejbHome methods ---------------------------------------------------------
   public Set ejbHomeSelectBoss(String name) throws FinderException {
      return ejbSelectBoss(name.trim());
   }

   // EJB callbacks -----------------------------------------------------------
   public void setEntityContext(EntityContext context)
   {
      ctx = context;
   }

   public void unsetEntityContext()
   {
      ctx = null;
   }

   public void ejbActivate()
   {
   }

   public void ejbPassivate()
   {
   }

   public void ejbRemove()
   {
   }

   public void ejbStore()
   {
   }

   public void ejbLoad()
   {
   }
}
