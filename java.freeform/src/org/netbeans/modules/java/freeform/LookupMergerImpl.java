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

package org.netbeans.modules.java.freeform;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.LookupMerger;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Merges ClassPathProviders.
 *
 * @author David Konecny
 */
public class LookupMergerImpl implements LookupMerger<ClassPathProvider> {

    public LookupMergerImpl() {}

    public Class<ClassPathProvider> getMergeableClass() {
        return ClassPathProvider.class;
    }

    public ClassPathProvider merge(Lookup lookup) {
        return new ClassPathProviderImpl(lookup);
    }
    
    private static class ClassPathProviderImpl implements ClassPathProvider {
        
        private Lookup lkp;
        
        public ClassPathProviderImpl(Lookup lkp) {
            this.lkp = lkp;
        }
        
        public ClassPath findClassPath(FileObject file, String type) {
            for (ClassPathProvider cpp : lkp.lookupAll(ClassPathProvider.class)) {
                ClassPath cp = cpp.findClassPath(file, type);
                if (cp != null) {
                    return cp;
                }
            }
            return null;
        }
        
    }

}
