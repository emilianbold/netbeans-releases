/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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
package org.netbeans.modules.gsfpath.api.queries;

import java.net.URL;
import java.util.HashSet;
import java.util.Set;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.gsfpath.api.classpath.ClassPath;
import org.netbeans.modules.gsfpath.spi.queries.BinaryForSourceQueryImplementation;
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
 * @since org.netbeans.modules.gsfpath.api/1 1.12
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
