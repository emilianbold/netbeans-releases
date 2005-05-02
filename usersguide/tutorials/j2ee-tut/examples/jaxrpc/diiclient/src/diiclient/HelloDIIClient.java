/*
 * HelloDIIClient.java
 *
 * Created on April 27, 2005, 11:21 AM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package diiclient;

import javax.xml.rpc.Call;
import javax.xml.rpc.Service;
import javax.xml.rpc.JAXRPCException;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceFactory;
import javax.xml.rpc.ParameterMode;

/**
 *
 * @author jungi
 */
public class HelloDIIClient {
    
    private static String qnameService = "Hello";
    private static String qnamePort = "HelloSEI";
    private static String BODY_NAMESPACE_VALUE = "urn:Hello/wsdl";
    private static String ENCODING_STYLE_PROPERTY =
            "javax.xml.rpc.encodingstyle.namespace.uri";
    private static String NS_XSD = "http://www.w3.org/2001/XMLSchema";
    private static String URI_ENCODING =
            "http://schemas.xmlsoap.org/soap/encoding/";
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        System.out.println("Endpoint address = " + args[0]);
        try {
            ServiceFactory factory = ServiceFactory.newInstance();
            Service service = factory.createService(new QName(qnameService));
            
            QName port = new QName(qnamePort);
            
            Call call = service.createCall(port);
            
            call.setTargetEndpointAddress(args[0]);
            call.setProperty(Call.SOAPACTION_USE_PROPERTY, new Boolean(true));
            call.setProperty(Call.SOAPACTION_URI_PROPERTY, "");
            call.setProperty(ENCODING_STYLE_PROPERTY, URI_ENCODING);
            
            QName QNAME_TYPE_STRING = new QName(NS_XSD, "string");
            
            call.setReturnType(QNAME_TYPE_STRING);
            
            call.setOperationName(new QName(BODY_NAMESPACE_VALUE, "sayHello"));
            call.addParameter("String_1", QNAME_TYPE_STRING, ParameterMode.IN);
            
            String[] params = { "Murph!" };
            
            String result = (String) call.invoke(params);
            
            System.out.println(result);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
    
}
