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
import add.foo.Foo;
import add.foo.FooException;

/**
 *
 * @author mkuchtiak
 */
@WebService(name="AddNumbers", serviceName="AddNumbers", targetNamespace="http://www.netbeans.org/tests/AddNumbersTest")
public class AddNumbers_1 {
    
    /**
     * add Method
     * @param x first number
     * @param y second number
     * @return SUM of 2 numbers
     */  
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
     * @return echo text
     */
    @WebMethod(operationName="echo-operation")
    public String echo() throws FooException {
        // TODO write your implementation code here:
        return "hello";
    }
    
    /**
     * Web service operation
     */
    @WebMethod(operationName="hello")
    public String hello(Foo foo) {
        // TODO write your implementation code here:
        return "hello "+foo.getName();
    }

    @WebMethod
    @Oneway
    public void send(@WebParam(name = "message")
                     String str, String to) {
    }
}
