/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javacard.project;

import java.beans.PropertyChangeListener;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.javacard.common.ListenerProxy;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;

/**
 * Mysterious why there is not a factory for this in the java common project
 * API, as it seems universally needed.
 * <p/>
 * Ensures we do not change classpath identity on roots changes.  Also,
 * for some reason, there are things getting a reference to the source
 * classpath while the project is still being created from template,
 * so we need a classpath that responds to metadata changes during that time.
 *
 * @author Tim Boudreau
 */
final class SourceRootsClasspathImpl extends ListenerProxy<SourceRoots> implements ClassPathImplementation {
    private final Res res;
    SourceRootsClasspathImpl (SourceRoots roots) {
        super (roots);
        res = new Res(roots);
    }

    @Override
    public List<? extends PathResourceImplementation> getResources() {
        return Arrays.asList(res);
    }

    @Override
    protected void attach(SourceRoots obj, PropertyChangeListener pcl) {
        obj.addPropertyChangeListener(pcl);
    }

    @Override
    protected void detach(SourceRoots obj, PropertyChangeListener pcl) {
        obj.removePropertyChangeListener(pcl);
    }

    @Override
    protected void onChange(String prop, Object old, Object nue) {
        if (SourceRoots.PROP_ROOT_PROPERTIES.equals(prop)) {
            fire (PROP_RESOURCES, old, getResources());
        }
    }

    private final class Res extends ListenerProxy<SourceRoots> implements PathResourceImplementation {
        Res(SourceRoots roots) {
            super (roots);
        }

        @Override
        public URL[] getRoots() {
            return get().getRootURLs();
        }

        @Override
        public ClassPathImplementation getContent() {
            return SourceRootsClasspathImpl.this;
        }

        @Override
        protected void attach(SourceRoots roots, PropertyChangeListener pcl) {
            roots.addPropertyChangeListener(pcl);
        }

        @Override
        protected void detach(SourceRoots roots, PropertyChangeListener pcl) {
            roots.removePropertyChangeListener(pcl);
        }

        @Override
        public void onChange(String propName, Object old, Object nue) {
            if (SourceRoots.PROP_ROOTS.equals(propName)) {
                fire (PathResourceImplementation.PROP_ROOTS, old, getRoots());
            }
        }
    }
}
