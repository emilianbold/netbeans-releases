/*
 * GcLog.java
 *
 * Created on October 8, 2002, 4:43 PM
 */

package org.netbeans.performance.impl.logparsing;
import org.netbeans.performance.spi.*;
import java.util.*;
import java.io.*;
/**Wrapper class for a JDK garbage collection log.
 *
 * @author  Tim Boudreau
 */
public class IdeCfg extends AbstractLogFile {
    private static final String IDE_CFG="javaconfig";
    
    /** Creates a new instance of GcLog using the
     * specified file.  */
    public IdeCfg(String filename) throws DataNotFoundException {
        super (filename);
        name = IDE_CFG;
    }
        
    /**Parse out all of the garbage collection entries from the
     * log file, and build some name-value statistics about them.
     */
    protected void parse() throws ParseException {
        String s;
        try {
            s=getFullText();
        } catch (IOException ioe) {
            if (ioe instanceof FileNotFoundException) {
                throw new DataNotFoundException ("Could not find log file.", ioe);
            } else {
                throw new ParseException ("Error getting log file text.", new File (getFileName()));
            }
        }
        StringTokenizer sk = new StringTokenizer(s);
        ArrayList switches = new ArrayList(10);
        while (sk.hasMoreElements()) {
            String el =sk.nextToken().trim();
            if (el.startsWith("-J")) {
                if (!(el.equals("-J-verbose:gc"))) {
                    if (!(el.startsWith("-J-D"))) {
                        JavaLineswitch curr = new JavaLineswitch(el.substring(2));
                        addElement (curr); 
                    }
                }
            }
        }
    }
    
    /**Test execution for debugging */
    public static void main (String[] args) {
        IdeCfg lg = new IdeCfg ("/home/tb97952/nb34/netbeans/bin/ide.cfg"); //("/space/nbsrc/performance/gc/report/vanilla/gclog");
        Iterator i = lg.iterator();
        System.out.println("Printing the stuff:");
        while (i.hasNext()) {
            System.out.println(i.next());
        }
    }
    
}
