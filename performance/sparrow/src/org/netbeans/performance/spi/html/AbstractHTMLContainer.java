/*
 * AbstractHTMLContainer.java
 *
 * Created on October 17, 2002, 7:47 PM
 */

package org.netbeans.performance.spi.html;
import java.util.*;
/** Convenience base class for HTML container elements. 
* @author  Tim Boudreau
*/    
abstract class AbstractHTMLContainer extends AbstractHTML implements HTMLContainer {
    /** The HTML items in the container. */        
    protected List items = new LinkedList();
    /** The title of the container or null. */        
    protected String title = "";
    protected AbstractHTMLContainer(String title, int preferredWidth) {
        super(preferredWidth); 
        this.title=title;
    }
    protected AbstractHTMLContainer(String title) {
        this.title = title;
    }
    protected AbstractHTMLContainer(int preferredWidth) {
        this.title = title;
    }
    protected AbstractHTMLContainer() {
    }
    public void add(HTML html) {
        if (html == this) throw new IllegalArgumentException ("Cannot add an element to itself!");
        items.add(html);
    }
    public void add(String st) {
        items.add(new HTMLTextItem(st)); 
    }
    public HTMLIterator iterator() {
        return new HTMLIterator(items);
    }
}

