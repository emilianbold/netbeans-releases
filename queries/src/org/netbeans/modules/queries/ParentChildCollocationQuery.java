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
import org.netbeans.spi.queries.CollocationQueryImplementation;

/**
 * Tests whether files are in parent-child relationship. Such files are
 * considered to be collocated.
 *
 * @author David Konecny
 */
public class ParentChildCollocationQuery implements CollocationQueryImplementation {
    
    /** Default constructor for lookup. */
    public ParentChildCollocationQuery() {}

    public boolean areCollocated(File file1, File file2) {
        if (file1.equals(file2)) {
            return true;
        }
        String f1 = file1.getAbsolutePath();
        if ((file1.isDirectory() || !file1.exists()) && !f1.endsWith(File.separator)) {
            f1 += File.separatorChar;
        }
        String f2 = file2.getAbsolutePath();
        if ((file2.isDirectory() || !file2.exists()) && !f2.endsWith(File.separator)) {
            f2 += File.separatorChar;
        }
        return f1.startsWith(f2) || f2.startsWith(f1);
    }
    
    public File findRoot(File file) {
        return null;
    }
    
}
