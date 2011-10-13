/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU General
 * Public License Version 2 only ("GPL") or the Common Development and
 * Distribution License("CDDL") (collectively, the "License"). You may not use
 * this file except in compliance with the License. You can obtain a copy of the
 * License at http://www.netbeans.org/cddl-gplv2.html or
 * nbbuild/licenses/CDDL-GPL-2-CP. See the License for the specific language
 * governing permissions and limitations under the License. When distributing
 * the software, include this License Header Notice in each file and include the
 * License file at nbbuild/licenses/CDDL-GPL-2-CP. Oracle designates this
 * particular file as subject to the "Classpath" exception as provided by Oracle
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * If you wish your version of this file to be governed by only the CDDL or only
 * the GPL Version 2, indicate your decision by adding "[Contributor] elects to
 * include this software in this distribution under the [CDDL or GPL Version 2]
 * license." If you do not indicate a single choice of license, a recipient has
 * the option to distribute your version of this file under either the CDDL, the
 * GPL Version 2 or to extend the choice of license to its licensees as provided
 * above. However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is made
 * subject to such option by the copyright holder.
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.platform;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.platform.Specification;
import org.netbeans.api.java.queries.JavadocForBinaryQuery;
import org.netbeans.api.java.queries.JavadocForBinaryQuery.Result;
import org.netbeans.modules.javafx2.platform.api.JavaFXPlatformUtils;
import org.netbeans.spi.java.project.support.JavadocAndSourceRootDetection;
import org.netbeans.spi.java.queries.JavadocForBinaryQueryImplementation;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.filesystems.URLMapper;
import org.openide.util.ChangeSupport;
import org.openide.util.Exceptions;
import org.openide.util.Parameters;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.ServiceProvider;

/**
 * JavadocForBinaryQuery implementation for JFX platform
 * @author Tomas Zezula
 */
@ServiceProvider(service=JavadocForBinaryQueryImplementation.class, position=11000)
public class JavaFXPlatformJavadoc implements JavadocForBinaryQueryImplementation, PropertyChangeListener {

    //@GuardedBy("this")
    private List<JavaFXSDK> sdks;
    //@GuaredBy("this")
    private volatile boolean sdksValid;
    //@GuaredBy("this")
    private PropertyEvaluator evaluator;

    public JavaFXPlatformJavadoc() {
        final JavaPlatformManager jpm = JavaPlatformManager.getDefault();
        jpm.addPropertyChangeListener(WeakListeners.propertyChange(this, jpm));
    }



    @Override
    public Result findJavadoc(@NonNull final URL binaryRoot) {
        Parameters.notNull("binaryRoot", binaryRoot);   //NOI18N
        for (JavaFXSDK sdk : getSdks()) {
            if (sdk.getRuntime().contains(binaryRoot)) {
                return sdk.createJavadocResult();
            }
        }
        return null;
    }

    @Override
    public void propertyChange(@NonNull final PropertyChangeEvent event) {
        if (JavaPlatformManager.PROP_INSTALLED_PLATFORMS.equals(event.getPropertyName())) {
            sdksValid = false;
        }
    }

    private Iterable<? extends JavaFXSDK> getSdks() {
        synchronized (this) {
            if (sdksValid) {
                assert sdks != null;
                return sdks;
            }
        }
        final PropertyEvaluator eval = getEvaluator();
        final JavaPlatform[] platforms = JavaPlatformManager.getDefault().getPlatforms(
                null,
                new Specification(
                    "j2se", //NOI18N
                    null));
        synchronized (this) {
            if (!sdksValid) {
                if (sdks == null) {
                    sdks = new ArrayList<JavaFXSDK>(platforms.length);
                    for (JavaPlatform jp : platforms) {
                        sdks.add(JavaFXSDK.forJavaPlatform(eval, jp));
                    }
                } else {
                    final HashMap<String,JavaPlatform> n2p = new HashMap<String,JavaPlatform>();
                    for (JavaPlatform jp : platforms) {
                        n2p.put(jp.getProperties().get(JavaFXPlatformUtils.PLATFORM_ANT_NAME), jp);
                    }
                    final HashMap<String,JavaFXSDK> n2s = new HashMap<String, JavaFXSDK>();
                    for (JavaFXSDK sdk : sdks) {
                        n2s.put(sdk.getAntName(), sdk);
                    }
                    final HashMap<String,JavaPlatform> toAdd = new HashMap<String, JavaPlatform>(n2p);
                    toAdd.keySet().removeAll(n2s.keySet());
                    n2s.keySet().removeAll(n2p.keySet());
                    sdks.removeAll(n2s.values());
                    for (JavaPlatform jp : toAdd.values()) {
                        sdks.add(JavaFXSDK.forJavaPlatform(eval, jp));
                    }
                }
                sdksValid = true;
            }
            return sdks;
        }
    }

    private synchronized PropertyEvaluator getEvaluator() {
        if (evaluator == null) {
            evaluator = PropertyUtils.sequentialPropertyEvaluator(
                PropertyUtils.globalPropertyProvider());
        }
        return evaluator;
    }

    private static final class JavaFXSDK implements PropertyChangeListener {

        public static final String PROP_RUNTIME = "runtime";    //NOI18N
        public static final String PROP_JAVADOC = "javadoc";    //NOI18N

