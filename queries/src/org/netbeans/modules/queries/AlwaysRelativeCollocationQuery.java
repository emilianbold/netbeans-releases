/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.queries;

import java.io.File;
import java.util.Arrays;
import org.netbeans.spi.queries.CollocationQueryImplementation;

/**
 * The CollocationQueryImplementation which makes all the paths in the single tree
 * relative. On the UNIX all the files lies within a single tree, all the files
 * are collocated. On the Windows (the VMS) all the files from single disk (volume) are collocated,
 * files lying on the different disks (volumes) are not collocated.
 * @author Tomas Zezula
 */
public class AlwaysRelativeCollocationQuery implements CollocationQueryImplementation {
    
    private File[] roots;
    
    /** Creates a new instance of AlwaysRelativeCollocationQuery */
    public AlwaysRelativeCollocationQuery() {
    }

    public File findRoot(File file) {
        File[] roots = getFileSystemRoots ();
        if (roots.length == 0) {
            assert false : "Cannot find filesystem roots";
            return null;
        }
        else if (roots.length == 1) {
            //On UNIX always relative
            return roots[0];
        }
        else {            
            while (!isRoot(file)) {
                file = file.getParentFile();
            }
            for (int i = 0; i < roots.length; i++) {
                if (file.equals(roots[i])) {
                    return roots[i];
                }
            }
            return null;
        }
    }

    public boolean areCollocated(File file1, File file2) {        
        File root1 = findRoot (file1);
        File root2 = findRoot (file2);
        return root1 != null && root1.equals(root2);
    }
    
    // ---------------- Unit test helper methods -----------------------        
    
    private File[] getFileSystemRoots () {
        if (this.roots != null) {
            return this.roots;
        }
        else {       
            return File.listRoots();
        }
    }
    
    private boolean isRoot (File f) {
        if (this.roots == null) {
            return f.getParentFile() == null;
        }
        else {
            //In unit test the roots are not necessary the real fs roots
            //we have to compare them
            return Arrays.asList(this.roots).contains(f);
        }
    }
    
    final void setFileSystemRoots (File[] roots) {
        this.roots = roots;
    }
    
}
