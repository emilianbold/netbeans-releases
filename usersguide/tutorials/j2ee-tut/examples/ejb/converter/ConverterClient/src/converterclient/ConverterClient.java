/*
 * ConverterClient.java
 *
 * Created on May 3, 2005, 4:37 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package converterclient;
import converter.ConverterRemote;
import converter.ConverterRemoteHome;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import java.math.BigDecimal;


public class ConverterClient {
    public static void main(String[] args) {
        try {
            Context initial = new InitialContext();
            Object objref = initial.lookup("ejb/SimpleConverter");

            ConverterRemoteHome home = (ConverterRemoteHome) PortableRemoteObject.narrow(objref, ConverterRemoteHome.class);

            ConverterRemote currencyConverter = home.create();

            BigDecimal param = new BigDecimal("100.00");
            BigDecimal amount = currencyConverter.dollarToYen(param);

            System.out.println(amount);
            amount = currencyConverter.yenToEuro(param);
            System.out.println(amount);

            System.exit(0);
        } catch (Exception ex) {
            System.err.println("Caught an unexpected exception!");
            ex.printStackTrace();
        }
    }
}