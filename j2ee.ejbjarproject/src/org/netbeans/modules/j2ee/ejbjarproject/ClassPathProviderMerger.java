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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2ee.ejbjarproject;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.project.LookupMerger;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Zezula
 */
public final class ClassPathProviderMerger implements LookupMerger<ClassPathProvider> {
    
    private final ClassPathProvider defaultProvider;
    
    public ClassPathProviderMerger (final ClassPathProvider defaultProvider) {
        assert defaultProvider != null;
        this.defaultProvider = defaultProvider;
    }

    public Class<ClassPathProvider> getMergeableClass() {
        return ClassPathProvider.class;
    }

    public ClassPathProvider merge(Lookup lookup) {
        return new CPProvider (lookup);
    }
    
    
    private class CPProvider implements ClassPathProvider {
        
        private final Lookup lookup;

        public CPProvider(final Lookup lookup) {
            assert lookup != null;
            this.lookup = lookup;
        }                

        public ClassPath findClassPath(FileObject file, String type) {
            ClassPath result = defaultProvider.findClassPath(file, type);
            if (result != null) {
                return result;
            }
            for (ClassPathProvider cpProvider : lookup.lookupAll(ClassPathProvider.class)) {
                result = cpProvider.findClassPath(file, type);
                if (result != null) {
                    return result;
                }
            }
            return null;
        }        
    }

}
