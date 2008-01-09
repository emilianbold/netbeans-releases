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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.groovy.grailsproject.classpath;

import org.netbeans.spi.java.classpath.ClassPathImplementation;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.classpath.ClassPath;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import org.netbeans.modules.groovy.support.api.GroovySettings;
import org.netbeans.spi.java.classpath.PathResourceImplementation;
import org.openide.filesystems.FileUtil;

final class BootClassPathImplementation implements ClassPathImplementation {

    private JavaPlatformManager platformManager;
    private List<PathResourceImplementation> resourcesCache;
    private long eventId;
    private PropertyChangeSupport support = new PropertyChangeSupport(this);

    public BootClassPathImplementation() {
    }

    public List<PathResourceImplementation> getResources() {
        long currentId;
        synchronized (this) {
            if (this.resourcesCache != null) {
                return this.resourcesCache;
            }
            currentId = eventId;
        }
        
        JavaPlatform jp = findActivePlatform();
        final List<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>();
        if (jp != null) {
            //TODO: May also listen on CP, but from Platform it should be fixed.
            final ClassPath cp = jp.getBootstrapLibraries();
            assert cp != null : jp;
            for (ClassPath.Entry entry : cp.entries()) {
                result.add(ClassPathSupport.createResource(entry.getURL()));
            }
        }
        
        result.addAll(findGroovyPlatform());
        
        synchronized (this) {
            if (currentId == eventId) {
                if (this.resourcesCache == null) {
                    this.resourcesCache = Collections.unmodifiableList(result);
                }
                return this.resourcesCache;
            }
            return Collections.unmodifiableList (result);
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        this.support.addPropertyChangeListener (listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        this.support.removePropertyChangeListener (listener);
    }

    private JavaPlatform findActivePlatform () {
        if (this.platformManager == null) {
            this.platformManager = JavaPlatformManager.getDefault();
        }
        return this.platformManager.getDefaultPlatform();
    }

    private List<PathResourceImplementation> findGroovyPlatform() {
        List<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>();
        
        GroovySettings groovySettings = new GroovySettings();
        File groovyHome = new File(groovySettings.getGroovyHome());
        if (!groovyHome.exists()) {
            return Collections.<PathResourceImplementation>emptyList();
        }
        File embeddableDir = new File(groovyHome, "embeddable"); // NOI18N
        if (!embeddableDir.exists()) {
            return Collections.<PathResourceImplementation>emptyList();
        }
        File[] jars = embeddableDir.listFiles();
        if (jars == null || jars.length == 0) {
            File libDir = new File(groovyHome, "lib"); // NOI18N
            if (!libDir.exists()) {
                return Collections.<PathResourceImplementation>emptyList();
            }
            jars = libDir.listFiles();
        }
        for (File f : jars) {
            try {
                if (f.isFile()) {
                    URL entry = f.toURI().toURL();
                    if (FileUtil.isArchiveFile(entry)) {
                        entry = FileUtil.getArchiveRoot(entry);
                        result.add(ClassPathSupport.createResource(entry));
                    }
                }
            } catch (MalformedURLException mue) {
                assert false : mue;
            }
        }
        return Collections.unmodifiableList(result);
    }
    
}
