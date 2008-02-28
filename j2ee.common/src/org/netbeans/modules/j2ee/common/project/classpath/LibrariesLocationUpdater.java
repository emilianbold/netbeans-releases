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

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
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
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListeners;

/**
 * Support which listens on library content changes
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
    
    private String oldValue;
    private String oldAdditionalValue;
    
    private final PropertyChangeListener listener;
    private boolean destroyed = false;
    
    private static final Logger LOG = Logger.getLogger(LibrariesLocationUpdater.class.getName());
    
    /** Keep list of libraries which are listened on otherwise the instances
     *  gets garbage collected and no notifications are received. */
    private List<Library> libsBeingListenOn = new ArrayList<Library>();

    /** Creates a new instance of J2SEProjectClassPathModifier */
    public LibrariesLocationUpdater(final Project project, final UpdateHelper helper, 
            final PropertyEvaluator eval, ClassPathSupport cs, String classPathProperty,
            String projectXMLElement, String additionalClassPathProperty, String additionalProjectXMLElement) {
        assert project != null;
        assert helper != null;
        assert eval != null;
        this.listener = this;
        this.project = project;
        this.helper = helper;
        this.eval = eval;
        this.antHelper = helper.getAntProjectHelper();
        this.cs = cs;
        this.classPathProperty = classPathProperty;
        this.projectXMLElement = projectXMLElement;
        this.additionalClassPathProperty = additionalClassPathProperty;
        this.additionalProjectXMLElement = additionalProjectXMLElement;

        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        this.oldValue = props.getProperty(classPathProperty);
        if (additionalClassPathProperty != null) {
            this.oldAdditionalValue = props.getProperty(additionalClassPathProperty);
        }
        
        //#56140
        registerLibraryListeners();
        eval.addPropertyChangeListener(listener); //listen for changes of libraries list
    }
    
    private synchronized boolean needsUpdate(String value, String additionalValue) {
        boolean needsUpdate = false;
        if (!stringEquals(value, oldValue)) {
            oldValue = value;
            needsUpdate = true;
        }
        if (additionalClassPathProperty != null && !stringEquals(additionalValue, oldAdditionalValue)) {
            oldAdditionalValue = additionalValue;
            needsUpdate = true;
        }
        return needsUpdate;
    }
    
    private synchronized void unregisterLibraryListeners() {
        for (Library lib : libsBeingListenOn) {
            lib.removePropertyChangeListener(listener);
        }
        libsBeingListenOn.clear();
    }
    
    /**
     * Destroy this listeners.
     */
    public synchronized void destroy() {
        destroyed = true;
        eval.removePropertyChangeListener(this);
        unregisterLibraryListeners();
    }
    
    private synchronized void registerLibraryListeners() {
        if (destroyed) {
            return;
        }
        ProjectManager.mutex().readAccess(new Runnable() {
            public void run() {
                unregisterLibraryListeners();
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
                        libsBeingListenOn.add(item.getLibrary());
                    }
                }
            }
        });
    }
    
    public void propertyChange(final PropertyChangeEvent e) {
        if (!ProjectManager.getDefault().isValid(project)) {
            return;
        }
        if (e.getSource().equals(eval)) {
            if (e.getPropertyName().equals(classPathProperty) ||
                    (additionalClassPathProperty != null && e.getPropertyName().equals(additionalClassPathProperty))) {
                // use different thread than callers one; method requires PM.readAccess
                RequestProcessor.getDefault().post(new Runnable() {
                    public void run() {
                        EditableProperties props = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
                        if (needsUpdate(props.getProperty(classPathProperty), props.getProperty(additionalClassPathProperty))) {
                            // if project property changed then update listeners
                            registerLibraryListeners();
                        }
                    }
                });
            }
        } else if (e.getPropertyName().equals(Library.PROP_CONTENT)) {
            // use different thread than callers one; method requires PM.writeAccess
            RequestProcessor.getDefault().post(new Runnable() {
                public void run() {
                    storeLibLocations();
                }
            });
        }
    }
    
    private static boolean stringEquals(String str1, String str2) {
        return (str1 == null || str1.trim().length() == 0) ? 
            (str2 == null || str2.trim().length() == 0) : str1.equals(str2);
    }
    
    private void storeLibLocations() {
        ProjectProperties.storeLibrariesLocations(project, antHelper, cs, additionalClassPathProperty != null ?
            new String[]{classPathProperty, additionalClassPathProperty} : new String[]{classPathProperty});
    }

}
