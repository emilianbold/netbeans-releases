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

package org.netbeans.modules.gsfpath.api.queries;

import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.gsfpath.spi.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;

/**
 * The query is used for finding sources for binaries.
 * The examples of usage of this query are:
 * <ul>
 * <li><p>finding source for library</p></li>
 * <li><p>finding src.zip for platform</p></li>
 * <li><p>finding source folder for compiled jar or build folder</p></li>
 * </ul>
 * @see SourceForBinaryQueryImplementation
 * @since org.netbeans.modules.gsfpath.api/1 1.4
 */
public class SourceForBinaryQuery {
    
    private static final Logger LOG = Logger.getLogger(SourceForBinaryQuery.class.getName());
    
    private static final Lookup.Result<? extends SourceForBinaryQueryImplementation> implementations =
        Lookup.getDefault().lookupResult (SourceForBinaryQueryImplementation.class);

    private SourceForBinaryQuery () {
    }

    /**
     * Returns the source root for given binary root (for example, src folder for jar file or build folder).
     * @param binaryRoot the ClassPath root of compiled files.
     * @return a result object encapsulating the answer (never null)
     */
    public static Result findSourceRoots (URL binaryRoot) {
        if (FileUtil.isArchiveFile(binaryRoot)) {
            throw new IllegalArgumentException("File URL pointing to " + // NOI18N
                "JAR is not valid classpath entry. Use jar: URL. Was: "+binaryRoot); // NOI18N
        }
        if (!binaryRoot.toExternalForm().endsWith("/")) {
            throw new IllegalArgumentException ("Folder URL must end with '/'. Was: "+binaryRoot);
        }
        for (SourceForBinaryQueryImplementation impl : implementations.allInstances()) {
            Result result = impl.findSourceRoots(binaryRoot);
            if (result != null) {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.log(Level.FINE, "findSourceRoots({0}) -> {1} from {2}", new Object[] {binaryRoot, Arrays.asList(result.getRoots()), impl});
                }
                return result;
            }
        }
        LOG.log(Level.FINE, "findSourceRoots({0}) -> nil", binaryRoot);
        return EMPTY_RESULT;
    }
    
    /**
     * Result of finding sources, encapsulating the answer as well as the
     * ability to listen to it.
     */
    public interface Result {
        
        /**
         * Get the source roots.         
         * @return array of roots of sources (may be empty but not null)
         */
        FileObject[] getRoots();
        
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
    
    private static final Result EMPTY_RESULT = new EmptyResult();
    private static final class EmptyResult implements Result {
        private static final FileObject[] NO_ROOTS = new FileObject[0];
        EmptyResult() {}
        public FileObject[] getRoots() {
            return NO_ROOTS;
        }
        public void addChangeListener(ChangeListener l) {}
        public void removeChangeListener(ChangeListener l) {}
    }    

}
