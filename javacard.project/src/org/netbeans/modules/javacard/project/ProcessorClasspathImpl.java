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
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.javacard.common.ListenerProxy;
import org.netbeans.modules.javacard.constants.ProjectPropertyNames;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 *
 * @author Tim Boudreau
 */
final class ProcessorClasspathImpl implements ClassPathImplementation {

    private final JCProject project;
    private final PlatformResource pformResource;
    private final PropertyResource propResource;

    ProcessorClasspathImpl(JCProject project) {
        this.project = project;
        this.pformResource = new PlatformResource(project);
        this.propResource = new PropertyResource();
    }

    @Override
    public List<? extends PathResourceImplementation> getResources() {
        return Arrays.asList(propResource, pformResource);
    }

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listener) {
        //do nothing
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listener) {
        //do nothing
    }

    void processorPathChanged() {
        propResource.change();
    }

    private class PlatformResource extends ListenerProxy<JCProject> implements PathResourceImplementation, ChangeListener {

        private ClassPath listeningTo;

        PlatformResource(JCProject project) {
            super(project);
        }

        @Override
        public URL[] getRoots() {
            List<URL> l = new ArrayList<URL>();
            for (ClassPath.Entry e : project.getPlatform().getProcessorClasspath(get().kind()).entries()) {
                URL url = e.getURL();
                l.add(url);
            }
            URL[] urls = (URL[]) l.toArray(new URL[l.size()]);
            return urls;
        }

        @Override
        public ClassPathImplementation getContent() {
            return ProcessorClasspathImpl.this;
        }

        @Override
        protected void attach(JCProject obj, PropertyChangeListener precreatedListener) {
            setListeningTo(project.getPlatform().getProcessorClasspath(get().kind()), precreatedListener);
            obj.addChangeListener(this);
        }

        @Override
        protected void detach(JCProject obj, PropertyChangeListener precreatedListener) {
            setListeningTo(null, precreatedListener);
        }

        @Override
        protected void onChange(String prop, Object old, Object nue) {
            if (ClassPath.PROP_ROOTS.equals(prop) || ClassPath.PROP_ENTRIES.equals(prop)) {
                fire(PathResourceImplementation.PROP_ROOTS, null, getRoots());
            }
        }

        private synchronized void setListeningTo(ClassPath cp, PropertyChangeListener precreatedListener) {
            if (listeningTo == cp) {
                return;
            }
            if (listeningTo != null) {
                listeningTo.removePropertyChangeListener(precreatedListener);
            }
            listeningTo = cp;
            if (cp != null) {
                cp.addPropertyChangeListener(precreatedListener);
            }
        }

        @Override
        public void stateChanged(ChangeEvent e) {
            fire(PROP_ROOTS, null, null);
        }
    }

    private class PropertyResource implements PathResourceImplementation {

        private final PropertyChangeSupport supp = new PropertyChangeSupport(this);

        void change() {
            supp.firePropertyChange(PROP_ROOTS, null, null);
        }

        @Override
        public URL[] getRoots() {
            List<URL> result = new ArrayList<URL>();
            String paths = project.evaluator().getProperty(ProjectPropertyNames.PROJECT_PROP_PROCESSOR_PATH);
            if (paths != null) {
                Pattern p = Pattern.compile(File.pathSeparator, Pattern.LITERAL);
                String[] abs;
                if (p.matcher(paths).matches()) {
                    String[] each = p.split(paths);
                    abs = new String[each.length];
                    for (int i = 0; i < abs.length; i++) {
                        String path = each[i];
                        abs[i] = absolutePath(path);
                    }
                } else {
                    abs = new String[]{absolutePath(paths)};
                }
                URL[] urls = getURLs(abs);
                result.addAll(Arrays.asList(urls));
            }
            URL[] urls = (URL[]) result.toArray(new URL[result.size()]);
            return urls;
        }

        @Override
        public ClassPathImplementation getContent() {
            return ProcessorClasspathImpl.this;
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener listener) {
            supp.addPropertyChangeListener(listener);
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener listener) {
            supp.removePropertyChangeListener(listener);
        }

        private String absolutePath(String possiblyRelativePath) {
            File f = new File(possiblyRelativePath);
            if (!f.exists()) {
                File nue = new File(FileUtil.toFile(project.getProjectDirectory()), possiblyRelativePath);
                if (nue.exists()) {
                    f = nue;
                }
            }
            return f.getAbsolutePath();
        }
    }

    private static URL[] getURLs(String[] entries) {
        //PENDING:  Once actual UI for this is set up, probably use ReferenceHelper to manage references instead
        List<URL> urls = new ArrayList<URL>(entries.length);
        for (String s : entries) {
            File f = new File(s);
            try {
                URL url = f.toURI().toURL();
                if (f.getName().endsWith(".jar")) { //NOI18N
                    String jarURL = "jar:" + url + "!/"; //NOI18N
                    url = new URL(jarURL);
                } else if (!url.toString().endsWith("/")) {
                    //path to src/ subdir in some distros will not exist
                    //Manually append a / so SimplePathResourceImplementation
                    //does not throw an exception
                    url = new URL(url.toString() + "/"); //NOI18N
                }
                urls.add(url);
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        URL[] results = urls.toArray(new URL[urls.size()]);
        return results;
    }
}
