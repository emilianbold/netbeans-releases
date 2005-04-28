/*
 * Main.java
 *
 * Created on April 27, 2005, 11:21 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package timerclient;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import timer.TimerSessionRemote;
import timer.TimerSessionRemoteHome;

/**
 *
 * @author blaha
 */
public class Main {
    
    /** Creates a new instance of Main */
    public Main() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        try {
            Context initial = new InitialContext();
            Object objref =
                    initial.lookup("ejb/TimerSessionBean");
            TimerSessionRemoteHome home =
                    (TimerSessionRemoteHome) PortableRemoteObject.narrow(objref,
                    TimerSessionRemoteHome.class);
            
            TimerSessionRemote timerSession = home.create();
            long intervalDuration = 30000;
            
            System.out.println("Creating a timer with an interval duration of " +
                    intervalDuration + " ms.");
            timerSession.myCreateTimer(intervalDuration);
            timerSession.remove();
            
            System.exit(0);
        } catch (Exception ex) {
            System.err.println("Caught an unexpected exception!");
            ex.printStackTrace();
            System.exit(1);
        }
    }
    
}
