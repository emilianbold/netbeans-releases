/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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
package org.netbeans.modules.versioning.spi;

import org.netbeans.modules.versioning.util.Utils;

import java.io.File;
import java.util.*;

/**
 * 
 * 
 * @author Maros Sandor
 */
public final class VCSContext {
    
    public static final VCSContext Empty = new VCSContext(emptySet(), emptySet() );

    static final long serialVersionUID = 1L;
    
    private final Set<File> rootFiles;
    private final Set<File> exclusions;

    public VCSContext(Set<File> rootFiles, Set<File> exclusions) {
        this.rootFiles = Collections.unmodifiableSet(new HashSet<File>(rootFiles));
        this.exclusions = Collections.unmodifiableSet(new HashSet<File>(exclusions));
    }

    public VCSContext(Set<File> rootFiles) {
        this.rootFiles = Collections.unmodifiableSet(new HashSet<File>(rootFiles));
        this.exclusions = emptySet();
    }

    public Set<File> getRootFiles() {
        return rootFiles;
    }

    public Set<File> getExclusions() {
        return exclusions;
    }

    public boolean contains(File file) {
        outter : for (File root : rootFiles) {
            if (Utils.isParentOrEqual(root, file)) {
                for (File excluded : exclusions) {
                    if (Utils.isParentOrEqual(excluded, file)) {
                        continue outter;
                    }
                }
                return true;
            }
        }
        return false;
    }
    
    private static final Set<File> emptySet() {
        return Collections.emptySet();
    }
}
