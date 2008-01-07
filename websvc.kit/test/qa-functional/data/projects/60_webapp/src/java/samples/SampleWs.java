/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package samples;

import javax.jws.Oneway;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;

/**
 *
 * @author lukas
 */
@WebService(serviceName = "SampleWebService")
public class SampleWs {

    /**
     * Web service operation
     */
    @WebMethod(operationName = "voidOperation")
    @Oneway
    public void voidOperation() {
    }

    /**
     * Web service operation
     */
    @WebMethod(operationName = "sayHi")
    public String sayHi(
            @WebParam(name = "s") String s) {
        return "Hello " + s + "!";
    }
}
