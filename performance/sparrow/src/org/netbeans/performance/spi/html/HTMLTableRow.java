/*
 * HTMLTableRow.java
 *
 * Created on October 17, 2002, 8:16 PM
 */

package org.netbeans.performance.spi.html;
import java.util.*;    

/** Convenience wrapper for things that need to create a 
 *  bunch of table rows.
 *
 * @author  Tim Boudreau
 */
public class HTMLTableRow extends AbstractHTMLContainer {
    /** Creates a new instance of HTMLTableRow */
    public HTMLTableRow(String item1, String item2) {
        add (item1);
        add (item2);
    }
    
    
    int targetColspan=-1;
    public void toHTML (StringBuffer sb) {
        HTMLIterator i = iterator();
        sb.append("<TR>");
        int padding = 0;
        if (targetColspan > 0) {
            padding = targetColspan - items.size();
        }
        if (padding < 0) {
            sb.append ("<TD COLSPAN=");
            sb.append (Integer.toString(targetColspan));
            sb.append ("><TABLE BORDER=0 WIDTH=100% HEIGHT=100%><TR>");
        }
        if (padding == 0) targetColspan = -1;
            
        
        while (i.hasNext()) {
            sb.append ("<TD");
            if (targetColspan > 0) {
                sb.append (" COLSPAN=");
                sb.append (Integer.toString(padding));
                targetColspan = -1;
            }
            i.nextHTML().toHTML (sb);
            sb.append ("</TD>");
        }
        
        if (padding < 0) {
            sb.append ("</TD></TR></TABLE></TD>");
        }
        
    }
}
