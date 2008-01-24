package com.titan.cabin;

import java.rmi.RemoteException;
import javax.ejb.CreateException;
import javax.ejb.FinderException;

public interface CabinHomeRemote extends javax.ejb.EJBHome 
{
   public CabinRemote create(Integer id)
      throws CreateException, RemoteException;
   
   public CabinRemote findByPrimaryKey(Integer pk)
      throws FinderException, RemoteException;
}
