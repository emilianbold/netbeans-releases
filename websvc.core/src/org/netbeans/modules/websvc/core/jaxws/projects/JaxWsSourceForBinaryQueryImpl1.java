/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.websvc.core.jaxws.projects;

import java.io.File;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.jaxws.api.JAXWSSupport;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/** SourceForBinaryQueryImplementation for JAX-WS Services
 *
 * @author mkuchtiak
 */
public class JaxWsSourceForBinaryQueryImpl1 implements SourceForBinaryQueryImplementation {

    private final Map<URL, SourceForBinaryQuery.Result> cache = new HashMap<URL, SourceForBinaryQuery.Result>();
    
    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        File archiveFile = FileUtil.archiveOrDirForURL(binaryRoot);

        if (archiveFile != null) {
            SourceForBinaryQuery.Result result = cache.get(binaryRoot);
            if (result == null) {
                Project prj = FileOwnerQuery.getOwner(archiveFile.toURI());
                if (prj != null) {
                    JAXWSSupport jaxWSS = JAXWSSupport.getJAXWSSupport(prj.getProjectDirectory());
                    if (jaxWSS != null) {
                        result = new Result(prj);
                        cache.put(binaryRoot, result);
                    }
                }
            }         
            return result;
        }
        return null;
    }
    
    private class Result implements SourceForBinaryQuery.Result {

        private Project prj;

        public Result(Project prj) {
              this.prj = prj;
        }

        public FileObject[] getRoots() {
            FileObject fo = 
                prj.getProjectDirectory().getFileObject("build/generated/wsimport/service"); // NOI18N
            return fo == null ? new FileObject[]{} : new FileObject[]{fo};
        }

        public void addChangeListener (ChangeListener l) {
        }

        public void removeChangeListener (ChangeListener l) {
        }

    }
}
