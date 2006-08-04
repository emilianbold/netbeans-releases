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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
            
            public synchronized URL[] getRoots() {
                URL [] urls = cache;
                if (urls == null) {
                    cache = new URL[0];
                    urls = createRoots();
                    synchronized (lock) {
                        if (cache != null) cache = urls;
                    }
                }
                return urls;
            }
            
            private URL[] createRoots() {
                final ArrayList<URL> roots = new ArrayList<URL>();
                final URL projectRoot = URLMapper.findURL(helper.getProjectDirectory(), URLMapper.EXTERNAL);
                if (J2MEProjectUtils.isParentOf(projectRoot, binaryRoot)) {
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
