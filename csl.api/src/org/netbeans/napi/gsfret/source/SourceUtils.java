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

package org.netbeans.napi.gsfret.source;

import java.net.URL;
import java.util.*;

import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.modules.csl.source.usages.RepositoryUpdater;


/**
 *
 * @author Dusan Balek
 */
public class SourceUtils {    
     
    private SourceUtils() {}
    
    
    /**
     * Tests whether the initial scan is in progress.
     */
    public static boolean isScanInProgress () {
        return RepositoryUpdater.getDefault().isScanInProgress();
    }

    /**
     * Waits for the end of the initial scan, this helper method 
     * is designed for tests which require to wait for end of initial scan.
     * @throws InterruptedException is thrown when the waiting thread is interrupted.
     */
    public static void waitScanFinished () throws InterruptedException {
        RepositoryUpdater.getDefault().waitScanFinished();
    }
    
    
    /**
     * Returns the dependent source path roots for given source root.
     * It returns all the open project source roots which have either
     * direct or transitive dependency on the given source root.
     * @param root to find the dependent roots for
     * @return {@link Set} of {@link URL}s containinig at least the
     * incomming root, never returns null.
     * @since 0.10
     */
    public static Set<URL> getDependentRoots (final URL root) {
        final Map<URL, List<URL>> deps = RepositoryUpdater.getDefault().getDependencies ();
        return getDependentRootsImpl (root, deps);
    }
    
    
    static Set<URL> getDependentRootsImpl (final URL root, final Map<URL, List<URL>> deps) {
        //Create inverse dependencies
        final Map<URL, List<URL>> inverseDeps = new HashMap<URL, List<URL>> ();
        for (Map.Entry<URL,List<URL>> entry : deps.entrySet()) {
            final URL u1 = entry.getKey();
            final List<URL> l1 = entry.getValue();
            for (URL u2 : l1) {
                List<URL> l2 = inverseDeps.get(u2);
                if (l2 == null) {
                    l2 = new ArrayList<URL>();
                    inverseDeps.put (u2,l2);
                }
                l2.add (u1);
            }
        }
        //Collect dependencies
        final Set<URL> result = new HashSet<URL>();
        final LinkedList<URL> todo = new LinkedList<URL> ();
        todo.add (root);
        while (!todo.isEmpty()) {
            final URL u = todo.removeFirst();
            if (!result.contains(u)) {
                result.add (u);
                final List<URL> ideps = inverseDeps.get(u);
                if (ideps != null) {
                    todo.addAll (ideps);
                }
            }
        }
        //Filter non opened projects
        Set<ClassPath> cps = GlobalPathRegistry.getDefault().getPaths(ClassPath.SOURCE);
        Set<URL> toRetain = new HashSet<URL>();
        for (ClassPath cp : cps) {
            for (ClassPath.Entry e : cp.entries()) {
                toRetain.add(e.getURL());
            }
        }
        result.retainAll(toRetain);
        return result;
    }    
}
