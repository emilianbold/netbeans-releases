/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ws;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 *
 * @author Lukas
 */
@WebService()
@SOAPBinding(style=SOAPBinding.Style.RPC, use=SOAPBinding.Use.LITERAL, parameterStyle=SOAPBinding.ParameterStyle.WRAPPED)

public class Echo_RPC {
/**
     * Web service operation
     */
    @WebMethod(operationName = "getString")
    public String getString(@WebParam(name = "parameter")
    String parameter) {
        return parameter;
    }

/**
     * Web service operation
     */
    @WebMethod(operationName = "getStringArray")
    public java.lang.String[] getStringArray(@WebParam(name = "parameter")
    java.lang.String[] parameter) {
        return parameter;
    }

/**
     * Web service operation
     */
    @WebMethod(operationName = "getInt")
    public int getInt(@WebParam(name = "parameter")
    int parameter) {
        return parameter;
    }

}