        private final PropertyEvaluator eval;
        private final PropertyChangeSupport support;
        private final String rtPropName;
        private final String jdocPropName;
        private final String antName;
        private final AtomicReference<Collection<URL>> rt;
        private final AtomicReference<Collection<URL>> jdoc;
        //@GuaredBy("this")
        private ResultImpl jdocResult;

        private JavaFXSDK(
            @NonNull final PropertyEvaluator eval,
            @NonNull final String antName,
            @NonNull final String rtPropName,
            @NonNull final String jdocPropName) {
            Parameters.notNull("eval", eval);   //NOI18N
            Parameters.notNull("antName", antName); //NOI18N
            Parameters.notNull("rtPropName", rtPropName);   //NOI18N
            Parameters.notNull("jdocPropName", jdocPropName);   //NOI18N
            this.eval = eval;
            this.antName = antName;
            this.rtPropName = rtPropName;
            this.jdocPropName = jdocPropName;
            this.rt = new AtomicReference<Collection<URL>>();
            this.jdoc = new AtomicReference<Collection<URL>>();
            this.support = new PropertyChangeSupport(this);
            this.eval.addPropertyChangeListener(WeakListeners.propertyChange(this, this.eval));
        }

        @NonNull
        Collection<? extends URL> getRuntime() {
            Collection<URL> res = rt.get();
            if (res == null) {
                res = new HashSet<URL>();
                //xxx: Don't use JavaFXPlatformUtils.getJavaFXClassPath() as it's an nonsense.
                final String val = eval.getProperty(rtPropName);
                if (val != null) {
                    res.addAll(Utils.getRuntimeClassPath(new File(val)));
                }
                rt.set(res);
            }
            return res;
        }

        @NonNull
        Collection<? extends URL> getJavadoc() {
            Collection<URL> res = jdoc.get();
            if (res == null) {
                res = new ArrayList<URL>();
                final String val = eval.getProperty(jdocPropName);
                if (val != null) {
                    for (final String path : PropertyUtils.tokenizePath(val)) {
                        final URL root = FileUtil.urlForArchiveOrDir(new File(path));
                        if (root != null) {
                            final FileObject rootFo = JavadocAndSourceRootDetection.findJavadocRoot(
                                    URLMapper.findFileObject(root));
                            if (rootFo != null) {
                                try {
                                    res.add(rootFo.getURL());
                                } catch (FileStateInvalidException ex) {
                                    Exceptions.printStackTrace(ex);
                                }
                            }
                        }
                    }
                }
                jdoc.set(res);
            }
            return res;
        }

        @NonNull
        String getAntName() {
            return antName;
        }

        @NonNull
        synchronized JavadocForBinaryQuery.Result createJavadocResult() {
            if (jdocResult == null) {
                jdocResult = new ResultImpl(this);
            }
            return jdocResult;
        }

        public void addPropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            this.support.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(@NonNull final PropertyChangeListener listener) {
            Parameters.notNull("listener", listener);   //NOI18N
            this.support.removePropertyChangeListener(listener);
        }

        @Override
        public void propertyChange(final PropertyChangeEvent event) {
            final String propName = event.getPropertyName();
            if (propName == null) {
                rt.set(null);
                jdoc.set(null);
                support.firePropertyChange(PROP_RUNTIME, null, null);
                support.firePropertyChange(PROP_JAVADOC, null, null);
            } else if (rtPropName.equals(propName)) {
                rt.set(null);
                support.firePropertyChange(PROP_RUNTIME, null, null);
            } else if (jdocPropName.equals(propName)) {
                jdoc.set(null);
                support.firePropertyChange(PROP_JAVADOC, null, null);
            }
        }

        @NonNull
        static JavaFXSDK forJavaPlatform(
            @NonNull final PropertyEvaluator eval,
            @NonNull final JavaPlatform platform) {
            Parameters.notNull("platform", platform);   //NOI18N
            final String antName = platform.getProperties().get(JavaFXPlatformUtils.PLATFORM_ANT_NAME);
            final String rtPropName = Utils.getRuntimePropertyKey(platform);
            final String jdocPropName = Utils.getJavadocPropertyKey(platform);
            return new JavaFXSDK(eval, antName, rtPropName, jdocPropName);
        }
    }

    private static final class ResultImpl implements JavadocForBinaryQuery.Result, PropertyChangeListener {

        private final JavaFXSDK sdk;
        private final ChangeSupport support;

        private ResultImpl(@NonNull final JavaFXSDK sdk) {
            Parameters.notNull("sdk", sdk); //NOI18N
            this.sdk = sdk;
            this.support = new ChangeSupport(this);
            this.sdk.addPropertyChangeListener(WeakListeners.propertyChange(this, sdk));
        }

        @Override
        public URL[] getRoots() {
            return sdk.getJavadoc().toArray(new URL[0]);
        }

        @Override
        public void addChangeListener(@NonNull final ChangeListener l) {
            Parameters.notNull("l", l);
            support.addChangeListener(l);
        }

        @Override
        public void removeChangeListener(@NonNull final ChangeListener l) {
            Parameters.notNull("l", l);
            support.removeChangeListener(l);
        }

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (JavaFXSDK.PROP_JAVADOC.equals(evt.getPropertyName())) {
                support.fireChange();
            }
        }

    }

}
