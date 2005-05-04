package dynamicproxy;

import java.net.URL;
import javax.xml.rpc.Service;
import javax.xml.rpc.JAXRPCException;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceFactory;
import dynamicproxy.HelloSEI;

public class HelloDProxyClient {
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
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
