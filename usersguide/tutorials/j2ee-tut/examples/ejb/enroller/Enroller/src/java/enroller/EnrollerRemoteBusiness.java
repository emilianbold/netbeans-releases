/*
 * EnrollerRemoteBusiness.java
 *
 * Created on 05 May 2005, 15:58
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
public interface EnrollerRemoteBusiness {
    
    public void enroll(String studentId, String courseId)
    throws RemoteException;
    
    public void unEnroll(String studentId, String courseId)
    throws RemoteException;
    
    public void deleteStudent(String studentId) throws RemoteException;
    
    public void deleteCourse(String courseId) throws RemoteException;
    
    public ArrayList getStudentIds(String courseId) throws RemoteException;
    
    public ArrayList getCourseIds(String studentId) throws RemoteException;
    
}
