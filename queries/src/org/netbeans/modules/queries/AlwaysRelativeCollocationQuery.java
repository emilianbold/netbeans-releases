/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.queries;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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
        final File[] roots = getFileSystemRoots ();
        if (roots.length == 0) {
            assert false : "Cannot find filesystem roots";
            return null;
        }
        else if (roots.length == 1) {
            //On UNIX always relative
            return roots[0];
        }
        else {
            final Set<File> rootsSet = new HashSet<File>(Arrays.asList(this.roots != null ? this.roots : roots));
            return getRoot (file, rootsSet);
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

    private File getRoot(File f, final Set<File> roots) {
        //We have to compare the file to File.listRoots(),
        //the test file.getParent() == null does not work on Windows
        //when the file was selected from the JFileChooser and user browsed
        //through the "This Computer" node
        while (f != null && !roots.contains(f)) {
            f = f.getParentFile();
        }
        return f;
    }

    final void setFileSystemRoots (File[] roots) {
        this.roots = roots;
    }

}
