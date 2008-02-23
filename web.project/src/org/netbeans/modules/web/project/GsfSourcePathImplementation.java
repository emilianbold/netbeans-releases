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
package org.netbeans.modules.web.project;

import java.beans.PropertyChangeEvent;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.gsfpath.spi.classpath.ClassPathImplementation;
import org.netbeans.modules.gsfpath.spi.classpath.PathResourceImplementation;
import org.netbeans.modules.gsfpath.spi.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;

/**
 * Implementation of a single classpath that is derived from one Ant property.
 */
final class GsfSourcePathImplementation implements ClassPathImplementation, PropertyChangeListener, ChangeListener {

    private static final String PROP_BUILD_DIR = "build.dir";   //NOI18N
    
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private List<PathResourceImplementation> resources;
    private final WebSources sourceRoots;
    private final AntProjectHelper projectHelper;
    private final PropertyEvaluator evaluator;
    private String urlString;
    
    /**
     * Construct the implementation.
     * @param sourceRoots used to get the roots information and events
     */
    public GsfSourcePathImplementation(WebSources sourceRoots) {
        assert sourceRoots != null;
        this.sourceRoots = sourceRoots;
        this.projectHelper = null;
        this.evaluator = null;
//        sourceRoots.addPropertyChangeListener (this);
        sourceRoots.addChangeListener (this);
    }
    
    /**
     * Construct the implementation.
     * @param sourceRoots used to get the roots information and events
     * @param projectHelper used to obtain the project root
     */
    public GsfSourcePathImplementation(WebSources sourceRoots, AntProjectHelper projectHelper, PropertyEvaluator evaluator) {
        assert sourceRoots != null && projectHelper != null && evaluator != null;
        this.sourceRoots = sourceRoots;
        //sourceRoots.addPropertyChangeListener (this);
        sourceRoots.addChangeListener(this);
        this.projectHelper = projectHelper;
        this.evaluator = evaluator;
        evaluator.addPropertyChangeListener (this);
    }

    public List<PathResourceImplementation> getResources() {
        synchronized (this) {
            if (this.resources != null) {
                return this.resources;
            }
        }                                
        
        // TODO - synchronize
        List<URL> roots = new ArrayList<URL>();
        SourceGroup[] sourceGroups = sourceRoots.getSourceGroups(Sources.TYPE_GENERIC);
        for (SourceGroup sg : sourceGroups) {
            try {
                roots.add(FileUtil.toFile(sg.getRootFolder()).toURI().toURL());
            } catch (MalformedURLException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        
        //URL[] roots = sourceRoots.getRootURLs();                                
        synchronized (this) {
            if (this.resources == null) {
                List<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>(roots.size());
                for (URL root : roots) {
                    urlString = root.toExternalForm();
                    result.add(ClassPathSupport.createResource(root));
                }
                this.resources = Collections.unmodifiableList(result);
            }
            return this.resources;
        }
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        support.addPropertyChangeListener (listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        support.removePropertyChangeListener (listener);
    }


    public void propertyChange(PropertyChangeEvent evt) {
/*        if (SourceRoots.PROP_ROOTS.equals (evt.getPropertyName())) {
            synchronized (this) {
                this.resources = null;
            }
            this.support.firePropertyChange (PROP_RESOURCES,null,null);
        }
        else */ if (this.evaluator != null && evt.getSource() == this.evaluator && 
            (evt.getPropertyName() == null || PROP_BUILD_DIR.equals(evt.getPropertyName()))) {
            synchronized (this) {
                this.resources = null;
            }
            this.support.firePropertyChange (PROP_RESOURCES,null,null);
        }
    }

    public void stateChanged(ChangeEvent e) {
        this.support.firePropertyChange(PROP_RESOURCES, null, null);
    }

}
