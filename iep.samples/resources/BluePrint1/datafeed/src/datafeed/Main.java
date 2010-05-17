/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package datafeed;

import com.sun.jbi.engine.iep.core.runtime.client.pojo.SheperdDriver;

/**
 *
 * @author Bing Lu 
 */
public class Main {
    
    /** Creates a new instance of Main */
    public Main() {
    }
    
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        SheperdDriver.main(new String[] {
            "datafeed/config.properties", //properties file
            "datafeed/data.txt", //data file 
            "1", //number of events per batch (must be consistent with template.xml)
            "200", //waiting interval (miliseconds) between each batch. Use -1 for interactive mode.
            "1" //number of times to repeat sending data file
        });
    }
    
}
