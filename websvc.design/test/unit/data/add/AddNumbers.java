/*
 * AddNumbers.java
 *
 * Created on March 29, 2007, 5:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package add;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 *
 * @author mkuchtiak
 */
@WebService()
public class AddNumbers {

    @WebMethod
    public int add(@WebParam(name = "x")
                   int x,
                   @WebParam(name = "y")
                   int y) {
        // TODO write your implementation code here:
        return 0;
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName="echo-operation")
    public String echo() {
        // TODO write your implementation code here:
        return "hello";
    }
    
    /**
     * Non Web service operation
     */    
    public String echo1() {
        // TODO write your implementation code here:
        return "hello";
    }

    /**
     * Web service operation
     */
    @WebMethod
    @Oneway
    public void send(@WebParam(name = "message")
                     String str) {
    }
    
    
}
