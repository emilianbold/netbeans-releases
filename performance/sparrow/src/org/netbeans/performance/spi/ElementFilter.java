/*
 * ElementFilter.java
 *
 * Created on October 8, 2002, 4:47 PM
 */

package org.netbeans.performance.spi;

/** Defines a filter object that can be used to retrieve only
 *  elements from a DataAggregation that it approves.  Useful for
 *  finding only elements of a certain kind.
 *
 * @author  Tim Boudreau
 */
public interface ElementFilter {
    public boolean accept (LogElement le);
}
