/*
 * HTMLContainer.java
 *
 * Created on October 17, 2002, 7:43 PM
 */

package org.netbeans.performance.spi.html;

/** Interface defining HTML containers.  An HTML container is an HTML instance that
 * can contain other HTML instances (such as a table, a list or a document).
 * @author  Tim Boudreau
 */    
public interface HTMLContainer {
    /** Add an HTML element to the container element.
     * @param html The string to add.
     */        
    public void add(HTML html);
    /** Add a String to the container (it will be wrapped in an HTMLTextElement instance
     * and passed to <code>add (HTML html)</code>.
     * @param st The string to add.
     */        
    public void add(String st);
    /** Returns an iterator that can iterate all of the elements of the container in
     * order.
     */        
    public HTMLIterator iterator();
}
