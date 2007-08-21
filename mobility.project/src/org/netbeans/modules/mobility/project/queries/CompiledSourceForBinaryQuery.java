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
import org.netbeans.spi.java.queries.SourceForBinaryQueryImplementation;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import java.net.URL;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import org.netbeans.api.java.queries.SourceForBinaryQuery;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.mobility.project.J2MEProjectUtils;
import org.netbeans.modules.mobility.project.J2MEProject;
import org.netbeans.modules.mobility.project.ui.CyclicDependencyWarningPanel;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.AntProjectEvent;
import org.openide.filesystems.URLMapper;
import org.openide.util.WeakListeners;

import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import org.openide.util.RequestProcessor;

public class CompiledSourceForBinaryQuery implements SourceForBinaryQueryImplementation {
    
    protected J2MEProject project;
    protected final Set<Thread> threads;    
    protected final AntProjectHelper helper;
    
    public CompiledSourceForBinaryQuery(J2MEProject project, AntProjectHelper helper) {
        this.project = project;
        this.helper = helper;
        this.threads = Collections.synchronizedSet(new HashSet<Thread>());
    }
    
    public SourceForBinaryQuery.Result findSourceRoots(final URL binaryRoot) {
        
        class R implements SourceForBinaryQuery.Result, AntProjectListener {
            
            ArrayList<ChangeListener> listeners = new ArrayList<ChangeListener>();
            private FileObject[] cache = null;
            final private AntProjectListener antProjectListener;
            final private transient Object lock = new Object();
            
            public R(AntProjectHelper helper) {
                antProjectListener = WeakListeners.create(AntProjectListener.class,
                                                          this, helper);
                helper.addAntProjectListener(antProjectListener);
            }
            
            public FileObject[] getRoots() {
                FileObject[] fo = cache;
                if (fo == null) {
                    fo = createRoots();
                    synchronized (lock) {
                        if (cache != null) cache = fo;
                    }
                }
                return fo;
            }
            
            private FileObject[] createRoots() {
                final ArrayList<FileObject> roots = new ArrayList<FileObject>();
                try {
                    final URL projectRoot = URLMapper.findURL(helper.getProjectDirectory(), URLMapper.EXTERNAL);
                    URL distRoot = helper.resolveFile("dist").toURI().toURL(); //NOI18N
                    URL buildRoot = helper.resolveFile("build").toURI().toURL(); //NOI18N
                    if (J2MEProjectUtils.isParentOf(distRoot, binaryRoot) || J2MEProjectUtils.isParentOf(buildRoot, binaryRoot)) {
                        final String srcPath = helper.getStandardPropertyEvaluator().getProperty("src.dir"); //NOI18N
                        final FileObject src = srcPath == null ? null : helper.resolveFileObject(srcPath);
                        if (src != null) roots.add(src);
                        final String cfg = J2MEProjectUtils.detectConfiguration(projectRoot, binaryRoot);
                        String path = J2MEProjectUtils.evaluateProperty(helper, "libs.classpath", cfg); //NOI18N
                        if (path != null) path = helper.resolvePath(path);
                        if (path != null) {
                            final String p[] = PropertyUtils.tokenizePath(path);
                            for (int i=0; i<p.length; i++) try {
                                final URL url = J2MEProjectUtils.wrapJar(new File(p[i]).toURI().toURL());
                                if (url != null && !J2MEProjectUtils.isParentOf(projectRoot, url)) {
                                    if (threads.contains(Thread.currentThread())) {
                                        CyclicDependencyWarningPanel.showWarning(ProjectUtils.getInformation(project).getDisplayName());
                                        return new FileObject[0];
                                    }
                                    try {
                                        threads.add(Thread.currentThread());
                                        final SourceForBinaryQuery.Result result = SourceForBinaryQuery.findSourceRoots(url);
                                        if (result != null)
                                            roots.addAll(Arrays.asList(result.getRoots()));
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
                return roots.toArray(new FileObject[roots.size()]);
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
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        synchronized (listeners) {
                            final ChangeEvent event=new ChangeEvent(this);
                            for (int i = 0; i < listeners.size(); i++) {
                                final ChangeListener changeListener = listeners.get(i);
                                changeListener.stateChanged(event);
                            }
                        }
                    }
                });
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
        
        try {
            URL distRoot = helper.resolveFile("dist").toURI().toURL(); //NOI18N
            URL buildRoot = helper.resolveFile("build").toURI().toURL(); //NOI18N
            if (J2MEProjectUtils.isParentOf(distRoot, binaryRoot) || J2MEProjectUtils.isParentOf(buildRoot, binaryRoot)) 
                return new R(helper);
        } catch (MalformedURLException mue) {}
        return null;
    }
    
}
