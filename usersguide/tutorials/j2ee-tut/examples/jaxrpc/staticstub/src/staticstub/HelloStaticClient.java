/*
 * HelloStaticClient.java
 *
 * Created on April 26, 2005, 7:22 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package staticstub;

import javax.xml.rpc.Stub;
/**
 *
 * @author Lukas Jungmann
 */
public class HelloStaticClient {
    
    private String endpointAddress;

    public static void main(String[] args) {
        System.out.println("Endpoint address = " + args[0]);
        try {
            Stub stub = createProxy();
            stub._setProperty(javax.xml.rpc.Stub.ENDPOINT_ADDRESS_PROPERTY, args[0]);
            HelloSEI hello = (HelloSEI) stub;
            System.out.println(hello.sayHello("Duke!"));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static Stub createProxy() {
        // Note: Hello_Impl is implementation-specific.
        return (Stub) (new Hello_Impl().getHelloSEIPort());
    }
}
