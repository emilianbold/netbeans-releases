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
package org.netbeans.modules.ruby.railsprojects.classpath;

import java.beans.PropertyChangeEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.net.URL;
import org.netbeans.modules.ruby.railsprojects.SourceRoots;
import org.netbeans.modules.ruby.spi.project.support.rake.PropertyEvaluator;
import org.netbeans.modules.ruby.spi.project.support.rake.RakeProjectHelper;
import org.netbeans.spi.gsfpath.classpath.ClassPathImplementation;
import org.netbeans.spi.gsfpath.classpath.PathResourceImplementation;
import org.netbeans.spi.gsfpath.classpath.support.ClassPathSupport;
import org.openide.ErrorManager;

/**
 * Implementation of a single classpath that is derived from one Ant property.
 */
final class SourcePathImplementation implements ClassPathImplementation, PropertyChangeListener {

    private static final String PROP_BUILD_DIR = "build.dir";   //NOI18N
    
    private final PropertyChangeSupport support = new PropertyChangeSupport(this);
    private List<PathResourceImplementation> resources;
    private final SourceRoots sourceRoots;
    private final RakeProjectHelper projectHelper;
    private final PropertyEvaluator evaluator;
    
    /**
     * Construct the implementation.
     * @param sourceRoots used to get the roots information and events
     */
    public SourcePathImplementation(SourceRoots sourceRoots) {
        assert sourceRoots != null;
        this.sourceRoots = sourceRoots;
        this.projectHelper = null;
        this.evaluator = null;
        sourceRoots.addPropertyChangeListener (this);
    }
    
    /**
     * Construct the implementation.
     * @param sourceRoots used to get the roots information and events
     * @param projectHelper used to obtain the project root
     */
    public SourcePathImplementation(SourceRoots sourceRoots, RakeProjectHelper projectHelper, PropertyEvaluator evaluator) {
        assert sourceRoots != null && projectHelper != null && evaluator != null;
        this.sourceRoots = sourceRoots;
        sourceRoots.addPropertyChangeListener (this);
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
        URL[] roots = sourceRoots.getRootURLs();                                
        synchronized (this) {
            if (this.resources == null) {
                List<PathResourceImplementation> result = new ArrayList<PathResourceImplementation>(roots.length);
                for (URL root : roots) {
                    result.add(ClassPathSupport.createResource(root));
                }
                // adds java artifacts generated by wscompile and wsimport to resources to be available for code completion
                if (projectHelper!=null) {
                    try {
                        String buildDir = this.evaluator.getProperty(PROP_BUILD_DIR);
                        if (buildDir != null) {
                            // generated/wsclient
                            File f =  new File (this.projectHelper.resolveFile (buildDir),"generated/wsclient"); //NOI18N
                            URL url = f.toURI().toURL();
                            if (!f.exists()) {  //NOI18N
                                assert !url.toExternalForm().endsWith("/");  //NOI18N
                                url = new URL (url.toExternalForm()+'/');   //NOI18N
                            }
                            result.add(ClassPathSupport.createResource(url));
                            
                            // generated/wsimport/client
                            f = new File (projectHelper.resolveFile(buildDir),"generated/wsimport/client"); //NOI18N
                            url = f.toURI().toURL();
                            if (!f.exists()) {  //NOI18N
                                assert !url.toExternalForm().endsWith("/");  //NOI18N
                                url = new URL (url.toExternalForm()+'/');   //NOI18N
                            }
                            result.add(ClassPathSupport.createResource(url));
                        }
                    } catch (MalformedURLException ex) {
                        ErrorManager.getDefault ().notify (ex);
                    }
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
        if (SourceRoots.PROP_ROOTS.equals (evt.getPropertyName())) {
            synchronized (this) {
                this.resources = null;
            }
            this.support.firePropertyChange (PROP_RESOURCES,null,null);
        }
        else if (this.evaluator != null && evt.getSource() == this.evaluator && 
            (evt.getPropertyName() == null || PROP_BUILD_DIR.equals(evt.getPropertyName()))) {
            synchronized (this) {
                this.resources = null;
            }
            this.support.firePropertyChange (PROP_RESOURCES,null,null);
        }
    }

}
