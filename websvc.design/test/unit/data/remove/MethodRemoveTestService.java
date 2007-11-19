/*
 * AddNumbers.java
 *
 * Created on March 29, 2007, 5:08 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package remove;

import javax.jws.WebMethod;
import javax.jws.WebService;

/**
 *
 * @author mkuchtiak
 */
@WebService()
public class MethodRemoveTestService {

    /**
     * Web service operation
     * @return echo text
     */
    @WebMethod(operationName="echo-operation")
    public String echo() {
        // TODO write your implementation code here:
        return "hello";
    }
}
