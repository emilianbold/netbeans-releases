/*
 * HTMLUnorderedList.java
 *
 * Created on October 17, 2002, 7:53 PM
 */

package org.netbeans.performance.spi.html;

/** Wrapper for unordered lists. 
 * @author Tim Boudreau
 */    
public class HTMLUnorderedList extends AbstractHTMLContainer {
    public HTMLUnorderedList() {

    }

    public HTMLUnorderedList(String title) {
        super(title);
    }

    public HTMLUnorderedList(String title, int preferredWidth) {
        super(title, preferredWidth);
    }

    public void toHTML (StringBuffer sb){
        sb.append ("<BR>");
        if (title.length() > 0) {
            sb.append("<B>");
            sb.append(title);
            sb.append("</B><BR>\n");
        }
        sb.append("<UL>");
        HTMLIterator i = iterator();
        HTML next;
        while (i.hasNext()) {
            next = i.nextHTML();
            if (next instanceof HTMLListItem) {
                sb.append(next.toHTML());
            } else {
                sb.append("\n   <LI>");
                sb.append(next.toHTML());
                sb.append("   </LI>");
            }
        }
        sb.append("</UL><P>\n");
    }
}
