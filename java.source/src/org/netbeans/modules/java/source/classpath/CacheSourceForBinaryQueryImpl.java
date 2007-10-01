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
package org.netbeans.modules.java.source.classpath;


import java.net.URL;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.java.source.usages.Index;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Lookup;

/**
 *
 * @author Tomas Zezula
 */
public class CacheSourceForBinaryQueryImpl implements SourceForBinaryQueryImplementation {

    private String FILE_PROTOCOL = "file";  //NOI18N
    
    /** Creates a new instance of CacheSourceForBinaryQueryImpl */
    public CacheSourceForBinaryQueryImpl() {
    }

    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        if (!FILE_PROTOCOL.equals (binaryRoot.getProtocol())) {
            return null;
        }
        URL sourceURL = Index.getSourceRootForClassFolder(binaryRoot);
        SourceForBinaryQuery.Result result = null;
        if (sourceURL != null) {            
            for ( SourceForBinaryQueryImplementation impl :Lookup.getDefault().lookupAll(SourceForBinaryQueryImplementation.class)) {
                if (impl != this) {
                    result = impl.findSourceRoots(sourceURL);
                    if (result != null) {
                        break;
                    }
                }
            }
            if (result == null) {
                result = new R (sourceURL);
            }
        }        
        return result;
    }
    
    private static class R implements SourceForBinaryQuery.Result {
        
        private final FileObject sourceRoot;
        
        public R (final URL sourceRootURL) {
            assert sourceRootURL != null;
            this.sourceRoot = URLMapper.findFileObject(sourceRootURL);
        }

        public void removeChangeListener(ChangeListener l) {
            //Imutable, not needed
        }

        public void addChangeListener(ChangeListener l) {
            //Imutable, not needed
        }

        public FileObject[] getRoots() {
            if (this.sourceRoot == null) {
                return new FileObject[0];
            }
            else {
                return new FileObject[] {this.sourceRoot};
            }
        }                
    }            
}
