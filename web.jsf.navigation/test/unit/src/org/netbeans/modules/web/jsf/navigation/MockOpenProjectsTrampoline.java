/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.navigation;

import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.project.uiapi.OpenProjectsTrampoline;

/**
 *
 * @author joelle
 */
public class MockOpenProjectsTrampoline implements OpenProjectsTrampoline {

    private Collection<Project> openProjects = new ArrayList<Project>();

        public MockOpenProjectsTrampoline() {
        }



        public Project[] getOpenProjectsAPI() {
            Project[] projects = new Project[openProjects.size()];
            openProjects.toArray(projects);
            return projects;
        }

        public void openAPI(Project[] projects, boolean openRequiredProjects) {
            for (Project project : projects) {
                openProjects.add(project);
                mainProject = project;
            }
        }

        public void closeAPI(Project[] projects) {
            for (Project project : projects) {
                openProjects.remove(project);
            }
        }

        public void addPropertyChangeListenerAPI(PropertyChangeListener listener, Object source) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        public void removePropertyChangeListenerAPI(PropertyChangeListener listener) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        private Project mainProject;

        public Project getMainProject() {
            return mainProject;
        }

        public void setMainProject(Project project) {
            if (mainProject != null && !openProjects.contains(mainProject)) {
                throw new IllegalArgumentException("Project " + ProjectUtils.getInformation(mainProject).getDisplayName() + " is not open and cannot be set as main.");
            }
            this.mainProject = mainProject;
        }

}
