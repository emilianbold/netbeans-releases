/*
 * DataNotFoundException.java
 *
 * Created on October 13, 2002, 6:05 AM
 */

package org.netbeans.performance.spi;
import org.apache.tools.ant.*;
import org.apache.tools.ant.types.*;
import java.io.File;
/**An exception thrown when no test data is found (e.g. a log
 * file is missing or by client code that expects to find data
 * from a query and finds none.  For some tests, that may be 
 * normal and the exception should be handled.  In other cases,
 * the test should treat it as fatal.
 *
 * @author  Tim Boudreau
 */
public class DataNotFoundException extends BuildException {
    File file=null;
    
    public DataNotFoundException (String message) {
        super (message);
    }
    
    /** Creates a new instance of DataNotFoundException */
    public DataNotFoundException(String message, File f) {
        super (message);
        this.file = f;
    }
    
    public DataNotFoundException (String message, File f, Throwable t) {
        super (message, t);
        this.file = f;
    }
    
    public DataNotFoundException (String message, Throwable t) {
        super (message, t);
    }
    
    public String getMessage() {
        StringBuffer result = new StringBuffer(getClass().getName());
        result.append (": ");
        result.append(super.getMessage());
        if (file != null) {
            result.append ("\nFile: " + file.toString());
        }
        return result.toString();
    }
    
    public File getFile () {
        return file;
    }
}
