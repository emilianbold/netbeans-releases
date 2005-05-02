package helloservice;


/**
 * This is the implementation bean class for the Hello web service.
 * Created Apr 26, 2005 11:36:43 AM
 * @author Lukas Jungmann
 */
public class HelloImpl implements HelloSEI {
    
    public String message = "Hello ";
    
    // Enter web service operations here. (Popup menu: Web Service->Add Operation)
    /**
     * Web service operation
     */
    public String sayHello(java.lang.String s) {
        // TODO implement operation 
        return message + s;
    }
    
}
