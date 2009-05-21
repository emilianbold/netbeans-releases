/*
 * OpenProjectsTrampolineImpl.java
 *
 * Created on March 14, 2007, 10:23 AM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema;

import java.beans.PropertyChangeListener;
import java.util.concurrent.Future;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.uiapi.OpenProjectsTrampoline;

/**
 *
 * @author Sonali
 */
public class OpenProjectsTrampolineImpl  implements OpenProjectsTrampoline {
    
    /** Creates a new instance of OpenProjectsTrampolineImpl */
    Project project;
    
    public OpenProjectsTrampolineImpl() {
    }

    public Project[] getOpenProjectsAPI() {
        Project[] projects = new Project[1];
        try {
        projects[0] = Util.createJavaTestProject();
        } catch(Exception e){}
       
        return projects;
    }

    public void openAPI(Project[] project, boolean b, boolean showProgress) {
    }

    public void closeAPI(Project[] project) {
    }

    public void addPropertyChangeListenerAPI(PropertyChangeListener propertyChangeListener) {
    }

    public void removePropertyChangeListenerAPI(PropertyChangeListener propertyChangeListener) {
    }

    public Project getMainProject() {
        try {
        return Util.createJavaTestProject();
        } catch (Exception e){}
        return null;
    }

    public void setMainProject(Project project) {
        this.project =project;
    }

    public void addPropertyChangeListenerAPI(PropertyChangeListener listener,
                                             Object source) {
    }

    public Future<Project[]> openProjectsAPI() {
        return null;
    }
    
}
