/*
 * HTMLSubdocument.java
 *
 * Created on October 17, 2002, 9:50 PM
 */

package org.netbeans.performance.spi.html;

/** A convenient HTML subdocument wrapper.  Handy if you are not creating
 * the sections of an HTML document in the order they should appear in 
 * the document.  Supports titles and generating a name link for the 
 * subdocument.
  * @author  Tim Boudreau
 */
public class HTMLSubdocument extends AbstractHTMLContainer {
    String name=null;
    /** Creates a new instance of HTMLSubdocument.  An A NAME tag will be
     * generated with the passed name.  A title header will appear if the
     * title is non-null and non-0-length. */
    public HTMLSubdocument(String title, String name, int preferredWidth) {
        super (title, preferredWidth);
        
    }

    public HTMLSubdocument(String title, String name) {
        super (title);
        this.name=name;
    }

    public HTMLSubdocument(String title) {
        super (title);
    }
    
    public HTMLSubdocument() {
    }
    
    public String getName() {
        return name;
    }
    
    public void toHTML (StringBuffer sb) {
	HTMLIterator i = iterator();
        if (name != null) {
            sb.append ("<A NAME=\"");
            sb.append (name);
            sb.append ("\">&nbsp;</A>");
        }
        if (title !=null) {
            sb.append ("<H3>");
            sb.append (title);
            sb.append ("</H3>\n");
        }
	while (i.hasNext()) {
            i.nextHTML().toHTML(sb);
            sb.append ("\n");
        }
    }
    
    public void para () {
        add ("<P>");
    }
    
    public void hr() {
        add ("<HR>");
    }
    
    public void br() {
        add ("<BR>");
    }
    
}
