/*
 * MakeLayer.java
 *
 * Created on February 1, 2001, 4:51 PM
 */

package org.netbeans.nbbuild;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

import org.apache.tools.ant.FileScanner;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.taskdefs.MatchingTask;

/**
 *
 * @author  Michal Zlamal
 * @version 
 */
public class MakeLayer extends MatchingTask {

    private File dest = null;
    private List topdirs = new ArrayList ();

    /** Target file containing list of all classes. */
    public void setDestfile(File f) {
        dest = f;
    }

    /** Set the top directory.
     * There should be subdirectories under this matching pacgages.
     */
    public void setTopdir (File t) {
        topdirs.add (t);
    }

    /** Nested topdir addition. */
    public class Topdir {
        /** Path to an extra topdir. */
        public void setPath (File t) {
            topdirs.add (t);
        }
    }

    /** Add a nested topdir.
     * If there is more than one topdir total, build products
     * may be taken from any of them, including from multiple places
     * for the same module. (Later topdirs may override build
     * products in earlier topdirs.)
     */
    public Topdir createTopdir () {
        return new Topdir ();
    }
    
    public void execute()  throws BuildException {
        if (topdirs.isEmpty()) {
            throw new BuildException ("You must set at least one topdir attribute", location);
        }
        if (dest == null) {
            throw new BuildException("You must specify output file", location);
        }
        FileWriter layerFile;
        try {
            layerFile = new FileWriter(dest);
        }
        catch (IOException e) {
            throw new BuildException(e.fillInStackTrace(),location);
        }
        for (int j = 0; j < topdirs.size (); j++) {
            File topdir = (File) topdirs.get (j);
        
            FileScanner scanner = getDirectoryScanner (topdir);
            String[] files = scanner.getIncludedFiles ();
            for (int i=0; i <files.length; i++) {
                File aFileName = new File(topdir, files[i]);
                try {
                    layerFile.write("<file name=\""+aFileName.getName()+"\"\n");
                    layerFile.write("  url=\""+aFileName.getAbsolutePath().substring(topdir.getAbsolutePath().length()+1)+"\"/>\n");
                }
                catch(IOException ex) {
                    throw new BuildException(ex.fillInStackTrace(),location);
                }
            }
        }
        
        try {
            layerFile.close();
        }
        catch (IOException e) {
            throw new BuildException(e.fillInStackTrace(),location);
        }
    }
}
