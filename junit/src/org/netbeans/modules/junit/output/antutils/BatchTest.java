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

package org.netbeans.modules.junit.output.antutils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.apache.tools.ant.module.spi.TaskStructure;

/**
 *
 * @author  Marian Petras
 */
final class BatchTest {

    /** */
    private final AntProject project;
    
    /** */
    private Collection<FileSet> fileSets = new ArrayList<FileSet>();

    /**
     */
    BatchTest(AntProject project) {
        this.project = project;
    }
    
    /**
     */
    void handleChildrenAndAttrs(TaskStructure struct) {
        for (TaskStructure child : struct.getChildren()) {
            String childName = child.getName();
            if (childName.equals("fileset")) {                          //NOI18N
                FileSet fs = new FileSet(project);
                fileSets.add(fs);
                fs.handleChildrenAndAttrs(child);
                continue;
            }
        }
    }
    
    /**
     *
     */
    int countTestClasses() {
        int count = 0;
        for (FileSet fileSet : fileSets) {
            Collection<File> matchingFiles = FileSetScanner.listFiles(fileSet);
            for (File file : matchingFiles) {
                final String name = file.getName();
                if (name.endsWith(".java") || name.endsWith(".class")) {//NOI18N
                    count++;
                }
            }
        }
        //TODO - handle the situation that two or more filesets contain
        //       the same file
        return count;
    }

}
