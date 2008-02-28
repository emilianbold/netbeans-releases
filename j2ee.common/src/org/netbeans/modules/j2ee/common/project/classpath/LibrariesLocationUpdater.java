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

package org.netbeans.modules.j2ee.common.project.classpath;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.j2ee.common.project.ui.ProjectProperties;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * Support which listens on library changes and classpath property changes
 * and generates properties in project.properties for any used library jar.
 * This is needed for Ant to be able to perform library copying without using
 * properietary Ant task.
 * 
 * @author Tomas Zezula, David Konecny
 */
public final class LibrariesLocationUpdater implements PropertyChangeListener {
    
    private String classPathProperty;
    private String projectXMLElement;
    private String additionalClassPathProperty;
    private String additionalProjectXMLElement;

    private final Project project;
    private final UpdateHelper helper;
    private final PropertyEvaluator eval;    
    private final ClassPathSupport cs;    
    private final AntProjectHelper antHelper;
    
    private final PropertyChangeListener listener = WeakListeners.propertyChange(this, null);
    
    private static final Logger LOG = Logger.getLogger(LibrariesLocationUpdater.class.getName());

    /** Creates a new instance of J2SEProjectClassPathModifier */
    public LibrariesLocationUpdater(final Project project, final UpdateHelper helper, 
            final PropertyEvaluator eval, ClassPathSupport cs, String classPathProperty,
            String projectXMLElement, String additionalClassPathProperty, String additionalProjectXMLElement) {
        assert project != null;
        assert helper != null;
        assert eval != null;
        this.project = project;
        this.helper = helper;
        this.eval = eval;
        this.antHelper = helper.getAntProjectHelper();
        this.cs = cs;
        this.classPathProperty = classPathProperty;
        this.projectXMLElement = projectXMLElement;
        this.additionalClassPathProperty = additionalClassPathProperty;
        this.additionalProjectXMLElement = additionalProjectXMLElement;
        
        //#56140
        eval.addPropertyChangeListener(listener); //listen for changes of libraries list
        registerLibraryListeners();
    }
    
    
    private void reRegisterLibraryListeners() {
        unregisterLibraryListeners();
        registerLibraryListeners();
    }
    
    private void unregisterLibraryListeners() {
        for (LibraryManager man : LibraryManager.getOpenManagers()) {
            Library libs [] = man.getLibraries();
            for (int i = 0; i < libs.length; i++) {
                libs[i].removePropertyChangeListener(listener);
            }
        }
    }
    
    /**
     * Destroy this listeners.
     */
    public void unregister() {
        unregisterLibraryListeners();
        eval.removePropertyChangeListener(this);
    }
    
    private void registerLibraryListeners() {
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                ProjectManager.mutex().readAccess(new Runnable() {
                    public void run() {
                        EditableProperties props = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH); //Reread the properties, PathParser changes them
                        HashSet set = new HashSet();
                        // intentionally pass null to itemsList() - we do not need additional info to be read
                        set.addAll(cs.itemsList(props.getProperty(classPathProperty),  null));
                        if (additionalClassPathProperty != null) {
                            set.addAll(cs.itemsList(props.getProperty(additionalClassPathProperty),  null));
                        }
                        Iterator i = set.iterator();
                        while (i.hasNext()) {
                            ClassPathSupport.Item item = (ClassPathSupport.Item)i.next();
                            if (item.getType() == ClassPathSupport.Item.TYPE_LIBRARY && !item.isBroken()) {
                                item.getLibrary().addPropertyChangeListener(listener);
                            }
                        }
                    }
                });
            }
        });
    }
    
    public void propertyChange (PropertyChangeEvent e) {
        if (!ProjectManager.getDefault().isValid(project)) {
            return;
        }
        if (e.getSource().equals(eval) && e.getPropertyName().equals(classPathProperty)) {
            // if project property changed then update listeners and store locations
            reRegisterLibraryListeners();
            storeLibLocations();
        } else if (e.getPropertyName().equals(Library.PROP_CONTENT)) {
            storeLibLocations();
        }
    }
    
    private void storeLibLocations() {
        if (!ProjectManager.getDefault().isValid(project)) {
            return;
        }
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                ProjectManager.mutex().writeAccess(new Runnable() {
                    public void run() {
                        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);    //Reread the properties, PathParser changes them
                        List wmLibs = cs.itemsList(props.getProperty(classPathProperty),  projectXMLElement);
                        cs.encodeToStrings(wmLibs, projectXMLElement);
                        HashSet set = new HashSet();
                        set.addAll(wmLibs);
                        if (additionalClassPathProperty != null) {
                            List additionalLibs = cs.itemsList(props.getProperty(additionalClassPathProperty),  additionalProjectXMLElement);
                            cs.encodeToStrings(additionalLibs, additionalProjectXMLElement);
                            set.addAll(additionalLibs);
                        }
                        ProjectProperties.storeLibrariesLocations(set.iterator(), props, project.getProjectDirectory());
                        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, props);
                        try {
                            ProjectManager.getDefault().saveProject(project);
                        } catch (IOException e) {
                            Exceptions.printStackTrace(e);
                        }
                    }
                });
            }
        });
    }

}
