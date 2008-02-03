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
package org.netbeans.modules.java.j2seproject.queries;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import java.net.URL;
import java.net.MalformedURLException;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.URLMapper;
import org.openide.util.ChangeSupport;

/**
 * Finds sources corresponding to binaries in a J2SE project.
 * @author Jesse Glick, Tomas Zezula
 */
public class CompiledSourceForBinaryQuery implements SourceForBinaryQueryImplementation {
    
    private static final String PROP_BUILD_DIR = "build.dir";   //NOI18N
    
    private final AntProjectHelper helper;
    private final PropertyEvaluator evaluator;
    private final SourceRoots sourceRoots;
    private final SourceRoots testRoots;
    private Map<URL,SourceForBinaryQuery.Result>  cache = new HashMap<URL,SourceForBinaryQuery.Result>();

    public CompiledSourceForBinaryQuery(AntProjectHelper helper, PropertyEvaluator evaluator, SourceRoots srcRoots, SourceRoots testRoots) {
        this.helper = helper;
        this.evaluator = evaluator;
        this.sourceRoots = srcRoots;
        this.testRoots = testRoots;
    }

    public SourceForBinaryQuery.Result findSourceRoots(URL binaryRoot) {
        if (FileUtil.getArchiveFile(binaryRoot) != null) {
            binaryRoot = FileUtil.getArchiveFile(binaryRoot);
            // XXX check whether this is really the root
        }
        SourceForBinaryQuery.Result res = cache.get(binaryRoot);
        if (res != null) {
            return res;
        }
        SourceRoots src = null;
        if (hasSources(binaryRoot,"build.classes.dir")) {   //NOI18N
            src = this.sourceRoots;
        }
        else if (hasSources (binaryRoot,"dist.jar")) {      //NOI18N
            src = this.sourceRoots;
        }
        else if (hasSources (binaryRoot,"build.test.classes.dir")) {    //NOI18N
            src = this.testRoots;
        }
        if (src == null) {
            return null;
        }
        else {
            res = new Result (src);
            cache.put (binaryRoot, res);
            return res;
        }
    }


    private boolean hasSources (URL binaryRoot, String binaryProperty) {
        try {
            String outDir = evaluator.getProperty(binaryProperty);
            if (outDir != null) {
                File f = helper.resolveFile (outDir);
                URL url = f.toURI().toURL();
                if (!f.exists() && !f.getPath().toLowerCase().endsWith(".jar")) { // NOI18N
                    // non-existing 
                    assert !url.toExternalForm().endsWith("/") : f; // NOI18N
                    url = new URL(url.toExternalForm() + "/"); // NOI18N
                }
                if (url.equals (binaryRoot)) {
                    return true;
                }
            }
        } catch (MalformedURLException malformedURL) {
            ErrorManager.getDefault().notify(malformedURL);
        }
        return false;
    }
    
    private class Result implements SourceForBinaryQuery.Result, PropertyChangeListener {

        private final ChangeSupport changeSupport = new ChangeSupport(this);
        private SourceRoots sourceRoots;

        public Result (SourceRoots sourceRoots) {
            this.sourceRoots = sourceRoots;
            this.sourceRoots.addPropertyChangeListener(this);
        }
        
        public FileObject[] getRoots () {
            //todo: May need to cache the result
            List<FileObject> result = new ArrayList<FileObject>();
            result.addAll(Arrays.asList(this.sourceRoots.getRoots()));            
            try {
                String buildDir = evaluator.getProperty(PROP_BUILD_DIR);
                if (buildDir != null) {
                    // generated/wsclient
                    File f =  new File (helper.resolveFile (buildDir),"generated/wsclient"); //NOI18N
                    URL url = f.toURI().toURL();
                    if (!f.exists()) {  //NOI18N
                        assert !url.toExternalForm().endsWith("/");  //NOI18N
                        url = new URL (url.toExternalForm()+'/');   //NOI18N
                    }
                    FileObject root = URLMapper.findFileObject(url);
                    if (root != null) {
                        result.add(root);
                    }

                    // generated/wsimport/client
                    f = new File (helper.resolveFile(buildDir),"generated/wsimport/client"); //NOI18N
                    url = f.toURI().toURL();
                    if (!f.exists()) {  //NOI18N
                        assert !url.toExternalForm().endsWith("/");  //NOI18N
                        url = new URL (url.toExternalForm()+'/');   //NOI18N
                    }
                    root = URLMapper.findFileObject(url);
                    if (root != null) {
                        result.add(root);
                    }

                    // generated/addons/jaxb
                    f =  new File (helper.resolveFile (buildDir),"generated/addons/jaxb"); //NOI18N
                    url = f.toURI().toURL();
                    if (!f.exists()) {  
                        assert !url.toExternalForm().endsWith("/");  //NOI18N
                        url = new URL (url.toExternalForm()+'/');   //NOI18N
                    }
                    root = URLMapper.findFileObject(url);
                    if (root != null) {
                        result.add(root);
                    }
                    
                }
            } catch (MalformedURLException ex) {
                ErrorManager.getDefault ().notify (ex);
            }
            return result.toArray(new FileObject[result.size()]);
        }
        
        public void addChangeListener (ChangeListener l) {
            changeSupport.addChangeListener(l);
        }
        
        public void removeChangeListener (ChangeListener l) {
            changeSupport.removeChangeListener(l);
        }

        public void propertyChange(PropertyChangeEvent evt) {
            if (SourceRoots.PROP_ROOTS.equals(evt.getPropertyName())) {
                this.changeSupport.fireChange ();
            }
        }

    }
    
}
