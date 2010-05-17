/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.versioning.system.cvss.util;

import org.netbeans.modules.versioning.spi.VersioningSupport;

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

    public static final Context Empty = new Context(Collections.EMPTY_SET, Collections.EMPTY_SET, Collections.EMPTY_SET);
    
    private static final long serialVersionUID = 1L;
    
    private final Set filteredFiles;
    private final Set rootFiles;
    private final Set exclusions;

    public Context(Set filteredFiles, Set rootFiles, Set exclusions) {
        this.filteredFiles = filteredFiles;
        this.rootFiles = rootFiles;
        this.exclusions = exclusions;
        while (normalize());
    }

    private boolean normalize() {
        for (Iterator i = rootFiles.iterator(); i.hasNext();) {
            File root = (File) i.next();
            for (Iterator j = exclusions.iterator(); j.hasNext();) {
                File exclusion = (File) j.next();
                if (Utils.isParentOrEqual(exclusion, root)) {
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

    private void removeDuplicates(Set files) {
        List newFiles = new ArrayList();
        outter: for (Iterator i = files.iterator(); i.hasNext();) {
            File file = (File) i.next();
            for (Iterator j = newFiles.iterator(); j.hasNext();) {
                File includedFile = (File) j.next();
                if (Utils.isParentOrEqual(includedFile, file) && (file.isFile() || !VersioningSupport.isFlat(includedFile))) continue outter;
                if (Utils.isParentOrEqual(file, includedFile) && (includedFile.isFile() || !VersioningSupport.isFlat(file))) {
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
            if (!Utils.isParentOrEqual(root, child)) {
                exclusions.add(child);
            }
        }
    }

    public Set<File> getExclusions() {
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
        return (File[]) filteredFiles.toArray(new File[filteredFiles.size()]);
    }

    public File[] getRootFiles() {
        return (File[]) rootFiles.toArray(new File[rootFiles.size()]);
    }
    
    public boolean contains(File file) {
        outter : for (Iterator i = rootFiles.iterator(); i.hasNext();) {
            File root = (File) i.next();
            if (Utils.isParentOrEqual(root, file)) {
                for (Iterator j = exclusions.iterator(); j.hasNext();) {
                    File excluded = (File) j.next();
                    if (Utils.isParentOrEqual(excluded, file)) {
                        continue outter;
                    }
                }
                return true;
            }
        }
        return false;
    }

    /**
     * Joins this context and the parameter context.
     * 
     * @param ctx
     * @return
     */
    public Context union(Context ctx) {
        Set<File> newfilteredFiles = new HashSet<File>(filteredFiles);
        Set<File> newrootFiles = new HashSet<File>(rootFiles);
        Set<File> newexclusions = new HashSet<File>(exclusions);
        
        // TODO: postprocess filetered file list? it may be larger than necessary  
        newfilteredFiles.addAll(ctx.filteredFiles);
        newrootFiles.addAll(ctx.rootFiles);
        newexclusions.addAll(ctx.exclusions);
        
        Context nc = new Context(newfilteredFiles, newrootFiles, newexclusions);
        return nc;
    }
}
