/*
 * Icons.java
 *
 * Created on November 8, 2006, 2:58 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.java.navigation.base;

import java.awt.Image;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.openide.util.ImageUtilities;
import org.openide.util.Utilities;

/** Capable of serving incns for the navigator modules. Notice that it is not
 * used for Element icons. May not be necessary an may be removed later.
 *
 * @author phrebejk
 */
public class Icons {
    
    private static final String GIF_EXTENSION = ".gif"; // NOI18N
    private static final String ICON_BASE = "org/netbeans/modules/java/navigation/resources"; // NOI18N
    private static final String WAIT = ICON_BASE + "wait" + GIF_EXTENSION; // NOI18N
       
    /** Creates a new instance of Icons */
    public Icons() {
    }
    
    public static Icon getBusyIcon () {
        Image img = ImageUtilities.loadImage (WAIT);
        if (img == null) {
            return null;
        }
        else {
            return new ImageIcon (img);
        }
    }
    
}
