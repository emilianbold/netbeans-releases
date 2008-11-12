/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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
package org.netbeans.modules.java.debug;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery.Result;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.URLMapper;
import org.openide.util.Mutex.Action;

/**
 *
 * @author Jan Lahoda
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation.class, position=1000)
public class SourceForBinaryQueryImpl implements SourceForBinaryQueryImplementation {

    private Map<URL,Reference<Result>> url2Result = new WeakHashMap<URL, Reference<Result>>();
    private Map<Result, URL> result2URL = new WeakHashMap<Result, URL>();
    
    public SourceForBinaryQueryImpl() {
    }
    
    public Result findSourceRoots(final URL binaryRoot) {
        return ProjectManager.mutex().readAccess(new Action<Result>() {
            public Result run() {
                return findSourceRootsImpl(binaryRoot);
            }
        });
    }

    private synchronized Result findSourceRootsImpl(URL binaryRoot) {
        Reference<Result> ref = url2Result.get(binaryRoot);
        Result r = ref != null ? ref.get() : null;

        if (r != null) {
            return r;
        }
        
        String binaryRootS = binaryRoot.toExternalForm();
        URL[] urls = null;
        if (binaryRootS.startsWith("jar:file:")) { // NOI18N
            if ((urls = checkForBinaryRoot(binaryRootS, "/libs.javacapi/external/javac-api")) == null) { // NOI18N
                urls = checkForBinaryRoot(binaryRootS, "/libs.javacimpl/external/javac-impl"); // NOI18N
            }
            final FileObject resultFO = urls != null ? URLMapper.findFileObject(urls[0]) : null;
            if (resultFO != null) {
                FileObject projectFO = urls != null ? URLMapper.findFileObject(urls[1]) : null;
                if (projectFO != null) {
                    try {
                        ProjectManager.getDefault().findProject(projectFO);
                    } catch (IOException ex) {
                        Logger.getLogger(SourceForBinaryQueryImpl.class.getName()).log(Level.FINE, null, ex);
                    } catch (IllegalArgumentException ex) {
                        Logger.getLogger(SourceForBinaryQueryImpl.class.getName()).log(Level.FINE, null, ex);
                    }
                }
                
                Result result = new Result() {
                    public FileObject[] getRoots() {
                        return new FileObject[]{resultFO};
                    }

                    public void addChangeListener(ChangeListener l) {
                    }

                    public void removeChangeListener(ChangeListener l) {
                    }
                };

                url2Result.put(binaryRoot, new WeakReference<Result>(result));
                result2URL.put(result, binaryRoot);

                return result;
            }
        }
        return null;
    }

    private URL[] checkForBinaryRoot(String ext, String prefix) {
        if (ext.endsWith(prefix + "-nb-7.0-b07.jar!/")) { // NOI18N
            try {
                String part = ext.substring("jar:".length(), ext.length() - prefix.length() - "-nb-7.0-b07.jar!/".length()); // NOI18N
                
                return new URL[] {
                    new URL(part + "/nb-javac/src/share/classes"), // NOI18N
                    new URL(part + "/nb-javac/make/netbeans/nb-javac")  // NOI18N
                };
            } catch (MalformedURLException ex) {
                Logger.getLogger("global").log(Level.INFO, null, ex); //NOI18N
            }
        }
        
        return null;
    }
    
}
