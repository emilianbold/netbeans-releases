/*
 * HelloDProxyClient.java
 *
 * Created on April 27, 2005, 11:32 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package dynamicproxy;

import java.net.URL;
import javax.xml.rpc.Service;
import javax.xml.rpc.JAXRPCException;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceFactory;
import dynamicproxy.HelloSEI;

/**
 *
 * @author Lukas Jungmann
 */
public class HelloDProxyClient {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        try {
            
            String UrlString = args[0] + "?WSDL";
            String nameSpaceUri = "urn:Hello/wsdl";
            String serviceName = "Hello";
            String portName = "HelloSEIPort";
            
            System.out.println("UrlString = " + UrlString);
            URL helloWsdlUrl = new URL(UrlString);
            
            ServiceFactory serviceFactory =
                    ServiceFactory.newInstance();
            
            Service helloService =
                    serviceFactory.createService(helloWsdlUrl,
                    new QName(nameSpaceUri, serviceName));
            
            dynamicproxy.HelloSEI myProxy =
                    (dynamicproxy.HelloSEI)
                    helloService.getPort(
                    new QName(nameSpaceUri, portName),
                    dynamicproxy.HelloSEI.class);
            
            System.out.println(myProxy.sayHello("Buzz"));
            
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}
