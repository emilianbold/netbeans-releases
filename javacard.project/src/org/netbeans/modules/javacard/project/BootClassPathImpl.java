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
import java.util.ArrayList;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.ClassPath.Entry;
import org.netbeans.modules.javacard.common.ListenerProxy;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;

/**
 *
 * @author Tim Boudreau
 */
final class BootClassPathImpl extends ListenerProxy<JCProject> implements ClassPathImplementation, ChangeListener {
    private ClassPath bootPath;
    volatile boolean attached;
    BootClassPathImpl (JCProject project) {
        super (project);
    }

    @Override
    protected void attach(JCProject obj, PropertyChangeListener precreatedListener) {
        obj.addChangeListener(this);
        bootPath = bootPath();
        attached = true;
        bootPath.addPropertyChangeListener(precreatedListener);
    }

    @Override
    protected void detach(JCProject obj, PropertyChangeListener precreatedListener) {
        if (bootPath != null) {
            bootPath.removePropertyChangeListener(precreatedListener);
            bootPath = null;
        }
        attached = false;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        List<? extends PathResourceImplementation> old = getResources();
        ClassPath bp = attached ? bootPath == null ? bootPath() : bootPath : bootPath();
        if (attached) {
            this.bootPath = bp;
        }
        List<? extends PathResourceImplementation> nue = getResources();
        if (!old.equals(nue)) {
            fire (PROP_RESOURCES, old, nue);
        }
    }

    @Override
    protected void onChange(String prop, Object old, Object nue) {
        fire(prop, old, nue);
    }

    @Override
    public List<? extends PathResourceImplementation> getResources() {
        ClassPath actual = attached ? bootPath == null ? bootPath() : bootPath : bootPath();
        List<PathResourceImplementation> l = new ArrayList<PathResourceImplementation>();
        for (ClassPath.Entry e : actual.entries()) {
            l.add (new PRI(e));
        }
        return l;
    }

    private ClassPath bootPath() {
        return get().getPlatform().getBootstrapLibraries(get().kind());
    }

    private final class PRI implements PathResourceImplementation {
        private final Entry e;
        PRI (ClassPath.Entry e) {
            this.e = e;
        }

        @Override
        public URL[] getRoots() {
            return new URL[] { e.getURL() };
        }

        @Override
        public ClassPathImplementation getContent() {
            return BootClassPathImpl.this;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            //do nothing
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            //do nothing
        }

    }
}
