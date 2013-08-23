package com.titan.cabin;

import java.rmi.RemoteException;

public interface CabinRemote extends javax.ejb.EJBObject 
{
   public String getName() throws RemoteException;
   public void setName(String str) throws RemoteException;
   public int getDeckLevel() throws RemoteException;
   public void setDeckLevel(int level) throws RemoteException;
   public int getShipId() throws RemoteException;
   public void setShipId(int sp) throws RemoteException;
   public int getBedCount() throws RemoteException;
   public void setBedCount(int bc) throws RemoteException; 

    String getHello() throws java.rmi.RemoteException;
}
