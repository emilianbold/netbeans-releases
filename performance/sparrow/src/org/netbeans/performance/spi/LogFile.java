/*
 * LogFile.java
 *
 * Created on October 8, 2002, 12:40 PM
 */

package org.netbeans.performance.spi;
import java.io.IOException;
/** Basic abstraction of a log file, supplying file name and text
 * of the file.
 *
 * @author  Tim Boudreau
 */
public interface LogFile {
    public String getFileName();
    public String getFullText() throws IOException;
}
