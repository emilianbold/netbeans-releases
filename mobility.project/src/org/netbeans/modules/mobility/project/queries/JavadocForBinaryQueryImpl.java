/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.mobility.project.queries;
import java.io.File;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.*;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;

import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.mobility.project.J2MEProjectUtils;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ui.CyclicDependencyWarningPanel;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.openide.ErrorManager;
import org.openide.filesystems.URLMapper;
import org.openide.util.WeakListeners;

/**
 * Finds Javadoc (if it is built) corresponding to binaries in J2SE project.
 * @author Adam Sotona
 */
public class JavadocForBinaryQueryImpl implements JavadocForBinaryQueryImplementation {
    
    protected J2MEProject project;
    protected final AntProjectHelper helper;
    protected final Set<Thread> threads;
    
    public JavadocForBinaryQueryImpl(J2MEProject project, AntProjectHelper helper) {
        this.project = project;
        this.helper = helper;
        this.threads = Collections.synchronizedSet(new HashSet<Thread>());
    }
    
    
    public JavadocForBinaryQuery.Result findJavadoc(final URL binaryRoot) {
        
        class R implements JavadocForBinaryQuery.Result, AntProjectListener {
            
            ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
            private URL[] cache = null;
            final private AntProjectListener antProjectListener;
            final private transient Object lock = new Object();
            
            public R(AntProjectHelper helper) {
                antProjectListener = WeakListeners.create(AntProjectListener.class,
                                                          this, helper);
                helper.addAntProjectListener(antProjectListener);
            }
            
            public URL[] getRoots() {
                URL [] urls = cache;
                if (urls == null) {
                    urls = createRoots();
                    synchronized (lock) {
                        if (cache != null) cache = urls;
                    }
                }
                return urls;
            }
            
            private URL[] createRoots() {
                final ArrayList<URL> roots = new ArrayList<URL>();
                try {
                    final URL projectRoot = URLMapper.findURL(helper.getProjectDirectory(), URLMapper.EXTERNAL);
                    URL distRoot = helper.resolveFile("dist").toURI().toURL(); //NOI18N
                    URL buildRoot = helper.resolveFile("build").toURI().toURL(); //NOI18N
                    if (J2MEProjectUtils.isParentOf(distRoot, binaryRoot) || J2MEProjectUtils.isParentOf(buildRoot, binaryRoot)) {
                        final String cfg = J2MEProjectUtils.detectConfiguration(projectRoot, binaryRoot);
                        if (cfg != null) try {
                            roots.add(J2MEProjectUtils.wrapJar(helper.resolveFile("dist/" + cfg + "/doc").toURI().toURL())); //NOI18N
                        } catch (MalformedURLException mue) {}
                        try {
                            roots.add(J2MEProjectUtils.wrapJar(helper.resolveFile("dist/doc").toURI().toURL())); //NOI18N
                        } catch (MalformedURLException mue) {}
                        String path = J2MEProjectUtils.evaluateProperty(helper, "libs.classpath", cfg); //NOI18N
                        if (path != null) path = helper.resolvePath(path);
                        if (path != null) {
                            final String p[] = PropertyUtils.tokenizePath(path);
                            for (int i=0; i<p.length; i++) try {
                                final URL url = J2MEProjectUtils.wrapJar(new File(p[i]).toURI().toURL());
                                if (url != null && !J2MEProjectUtils.isParentOf(projectRoot, url)) {
                                    if (threads.contains(Thread.currentThread())) {
                                        CyclicDependencyWarningPanel.showWarning(ProjectUtils.getInformation(project).getDisplayName());
                                        return new URL[0];
                                    }
                                    try {
                                        threads.add(Thread.currentThread());
                                        roots.addAll(Arrays.asList(JavadocForBinaryQuery.findJavadoc(url).getRoots()));
                                    } finally {
                                        threads.remove(Thread.currentThread());
                                    }
                                }
                            } catch (MalformedURLException mue) {
                            }
                        }
                    }
                } catch (MalformedURLException mue) {
                    ErrorManager.getDefault().notify(mue);
                }
                return roots.toArray(new URL[roots.size()]);
            }
            
            public void addChangeListener(ChangeListener l) {
                synchronized (listeners) {
                    listeners.add(l);
                }
            }
            public void removeChangeListener(ChangeListener l) {
                synchronized (listeners) {
                    listeners.remove(l);
                }
            }
            
            private void fireChanged() {
                synchronized (listeners) {
                    final ChangeEvent event=new ChangeEvent(this);
                    for (int i = 0; i < listeners.size(); i++) {
                        final ChangeListener changeListener = listeners.get(i);
                        changeListener.stateChanged(event);
                    }
                }
            }
            
            public void configurationXmlChanged(@SuppressWarnings("unused") AntProjectEvent event) {
            }
            
            public void propertiesChanged(@SuppressWarnings("unused") AntProjectEvent event) {
                synchronized (lock) {
                    cache = null;
                }
                fireChanged();
            }
        }
        
        return new R(helper);
    }
    
}
