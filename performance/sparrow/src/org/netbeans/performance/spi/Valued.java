/*
 * Valued.java
 *
 * Created on October 10, 2002, 1:08 AM
 */

package org.netbeans.performance.spi;

/** Interface defining objects that represent a value.  Used to allow
 * for writing Comparators and ElementFilters that don't need to know
 * the details of the object, since they compare only one attribute -
 * this makes it possible to write them in a generic way.
 *
 * @author  Tim Boudreau
 */
public interface Valued {
    public Object getValue();
}
