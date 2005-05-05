/*
 * CourseRemoteBusiness.java
 *
 * Created on 05 May 2005, 15:54
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package enroller;

import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 *
 * @author Administrator
 */
public interface CourseRemoteBusiness {
    
    public ArrayList getStudentIds() throws RemoteException;
    
    public String getName() throws RemoteException;
    
    public void setName(String name) throws RemoteException;
    
}
