/*
 * HTMLDocument.java
 *
 * Created on October 17, 2002, 7:57 PM
 */

package org.netbeans.performance.spi.html;
import java.util.*;
/** Wrapper for an HTML document. 
* @author Tim Boudreau
*/    
public class HTMLDocument extends AbstractHTMLContainer {

    public HTMLDocument(String title) {
        super(title);
    }

    public HTMLDocument() {
    }

    public void toHTML(StringBuffer sb) {
        if (title.length() > 0) {
            genHtmlHeader(sb, title);
            sb.append("<H1>");
            sb.append(title);
            sb.append("</H1>\n<BR>");
        } else {
            genHtmlHeader(sb);
        }
        HTMLIterator i = iterator();
        while (i.hasNext()) {
            i.nextHTML().toHTML (sb);
        }
        sb.append("\n");
        genHtmlFooter(sb);
    }
}

