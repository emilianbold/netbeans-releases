/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2015 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2015 Sun Microsystems, Inc.
 */
package org.netbeans.modules.java.api.common.classpath;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.RequiresTree;
import com.sun.source.util.TreeScanner;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;

/**
 *
 * @author Tomas Zezula
 */
final class ModuleClassPathImplementation  implements ClassPathImplementation, PropertyChangeListener {

    private static final String PLATFORM_ACTIVE = "platform.active"; // NOI18N
    private static final String MODULE_INFO = "module-info.java";   //NOI18N
    private static final String PLATFORM_ANT_NAME = "platform.ant.name";    //NOI18N
    private static final String PROTOCOL_NBJRT = "nbjrt";   //NOI18N

    private final SourceRoots sources;
    private final PropertyEvaluator eval;
    private final PropertyChangeSupport listeners;
    private final AtomicReference<List<PathResourceImplementation>> cache;
    private final ThreadLocal<List<PathResourceImplementation>> pastNaGibona;

    ModuleClassPathImplementation(
            @NonNull final SourceRoots sources,
            @NonNull final PropertyEvaluator eval) {
        Parameters.notNull("sources", sources); //NOI18N
        Parameters.notNull("eval", eval);   //NOI18N
        this.sources = sources;
        this.eval = eval;
        this.listeners = new PropertyChangeSupport(this);
        this.cache = new AtomicReference<>();
        this.pastNaGibona = new ThreadLocal<>();
        this.sources.addPropertyChangeListener(WeakListeners.propertyChange(this, this.sources));
        this.eval.addPropertyChangeListener(WeakListeners.propertyChange(this, this.eval));
    }

    @Override
    @NonNull
    public List<? extends PathResourceImplementation> getResources() {
        List<PathResourceImplementation> res = cache.get();
        if (res == null) {
            List<? extends PathResourceImplementation> bestSoFar = pastNaGibona.get();
            if (bestSoFar != null) {
                return bestSoFar;
            }
            res = getAllModules();
            pastNaGibona.set(res);
            try {
                for (FileObject root : sources.getRoots()) {
                    final FileObject modules = root.getFileObject(MODULE_INFO);
                    if (modules != null) {
                        final JavaSource src = JavaSource.forFileObject(modules);
                        if (src != null) {
                            try {
                                final List<List<PathResourceImplementation>> resInOut = new ArrayList<>(1);
                                resInOut.add(res);
                                src.runUserActionTask(new Task<CompilationController>() {
                                    @Override
                                    public void run(CompilationController cc) throws Exception {
                                        cc.toPhase(JavaSource.Phase.ELEMENTS_RESOLVED);
                                        final CompilationUnitTree cu = cc.getCompilationUnit();
                                        final Set<String> requires = new HashSet<>();
                                        cu.accept(new TreeScanner<Void, Collection<? super String>>() {
                                            @Override
                                            public Void visitRequires(RequiresTree node, Collection<? super String> p) {
                                                p.add(node.getModuleName().toString());
                                                return super.visitRequires(node, p);
                                            }
                                        }, requires);
                                        resInOut.set(0, filterModules(resInOut.get(0), requires));
                                    }
                                }, true);
                                res = resInOut.get(0);
                            } catch (IOException ioe) {
                                Exceptions.printStackTrace(ioe);
                            }
                        }
                    }
                }
            } finally {
                pastNaGibona.remove();
            }
            assert res != null;
            if (!cache.compareAndSet(null, res)) {
                res = cache.get();
            }
            this.listeners.firePropertyChange(PROP_RESOURCES, null, null);
        }
        return res;
    }

    @Override
    public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        this.listeners.addPropertyChangeListener(listener);
    }

    @Override
    public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
        Parameters.notNull("listener", listener);   //NOI18N
        this.listeners.removePropertyChangeListener(listener);
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        final String propName = evt.getPropertyName();
        if (propName == null || PLATFORM_ACTIVE.equals(propName) || SourceRoots.PROP_ROOTS.equals(propName)) {
            reset();
        }
    }

    private void reset() {
        this.cache.set(null);
        this.listeners.firePropertyChange(PROP_RESOURCES, null, null);
    }

    private List<PathResourceImplementation> getAllModules() {
        final List<PathResourceImplementation> res = new ArrayList<>();
        final String platformName = eval.getProperty(PLATFORM_ACTIVE);
        if (platformName != null && !platformName.isEmpty()) {
            for (JavaPlatform plat : JavaPlatformManager.getDefault().getInstalledPlatforms()) {
                if (platformName.equals(plat.getProperties().get(PLATFORM_ANT_NAME))) {
                    for (ClassPath.Entry entry : plat.getBootstrapLibraries().entries()) {
                        final URL root = entry.getURL();
                        if (PROTOCOL_NBJRT.equals(root.getProtocol())) {
                            res.add(ClassPathSupport.createResource(root));
                        }
                    }
                }
            }
        }
        return res;
    }

    @NonNull
    private static List<PathResourceImplementation> filterModules(
            @NonNull List<PathResourceImplementation> modules,
            @NonNull Set<String> requires) {
        final List<PathResourceImplementation> res = new ArrayList<>(modules.size());
        for (PathResourceImplementation pr : modules) {
            for (URL url : pr.getRoots()) {
                if (requires.contains(getModuleName(url))) {
                    res.add(pr);
                }
            }
        }
        return res;
    }

    @NonNull
    private static String getModuleName(@NonNull final URL url) {
        final String path = url.getPath();
        final int start = path.lastIndexOf('/', path.length()-2);   //NOI18N
        return path.substring(start+1, path.length()-1);
    }

}
