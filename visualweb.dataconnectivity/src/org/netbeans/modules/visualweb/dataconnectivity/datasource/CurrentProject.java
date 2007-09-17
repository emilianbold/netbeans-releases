
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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.visualweb.dataconnectivity.datasource;

import com.sun.rave.designtime.DesignBean;
import com.sun.rave.designtime.DesignContext;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.visualweb.insync.live.LiveUnit;
import org.netbeans.modules.visualweb.insync.models.FacesModel;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.Utilities;
import org.openide.windows.TopComponent;

/**
 *
 * @author JohnBaker
 */
public class CurrentProject {
    private static CurrentProject _instance = null;
    private Project project = null;
        
    /** Creates a new instance of CurrentProject */
    private CurrentProject() {                        
        DataObject obj = Utilities.actionsGlobalContext().lookup(DataObject.class);
        
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
               
}
