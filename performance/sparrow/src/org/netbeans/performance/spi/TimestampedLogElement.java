/*
 * TimestampedLogElement.java
 *
 * Created on October 8, 2002, 2:42 PM
 */

package org.netbeans.performance.spi;

/**
 *
 * @author  Tim Boudreau
 */
public abstract class TimestampedLogElement extends AbstractLogElement implements Timestamped {
    long timestamp=0l;
    /** Creates a new instance of TimestampedLogElement */
    public TimestampedLogElement(String s) {
        super (s);
    }
    
    public synchronized long getTimeStamp() {
        checkParsed();
        return timestamp;
    }
    
    /** Parse the String passed to the constructor, which presumably consists
     * of a line from a log file.  This method should populate any instance
     * fields which subclasses will provide accessors for.
     * Subclasses should not call this method directly, but instead call
     * checkParse() in accessors which rely of fully parsed data being
     * available.
     *
     */
    protected void parse() throws ParseException {
    }
    
}
