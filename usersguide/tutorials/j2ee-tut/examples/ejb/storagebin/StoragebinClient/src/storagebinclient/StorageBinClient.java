/*
 * StorageBinClient.java
 *
 * Created on May 5, 2005, 3:05 PM
 *
 * To change this template, choose Tools | Options and locate the template under
 * the Source Creation and Management node. Right-click the template and choose
 * Open. You can then make changes to the template in the Source Editor.
 */

package storagebinclient;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.rmi.PortableRemoteObject;
import storagebin.StorageBinRemote;
import storagebin.StorageBinRemoteHome;
import storagebin.WidgetRemote;
import storagebin.WidgetRemoteHome;


public class StorageBinClient {
    public static void main(String[] args) {
        try {
            Context initial = new InitialContext();
            Object objref =
                initial.lookup("ejb/StorageBinBean");

            StorageBinRemoteHome storageBinHome =
                (StorageBinRemoteHome) PortableRemoteObject.narrow(objref, StorageBinRemoteHome.class);

            objref = initial.lookup("ejb/WidgetBean");

            WidgetRemoteHome widgetHome =
                (WidgetRemoteHome) PortableRemoteObject.narrow(objref, WidgetRemoteHome.class);

            String widgetId = "777";
            StorageBinRemote storageBin = storageBinHome.findByWidgetId(widgetId);
            String storageBinId = (String) storageBin.getPrimaryKey();
            int quantity = storageBin.getQuantity();

            WidgetRemote widget = widgetHome.findByPrimaryKey(widgetId);
            double price = widget.getPrice();
            String description = widget.getDescription();

            System.out.println(widgetId + " " + storageBinId + " " + quantity +
                " " + price + " " + description);

            System.exit(0);
        } catch (Exception ex) {
            System.err.println("Caught an exception.");
            ex.printStackTrace();
        }
    }
}