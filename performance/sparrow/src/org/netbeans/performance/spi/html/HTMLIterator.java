/*
 * HTMLIterator.java
 *
 * Created on October 17, 2002, 7:44 PM
 */

package org.netbeans.performance.spi.html;
import java.util.*;
/** An Iterator implementation with a convenience method for getting the next
 * HTML element without casting.
 * @author  Tim Boudreau
 */    
public class HTMLIterator implements Iterator  {
    Iterator i=null;
    Collection collection;
    public HTMLIterator(Collection c) {
        collection = c;
    }

    private synchronized Iterator getWrapped() {
        if (i==null) {
            i = collection.iterator();
        }
        return i;
    }

    public boolean hasNext() {
        return getWrapped().hasNext();
    }

    public Object next() {
        return getWrapped().next();
    }

    /** Get the next element of the underlying collection, cast as an HTML instance. */        
    public HTML nextHTML() {
        return (HTML) getWrapped().next();
    }

    public void remove() {
        getWrapped().remove();
    }

}
