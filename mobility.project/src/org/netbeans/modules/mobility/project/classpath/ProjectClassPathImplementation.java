/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.mobility.project.classpath;
import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.AntProjectListener;
import org.netbeans.spi.project.support.ant.AntProjectEvent;

import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.MalformedURLException;
import java.io.File;
import java.net.URL;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.openide.filesystems.FileUtil;

public abstract class ProjectClassPathImplementation implements ClassPathImplementation, AntProjectListener, Runnable {
    
    final private PropertyChangeSupport support = new PropertyChangeSupport(this);
    final private AntProjectHelper helper;
    private List<PathResourceImplementation> resources;
    private String path;
    
    
    public ProjectClassPathImplementation(AntProjectHelper helper) {
        assert helper != null;
        this.helper = helper;
        this.helper.addAntProjectListener(this);
    }
    
    public List<PathResourceImplementation> getResources() {
        if (this.resources == null) {
            final String newPath = evaluatePath();
            if (this.resources == null) {
                final List<PathResourceImplementation> newResources = createResources(newPath);
                synchronized (this) {
                    if (this.resources == null) {
                        this.path = newPath;
                        this.resources = newResources;
                    }
                }
            }
        }
        return this.resources;
    }
    
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        support.addPropertyChangeListener(listener);
    }
    
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        support.removePropertyChangeListener(listener);
    }
    
    public void addResource(@SuppressWarnings("unused")
	final PathResourceImplementation resource) {
        //TODO: Implement this
        throw new UnsupportedOperationException();
    }
    
    public void removeResource(final PathResourceImplementation resource) {
        final List<PathResourceImplementation> l = this.getResources();
        if (l.contains(resource)) {
            //TODO: Implement this
            throw new UnsupportedOperationException();
        }
    }
    
    public void reorder(@SuppressWarnings("unused")
	final PathResourceImplementation[] order) throws IllegalArgumentException {
        //TODO: Implement this
        throw new UnsupportedOperationException();
    }
    
    
    public void configurationXmlChanged(@SuppressWarnings("unused")
	final AntProjectEvent ev) {
        //Not interesting
    }
    
    public void propertiesChanged(@SuppressWarnings("unused")
	final AntProjectEvent ev) {
        ProjectManager.mutex().postWriteRequest(this);
    }
    
    public void run() {
        final String newPath = evaluatePath();
        boolean fire = false;
        if (this.resources == null || newPath == null || !newPath.equals(this.path)) {
            final List<PathResourceImplementation> newResources = createResources(newPath);
            synchronized (this) {
                if (this.resources == null || newPath == null || !newPath.equals(this.path)) {
                    this.path = newPath;
                    this.resources = newResources;
                    fire = true;
                }
            }
        }
        if (fire) {
            support.firePropertyChange(PROP_RESOURCES,null,null);
        }
    }
    
    private List<PathResourceImplementation> createResources(final String _path) {
        final List<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>();
        if (_path != null) {
            final String[] pieces = PropertyUtils.tokenizePath(_path);
            for (int i = 0; i < pieces.length; i++) {
                final File f = FileUtil.normalizeFile(helper.resolveFile(pieces[i]));
                try {
                    URL entry = f.toURI().toURL();
                    if (FileUtil.isArchiveFile(entry)) {
                        entry = FileUtil.getArchiveRoot(entry);
                    }
                    else if (!f.exists()) {
                        // if file does not exist (e.g. build/classes folder
                        // was not created yet) then corresponding File will
                        // not be ended with slash. Fix that.
                        assert !entry.toExternalForm().endsWith("/") : f; // NOI18N
                        entry = new URL(entry.toExternalForm() + "/"); // NOI18N
                    }
                    else if ( !entry.toExternalForm().endsWith("/")) { // NOI18N
                        /* Possible fix for #156890 - IllegalArgumentException: URL must be a folder URL (append '/' if necessary):
                         * This will fix an the issue in any case. But possibly
                         * this "else" will never work .
                         */
                        entry = new URL(entry.toExternalForm() + "/"); // NOI18N
                    }
                    result.add(ClassPathSupport.createResource(entry));
                } catch (MalformedURLException mue) {
                    assert false : mue;
                }
            }
        }
        return Collections.unmodifiableList(result);
    }
    
    abstract protected String evaluatePath();
}
