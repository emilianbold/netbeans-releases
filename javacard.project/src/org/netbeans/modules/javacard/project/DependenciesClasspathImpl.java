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
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.javacard.common.ListenerProxy;
import org.netbeans.modules.javacard.project.deps.ArtifactKind;
import org.netbeans.modules.javacard.project.deps.Dependencies;
import org.netbeans.modules.javacard.project.deps.Dependency;
import org.netbeans.modules.javacard.project.deps.ResolvedDependencies;
import org.netbeans.modules.javacard.project.deps.ResolvedDependency;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.xml.sax.SAXException;

/**
 *
 * @author Tim Boudreau
 */
final class DependenciesClasspathImpl extends ListenerProxy<JCProject> implements ClassPathImplementation, ChangeListener {
    private final Set<DependencyPathResourceImplementation> resources = new TreeSet<DependencyPathResourceImplementation>();
    volatile boolean attached;
    DependenciesClasspathImpl(JCProject project) {
        super (project);
    }

    @Override
    protected void attach(JCProject obj, PropertyChangeListener precreatedListener) {
        getDeps();
        obj.addDependencyChangeListener(this);
        attached = true;
    }

    synchronized Dependencies getDeps() {
        try {
            return get().syncGetDependencies();
        } catch (SAXException ex) {
            Exceptions.printStackTrace(ex);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    @Override
    protected void detach(JCProject obj, PropertyChangeListener precreatedListener) {
        obj.removeDependencyChangeListener(this);
        resources.clear();
        attached = false;
    }

    @Override
    protected void onChange(String prop, Object old, Object nue) {
        //do nothing
    }

    @Override
    public List<? extends PathResourceImplementation> getResources() {
        if (!attached) {
            synchronized (this) {
                List<? extends PathResourceImplementation> l = refresh();
                resources.clear();
                return l;
            }
        }
        synchronized (this) {
            return new ArrayList<PathResourceImplementation>(resources);
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        refresh();
    }

    private synchronized final List<? extends PathResourceImplementation> refresh() {
        List<Dependency> l = getDeps().all();
        Set<DependencyPathResourceImplementation> old;
        Set<DependencyPathResourceImplementation> nue;
        synchronized (this) {
            old = new TreeSet<DependencyPathResourceImplementation>(resources);
            nue = new TreeSet<DependencyPathResourceImplementation>();
            for (int i= 0; i < l.size(); i++) {
                nue.add (new DependencyPathResourceImplementation(l.get(i), i));
            }
            if (resources.equals(nue)) {
                return new ArrayList<PathResourceImplementation>(resources);
            }
            Set<DependencyPathResourceImplementation> removed = new HashSet<DependencyPathResourceImplementation>(old);
            removed.removeAll(nue);
            Set<DependencyPathResourceImplementation> added = new HashSet<DependencyPathResourceImplementation>(nue);
            added.removeAll(old);
            for (DependencyPathResourceImplementation d : removed) {
                resources.remove (d);
            }
            for (DependencyPathResourceImplementation d : added) {
                resources.add(d);
            }
        }
        List <? extends PathResourceImplementation> result = new ArrayList<PathResourceImplementation>(nue);
        fire (ClassPathImplementation.PROP_RESOURCES, old, result);
        return result;
    }

    final ResolvedDependencies rd() {
        ResolvedDependencies result = null;
        Dependencies d = getDeps();
        if (d != null) { //corrupt project metadata
            result = get().createResolvedDependencies(d);
        }
        return result;
    }

    private final class DependencyPathResourceImplementation implements PathResourceImplementation, Comparable<DependencyPathResourceImplementation> {
        private final Dependency dep;
        private final int index;
        DependencyPathResourceImplementation(Dependency dep, int index) {
            this.dep = dep;
            this.index = index;
        }

        @Override
        public String toString() {
            return super.toString() + "[" + dep.getID() + ":" + Arrays.asList(getRoots())  + "]"; //NOI18N
        }

        @Override
        public URL[] getRoots() {
            ResolvedDependencies rd = rd();
            if (rd == null) { //corrupt project metadata
                return new URL[0];
            }
            ResolvedDependency d = rd.get(dep.getID());
            assert d != null : "Dependency " + dep + " resolves to null";
            
            List<URL> urls = new ArrayList<URL>();
            File f = d.resolveFile(ArtifactKind.ORIGIN);
            if (f != null) {
                if (d.getKind().isProjectDependency()) {
                    FileObject fo = FileUtil.toFileObject(f);
                    if (fo != null) {
                        Project p = FileOwnerQuery.getOwner(fo);
                        if (p != null) {
                            try {
                                URL url = p.getProjectDirectory().getURL();
                                AntArtifactProvider prov = p.getLookup().lookup(AntArtifactProvider.class);
                                for (AntArtifact a : prov.getBuildArtifacts()) {
                                    if (JavaProjectConstants.ARTIFACT_TYPE_JAR.equals(a.getType())) {
                                        URI[] uris = a.getArtifactLocations();
                                        for (URI u : uris) {
                                            url = new URL(u.toString());
                                            if (FileUtil.isArchiveFile(url)) {
                                                url = FileUtil.getArchiveRoot(url);
                                                urls.add(url);
                                            }
                                        }
                                    }
                                }
                            } catch (MalformedURLException ex) {
                                Exceptions.printStackTrace(ex);
                            } catch (FileStateInvalidException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                } else {
                    if (f != null) {
                        try {
                            URL url = f.toURI().toURL();
                            if (FileUtil.isArchiveFile(url)) {
                                url = FileUtil.getArchiveRoot(url);
                            }
                            urls.add(url);
                        } catch (MalformedURLException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            }
            return urls.toArray(new URL[urls.size()]);
        }

        @Override
        public ClassPathImplementation getContent() {
            return DependenciesClasspathImpl.this;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            //do nothing
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            //do nothing
        }

        @Override
        public int compareTo(DependencyPathResourceImplementation o) {
            return index - o.index;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final DependencyPathResourceImplementation other = (DependencyPathResourceImplementation) obj;
            if (this.dep != other.dep && (this.dep == null || !this.dep.equals(other.dep))) {
                return false;
            }
            if (this.index != other.index) {
                return false;
            }
            return true;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 71 * hash + (this.dep != null ? this.dep.hashCode() : 0);
            hash = 71 * hash + this.index;
            return hash;
        }
    }
}
