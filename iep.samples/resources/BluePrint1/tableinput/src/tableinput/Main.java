/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tableinput;

import com.sun.jbi.engine.iep.core.runtime.client.pojo.TableSheperd;

/**
 *
 * @author Yanbing lu
 */
public class Main {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        System.setProperty("DatabaseType", "derby");
        System.setProperty("RuntimeStyle", "standAlone");
        System.setProperty("DatabaseHostname", "localhost");
        System.setProperty("DatabasePort", "1527");
        System.setProperty("DatabaseUsername", "iepseDB");
        System.setProperty("DatabasePassword", "iepseDB");
        System.setProperty("DatabaseSchemaName", "iepseDB");
        System.setProperty("DatabaseSid", "iepseDB");
        TableSheperd.main(new String[]{
            "PersonsOfInterest",
            "tableinput/PersonsOfInterest.txt"
        });

    }

}
