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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.compapp.projects.base.queries;

import org.netbeans.spi.queries.CollocationQueryImplementation;

import java.io.File;

public class SameDriveCollocationQuery implements CollocationQueryImplementation {

    /** Default constructor for lookup. */
    public SameDriveCollocationQuery() {}

    public boolean areCollocated(File file1, File file2) {
        if (file1.equals(file2)) {
            return false;
        }
        String f1 = file1.getAbsolutePath();
        String f2 = file2.getAbsolutePath();
        int idx1 = f1.indexOf(':');
        int idx2 = f2.indexOf(':');
        if ((idx1 > 0) && (idx1 == idx2)) {
            return (f1.substring(0,idx1).compareToIgnoreCase(f2.substring(0,idx2)) == 0);
        }
        return false;
    }

    public File findRoot(File file) {
        return null;
    }

}

