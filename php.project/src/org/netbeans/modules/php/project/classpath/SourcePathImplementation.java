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

package org.netbeans.modules.php.project.classpath;

import java.beans.PropertyChangeEvent;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.URI;
import java.net.URL;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.SourceRoots;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.FilteringPathResourceImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.project.support.ant.PathMatcher;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.util.WeakListeners;

/**
 * Source class path implementation.
 * @author Tor Norbye
 * @author Tomas Zezula
 */
final class SourcePathImplementation implements ClassPathImplementation, PropertyChangeListener {
    static final String INCLUDES = "**"; // NOI18N

    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private final PhpProject project;
    private final PropertyEvaluator evaluator;
    private List<PathResourceImplementation> resources;
    private final SourceRoots src;

    public SourcePathImplementation(PhpProject project, SourceRoots sources) {
        assert project != null;
        assert sources != null;

        this.project = project;
        evaluator = ProjectPropertiesSupport.getPropertyEvaluator(project);
        src = sources;
        src.addPropertyChangeListener(WeakListeners.propertyChange(this, src));
    }

    public List<PathResourceImplementation> getResources() {
        synchronized (this) {
            if (resources != null) {
                return resources;
            }
        }
        final URL[] urls = src.getRootURLs();
        synchronized (this) {
            if (resources == null) {
                List<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>(urls.length);
                for (URL root : urls) {
                    result.add(new FilteringPathResource(evaluator, root));
                }
                resources = Collections.unmodifiableList(result);
            }
            return resources;
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }

    public synchronized void propertyChange(PropertyChangeEvent evt) {
        if (SourceRoots.PROP_ROOTS.equals(evt.getPropertyName())) {
            invalidate();
        } else if (evt.getSource() == evaluator && evt.getPropertyName() == null) {
            invalidate();
        }
    }

    private void invalidate() {
        synchronized (this) {
            resources = null;
        }
        support.firePropertyChange(PROP_RESOURCES, null, null);
    }

    // compute ant pattern
    String computeExcludes(File root) {
        StringBuilder buffer = new StringBuilder(100);
        boolean first = true;
        for (File file : project.getIgnoredFiles()) {
            String relPath = PropertyUtils.relativizeFile(root, file);
            if (relPath != null
                    && !relPath.equals(".") // NOI18N
                    && !relPath.startsWith("../")) { // NOI18N
                String pattern = relPath;
                if (file.isDirectory()) {
                    pattern += "/**"; // NOI18N
                }
                if (first) {
                    first = false;
                } else {
                    buffer.append(","); // NOI18N
                }
                buffer.append(pattern);
            }
        }

        return buffer.toString();
    }

    private final class FilteringPathResource implements FilteringPathResourceImplementation, PropertyChangeListener {

        final PropertyChangeSupport pcs = new PropertyChangeSupport(this);
        volatile PathMatcher matcher;
        private final URL root;

        FilteringPathResource(PropertyEvaluator evaluator, URL root) {
            assert evaluator != null;
            assert root != null;

            this.root = root;
            evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this, evaluator));
        }

        public URL[] getRoots() {
            return new URL[]{root};
        }

        public boolean includes(URL root, String resource) {
            if (matcher == null) {
                File rootFile = new File(URI.create(root.toExternalForm()));
                matcher = new PathMatcher(
                        INCLUDES,
                        computeExcludes(rootFile),
                        rootFile);
            }
            return matcher.matches(resource, true);
        }

        public ClassPathImplementation getContent() {
            return null;
        }

        public void addPropertyChangeListener(PropertyChangeListener listener) {
            pcs.addPropertyChangeListener(listener);
        }

        public void removePropertyChangeListener(PropertyChangeListener listener) {
            pcs.removePropertyChangeListener(listener);
        }

        public void propertyChange(PropertyChangeEvent ev) {
            String prop = ev.getPropertyName();
            // listen only on IGNORE_PATH, VisibilityQuery changes should be checked by parsing & indexing automatically
            if (prop == null || prop.equals(PhpProjectProperties.IGNORE_PATH)) {
                matcher = null;
                PropertyChangeEvent ev2 = new PropertyChangeEvent(this, FilteringPathResourceImplementation.PROP_INCLUDES, null, null);
                ev2.setPropagationId(ev);
                pcs.firePropertyChange(ev2);
            }
        }
    }
}
