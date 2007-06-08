/*
 * AddNumbers.java
 *
 * Created on March 29, 2007, 5:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package hello;


import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.WebResult;

/**
 *
 * @author mkuchtiak
 */
@WebService()
public interface HelloInterface {

    /**
     * hello Method
     * @param x name
     * @return echo string
     */    
    @WebMethod(operationName="hello_operation")
    @WebResult(name="echoString", targetNamespace = "http://www.netbeans.org/hello")
    public String hello(@WebParam(name = "name")String s);
}
