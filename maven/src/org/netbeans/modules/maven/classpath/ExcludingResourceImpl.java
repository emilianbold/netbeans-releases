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

package org.netbeans.modules.maven.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.maven.model.Resource;
import org.netbeans.modules.maven.api.FileUtilities;
import org.netbeans.modules.maven.NbMavenProjectImpl;
import org.netbeans.modules.maven.api.NbMavenProject;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.support.PathResourceBase;
import org.netbeans.spi.project.support.ant.PathMatcher;
import org.openide.ErrorManager;
import org.openide.filesystems.FileUtil;
import org.openide.util.WeakListeners;

/**
 *
 * @author mkleint
 */
public class ExcludingResourceImpl extends PathResourceBase 
       implements FilteringPathResourceImplementation, PropertyChangeListener {

    private NbMavenProjectImpl project;
    private URL[] cachedRoots;
    private HashMap<URL, PathMatcher> matchers;
    private boolean test;

    //for tests only..
    protected ExcludingResourceImpl(boolean test) {
        this.test = test;
        matchers = new HashMap<URL, PathMatcher>();
    }
    
    public ExcludingResourceImpl(NbMavenProjectImpl project, boolean test) {
        this(test);
        this.project = project;
        NbMavenProject watch = project.getLookup().lookup(NbMavenProject.class);
        watch.addPropertyChangeListener(WeakListeners.propertyChange(this, watch));
    }
    
    public synchronized URL[] getRoots() {
        if (cachedRoots != null) {
            return cachedRoots;
        }
        URL[] urls = calculateRoots();
        cachedRoots = urls;
        return urls;
    }

    public ClassPathImplementation getContent() {
        return null;
    }

    public synchronized boolean includes(URL root, String resource) {
        PathMatcher match = matchers.get(root);
        assert match != null : "No PathMatcher for " + root;
        return match.matches(resource, true);
    }
    
    //protected for tests usage
    protected List<Resource> getResources(boolean istest) {
        return istest ? project.getOriginalMavenProject().getTestResources() : 
                          project.getOriginalMavenProject().getResources();
    }
    
    //protected for tests usage
    protected File getBase() {
        return FileUtil.toFile(project.getProjectDirectory());
    }

    private URL[] calculateRoots() {
        assert Thread.holdsLock(this);
        List<URL> newurls = new ArrayList<URL>();
        Map<URL, String> includes = new HashMap<URL, String>();
        Map<URL, String> excludes = new HashMap<URL, String>();
        List<Resource> lst = getResources(test);
        for (Resource res : lst) {
            URI uri = FileUtilities.getDirURI(getBase(), res.getDirectory());
            try {
                URL entry;
                //TODO what are all the extensions that get into classpath??
                // resources should be primarily non-jar anyway..
                if (uri.toString().toLowerCase().endsWith(".jar")  //NOI18N
                 || uri.toString().toLowerCase().endsWith(".ejb3")) {//NOI18N
                    entry = FileUtil.getArchiveRoot(uri.toURL());
                } else {
                    entry = uri.toURL();
                    if  (!entry.toExternalForm().endsWith("/")) { //NOI18N
                        entry = new URL(entry.toExternalForm() + "/"); //NOI18N
                    }
                }
                if (entry != null) {
                    if (!newurls.contains(entry)) {
                        newurls.add(entry);
                    }
                    processInEx(includes, entry, res.getIncludes());
                    processInEx(excludes, entry, res.getExcludes());
                }
            } catch (MalformedURLException mue) {
                ErrorManager.getDefault().notify(mue);
            }
        }
        matchers.clear();
        for (URL u : newurls) {
            String in = includes.get(u);
            String ex = excludes.get(u);
            matchers.put(u, new PathMatcher(in, ex, new File(u.toExternalForm())));
        }
        cachedRoots = newurls.toArray(new URL[0]);
        return cachedRoots;
    }

    public void propertyChange(PropertyChangeEvent evt) {
        if (NbMavenProjectImpl.PROP_PROJECT.equals(evt.getPropertyName())) {
            //TODO optimize somehow? it's just too much work to figure if something changed..
             firePropertyChange(PROP_ROOTS, null, null);
//             super.firePropertyChange(this.PROP_INCLUDES, null, null);
        }
    }

    private void processInEx(Map<URL, String> cludes, URL entry, List res) {
        String clude = cludes.get(entry);
        if (clude == null) {
            clude = "";
        } else {
            clude = clude + ","; // PathMatcher assumes this as delimiter
        }
        if (res != null && res.size() > 0) {
            for (Object incl : res) {
                clude = clude + incl + ",";
            }
            if (clude.endsWith(",")) {
                clude.substring(0, clude.length() - 1);
            }
        } else {
//            clude = clude + "**";
        }
        if (clude.length() == 0) {
            clude = null;
        }
        cludes.put(entry, clude);
    }

}
