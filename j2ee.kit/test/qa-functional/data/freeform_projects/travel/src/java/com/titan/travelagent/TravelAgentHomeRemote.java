package com.titan.travelagent;

import java.rmi.RemoteException;
import javax.ejb.CreateException;

public interface TravelAgentHomeRemote extends javax.ejb.EJBHome {

    public TravelAgentRemote create()
        throws RemoteException, CreateException;

}
