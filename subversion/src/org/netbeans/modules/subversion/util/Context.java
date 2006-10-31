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

package org.netbeans.modules.subversion.util;

import org.netbeans.modules.versioning.util.FlatFolder;

import java.io.File;
import java.io.Serializable;
import java.util.*;

/**
 * Encapsulates context of an action. There are two ways in which context may be defined:
 * - list of files (f/b.txt, f/c.txt, f/e.txt)
 * - list of roots (top folders) plus list of exclusions (f),(a.txt, d.txt)
 *
 * @author Maros Sandor
 */
public class Context implements Serializable {

    public static final Context Empty = new Context( getEmptyList(), getEmptyList(), getEmptyList() );

    private static final long serialVersionUID = 1L;
    
    private final List<File> filteredFiles;
    private final List<File> rootFiles;
    private final List<File> exclusions;

    public Context(List<File> filteredFiles, List<File> rootFiles, List<File> exclusions) {
        this.filteredFiles = filteredFiles;
        this.rootFiles = rootFiles;
        this.exclusions = exclusions;
        while (normalize());
    }

    public Context(File file) {
        this(new File [] { file });
    }

    public Context(File [] files) {
        List<File> list = new ArrayList<File>(files.length);
        list.addAll(Arrays.asList(files));
        removeDuplicates(list);
        this.filteredFiles = list;
        this.rootFiles = list;
        this.exclusions = Collections.emptyList();
    }

    private boolean normalize() {
        for (Iterator<File> i = rootFiles.iterator(); i.hasNext();) {
            File root = i.next();
            for (Iterator<File> j = exclusions.iterator(); j.hasNext();) {
                File exclusion = j.next();
                if (SvnUtils.isParentOrEqual(exclusion, root)) {
                    j.remove();
                    exclusionRemoved(exclusion, root);
                    return true;
                }
            }
        }
        removeDuplicates(rootFiles);
        removeDuplicates(exclusions);
        return false;
    }

    private void removeDuplicates(List<File> files) {
        List<File> newFiles = new ArrayList<File>();
        outter: for (Iterator<File> i = files.iterator(); i.hasNext();) {
            File file = i.next();
            for (Iterator<File> j = newFiles.iterator(); j.hasNext();) {
                File includedFile = j.next();
                if (SvnUtils.isParentOrEqual(includedFile, file) && (file.isFile() || !(includedFile instanceof FlatFolder))) continue outter;
                if (SvnUtils.isParentOrEqual(file, includedFile) && (includedFile.isFile() || !(file instanceof FlatFolder))) {
                    j.remove();
                }
            }
            newFiles.add(file);
        }
        files.clear();
        files.addAll(newFiles);
    }
    
    private void exclusionRemoved(File exclusion, File root) {
        File [] exclusionChildren = exclusion.listFiles();
        if (exclusionChildren == null) return;
        for (int i = 0; i < exclusionChildren.length; i++) {
            File child = exclusionChildren[i];
            if (!SvnUtils.isParentOrEqual(root, child)) {
                exclusions.add(child);
            }
        }
    }

    public List<File> getRoots() {
        return rootFiles;
    }

    public List<File> getExclusions() {
        return exclusions;
    }

    /**
     * Gets exact set of files to operate on, it is effectively defined as (rootFiles - exclusions). This set
     * is NOT suitable for Update command because Update should operate on all rootFiles and just exclude some subfolders.
     * Otherwise update misses new files and folders directly in rootFiles folders. 
     *  
     * @return files to operate on
     */ 
    public File [] getFiles() {
        return filteredFiles.toArray(new File[filteredFiles.size()]);
    }

    public File[] getRootFiles() {
        return rootFiles.toArray(new File[rootFiles.size()]);
    }
    
    public boolean contains(File file) {
        outter : for (Iterator i = rootFiles.iterator(); i.hasNext();) {
            File root = (File) i.next();
            if (SvnUtils.isParentOrEqual(root, file)) {
                for (Iterator j = exclusions.iterator(); j.hasNext();) {
                    File excluded = (File) j.next();
                    if (SvnUtils.isParentOrEqual(excluded, file)) {
                        continue outter;
                    }
                }
                return true;
            }
        }
        return false;
    }

    public static final List<File> getEmptyList() {
        // XXX
        return Collections.emptyList();
    }
}
