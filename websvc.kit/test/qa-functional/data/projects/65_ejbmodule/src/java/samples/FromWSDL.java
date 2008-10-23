/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package samples;

import javax.ejb.Stateless;
import javax.jws.WebService;
import org.example.duke.AddNumbersFault_Exception;
import org.example.duke.AddNumbersPortType;

/**
 *
 * @author lukas
 */
@WebService(serviceName = "AddNumbersService", portName = "AddNumbersPort", endpointInterface = "org.example.duke.AddNumbersPortType", targetNamespace = "http://duke.example.org", wsdlLocation = "META-INF/wsdl/FromWSDL/AddNumbers.wsdl")
@Stateless
public class FromWSDL implements AddNumbersPortType {

    public int addNumbers(int arg0, int arg1) throws AddNumbersFault_Exception {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

    public void oneWayInt(int arg0) {
        //TODO implement this method
        throw new UnsupportedOperationException("Not implemented yet.");
    }

}
