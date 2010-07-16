
/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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

package org.netbeans.modules.visualweb.dataconnectivity.datasource;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.util.WeakListeners;
import org.openide.windows.TopComponent;

/**
 *
 * @author JohnBaker
 */
public class CurrentProject {
    private static CurrentProject _instance = null;    
    private Project project = null;
    protected ProjectsChangedListener changedProjectsListener = new ProjectsChangedListener();
        
    /** Creates a new instance of CurrentProject */
    private CurrentProject() {                        
        DataObject obj = Utilities.actionsGlobalContext().lookup(DataObject.class);
        OpenProjects.getDefault().addPropertyChangeListener(WeakListeners.create(PropertyChangeListener.class, changedProjectsListener, OpenProjects.getDefault()));
        
        if (obj != null) {
            FileObject fileObject = obj.getPrimaryFile();
            project = FileOwnerQuery.getOwner(fileObject);            
        }                         
    }

    
    public static CurrentProject getInstance() {
        if (_instance == null) {
            _instance = new CurrentProject();                    
        } 
                
        return _instance;
    }
    
    public void setProject(Project project) {
        this.project = project;
    }

    /**
     * getOpenedProject returns the project that is being opened 
     */     
    public Project getOpenedProject() {
        if (TopComponent.getRegistry().getActivated() != null) {
            Lookup lookup = TopComponent.getRegistry().getActivated().getLookup();
            DataObject obj = lookup.lookup(DataObject.class);

            if (obj != null) {
                FileObject fileObject = obj.getPrimaryFile();
                project = FileOwnerQuery.getOwner(fileObject);
            }
        }
        
        // When a project is opened, get the main project if Projects window is not in focus
        if (project == null) {
            project = OpenProjects.getDefault().getMainProject();
        }

        return project;
    }
    
    // Return project associated with the current page
    public Project getCurrentProject(DesignBean[] designBeans) {
        DesignContext context = designBeans[0].getDesignContext();
        FacesModel model = ((LiveUnit) context).getModel();
        project = model.getProject();
        return project;
    }
      
    public class ProjectsChangedListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent event) {
            
            // The list of open projects has changed; clean up any old projects we may be holding on to.
            if (OpenProjects.PROPERTY_OPEN_PROJECTS.equals(event.getPropertyName())) {                
                List<Project> oldOpenProjectsList = Arrays.asList((Project[]) event.getOldValue());
                List<Project> newOpenProjectsList = Arrays.asList((Project[]) event.getNewValue());
                Set<Project> closedProjectsSet = new LinkedHashSet<Project>(oldOpenProjectsList);
                closedProjectsSet.removeAll(newOpenProjectsList);
                for (Project project : closedProjectsSet) {
                    // Project has been closed; null the project
                    if (_instance.project == project) {
                        _instance.project = null;
                        OpenProjects.getDefault().removePropertyChangeListener(this);
                    }                                         
                }                                               
            }
        }
    }
               
}
