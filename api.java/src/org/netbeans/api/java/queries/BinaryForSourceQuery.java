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
package org.netbeans.api.java.queries;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.spi.java.queries.BinaryForSourceQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.URLMapper;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 *
 * The query is used for finding binaries for sources,
 * this is intended to be the inverse of the SourceForBinaryQuery.
 * @see BinaryForSourceQueryImplementation
 * @see SourceForBinaryQuery
 * @since org.netbeans.api.java/1 1.12
 * @author Tomas Zezula
 * 
 */
public final class BinaryForSourceQuery {
    
    
    /** Creates a new instance of BInaryForSOurceQuery */
    private BinaryForSourceQuery() {
    }
    
    /**
     * Returns the binary root for given source root.
     * @param sourceRoot the source path root.
     * @return a result object encapsulating the answer (never null)
     */
    public static Result findBinaryRoots (final URL sourceRoot) {
       assert sourceRoot != null;
       for (BinaryForSourceQueryImplementation impl : Lookup.getDefault().lookupAll(BinaryForSourceQueryImplementation.class)) {
           BinaryForSourceQuery.Result result = impl.findBinaryRoots (sourceRoot);
           if (result != null) {
               return result;
           }
       }
       return new DefaultResult (sourceRoot);
    }
    
    /**
     * Result of finding binaries, encapsulating the answer as well as the
     * ability to listen to it.
     */
    public static interface Result {
        
        /**
         * Get the binary roots.         
         * @return array of roots of compiled classes (may be empty but not null)
         */
        URL[] getRoots();
        
        /**
         * Add a listener to changes in the roots.
         * @param l a listener to add
         */
        void addChangeListener(ChangeListener l);
        
        /**
         * Remove a listener to changes in the roots.
         * @param l a listener to remove
         */
        void removeChangeListener(ChangeListener l);
    }        
    
    private static class DefaultResult implements Result {
        
        private final URL sourceRoot;
        
        DefaultResult (final URL sourceRoot) {
            this.sourceRoot = sourceRoot;
        }
    
        public URL[] getRoots() {
            FileObject fo = URLMapper.findFileObject(sourceRoot);
            if (fo == null) {
                return new URL[0];
            }
            ClassPath exec = ClassPath.getClassPath(fo, ClassPath.EXECUTE);
            if (exec == null) {
                return new URL[0];
            }           
            Set<URL> result = new HashSet<URL>();
            for (ClassPath.Entry e : exec.entries()) {
                FileObject[] roots = SourceForBinaryQuery.findSourceRoots(e.getURL()).getRoots();
                for (FileObject root : roots) {
                    try {
                        if (sourceRoot.equals (root.getURL())) {
                            result.add (e.getURL());
                        }
                    } catch (FileStateInvalidException fsie) {
                        Exceptions.printStackTrace(fsie);
                    }
                }
            }
            return result.toArray(new URL[result.size()]);
        }

        public void addChangeListener(ChangeListener l) {            
        }

        public void removeChangeListener(ChangeListener l) {            
        }
    }
}
