package helloservice;


/**
 * This is the service endpoint interface for the Helloweb service.
 * Created Apr 26, 2005 11:36:43 AM
 * @author Lukas Jungmann
 */

public interface HelloSEI extends java.rmi.Remote {
    /**
     * Web service operation
     */
    public String sayHello(java.lang.String s) throws java.rmi.RemoteException;
    
}
