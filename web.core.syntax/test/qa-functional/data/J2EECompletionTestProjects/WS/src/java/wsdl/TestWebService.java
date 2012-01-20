/*
 * testWebService.java
 *
 * Created on May 24, 2006, 6:40 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package wsdl;

import javax.ejb.Stateless;
import javax.jws.HandlerChain;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.soap.SOAPBinding;

/**
 *
 * @author js201828
 */

//Annotations
/**CC
@W|
WebService
@WebService
*/

/**CC
@S|
SOAPBinding
@SOAPBinding
*/

//attributes
/**CC
@HandlerChain(|)
String file
@HandlerChain(file=)
*/

//values
/**CC
@WebParam(header=|)
true
@WebParam(header=true)
*/


//values
/**CC
@WebParam(mode=|)
Mode Mode.OUT
@WebParam(mode= WebParam.Mode.OUT)
*/

//file sections
/**
@HandlerChain(file="|")
../
@HandlerChain(file="../")
 */

//file sections
/**
@HandlerChain(file="../|")
wsdl/
@HandlerChain(file="../wsdl/")
*/

/**
@WebService(wsdlLocation="")
testWSDL.wsdl
@WebService(wsdlLocation="META-INF/wsdl/testWSDL.wsdl")
*/

public interface TestWebService extends java.rmi.Remote {
    
}




