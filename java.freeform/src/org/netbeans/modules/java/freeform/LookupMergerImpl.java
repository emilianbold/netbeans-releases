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

package org.netbeans.modules.java.freeform;

import java.util.Iterator;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.ant.freeform.spi.LookupMerger;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Merges ClassPathProviders.
 *
 * @author David Konecny
 */
public class LookupMergerImpl implements LookupMerger {

    public LookupMergerImpl() {}
    
    public Class[] getMergeableClasses() {
        return new Class[]{ClassPathProvider.class};
    }
    
    public Object merge(Lookup lookup, Class clazz) {
        if (clazz == ClassPathProvider.class) {
            return new ClassPathProviderImpl(lookup);
        }
        throw new IllegalArgumentException("merging of "+clazz+" is not supported"); // NOI18N
    }
    
    private static class ClassPathProviderImpl implements ClassPathProvider {
        
        private Lookup lkp;
        
        public ClassPathProviderImpl(Lookup lkp) {
            this.lkp = lkp;
        }
        
        public ClassPath findClassPath(FileObject file, String type) {
            Iterator it = lkp.lookup(new Lookup.Template(ClassPathProvider.class)).allInstances().iterator();
            while (it.hasNext()) {
                ClassPathProvider cpp = (ClassPathProvider)it.next();
                ClassPath cp = cpp.findClassPath(file, type);
                if (cp != null) {
                    return cp;
                }
            }
            return null;
        }
        
    }

}
