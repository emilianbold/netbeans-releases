/*
 * FolderAggregation.java
 *
 * Created on October 8, 2002, 6:56 PM
 */

package org.netbeans.performance.impl.logparsing;
import org.netbeans.performance.spi.*;
import java.util.*;
/** A simple DataAggregation that acts as a folder for child
 *  aggregations.  This is used to separate different categories
 *  of DataAggregation, such as platforms, types of tests run,
 *  etc.  They can be stacked arbitrarily deep.
 *
 * @author  Tim Boudreau
 */
public class FolderAggregation extends AbstractDataAggregation implements Named {
    /** Creates a new instance of FolderAggregation */
    public FolderAggregation(String foldername) {
        name = foldername;
    }
    
    public String toString() {
        if (name.indexOf ("/") != -1) 
            return name.substring (name.lastIndexOf ("/"));
        return name;
    }
    
    public String getName() {
        return name;
    }    
    
    public void addElement (LogElement le) {
        super.addElement (le);
    }
    
    protected void parse() throws ParseException {
    }    
        
    
    /**Test execution for debugging */
    public static void main (String[] args) {
        FolderAggregation f = new FolderAggregation ("tests");
        IdeCfg ic = new IdeCfg ("/home/tb97952/nb34/netbeans/bin/ide.cfg"); //("/space/nbsrc/performance/gc/report/vanilla/gclog");
        GcLog gcl = new GcLog ("/space/nbsrc/performance/gc/report/vanilla/gclog");
        NbLog lg = new NbLog ("/home/tb97952/.netbeans/3.4/system/ide.log");
        f.addElement (ic);
        f.addElement (lg);
        f.addElement (gcl);
        
        Iterator i = f.iterator();
        while (i.hasNext()) {
            System.out.println(i.next());
        }
    }
    
    
}
