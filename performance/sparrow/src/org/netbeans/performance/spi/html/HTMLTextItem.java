/*
 * HTMLTextItem.java
 *
 * Created on October 17, 2002, 8:00 PM
 */

package org.netbeans.performance.spi.html;

 /** Wrapper for items containing text. 
  * @author Tim Boudreau
  */    
public class HTMLTextItem extends AbstractHTML {
    String text;
    public HTMLTextItem(String s) {
        text = s;
    }
    
    public HTMLTextItem(Object o) {
        text = o.toString();
    }

    public HTMLTextItem(String s, int preferredWidth) {
        super (preferredWidth);
        text = s;
    }

    public void toHTML (StringBuffer sb) {
        sb.append(text + "\n");
    }

}
