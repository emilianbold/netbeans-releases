
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

package org.netbeans.modules.sql.project;
import java.io.IOException;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
/**
 *
 * @author sgenipudi
 */
public class SQLproProjectHelper {

    private SQLproProject mProject = null;
    private static SQLproProjectHelper mInstance = null;

    /** Creates a new instance of SQLProjectHelper */
    public void setProject(SQLproProject project ) {
        mProject = project;
    }

    public static SQLproProjectHelper getInstance() {
        if (mInstance == null) {
            mInstance = new SQLproProjectHelper();
        }
        return mInstance;
    }

   public void setProjectProperty(String propertyName, String value, boolean save) {
        AntProjectHelper helper =  mProject.getAntProjectHelper();
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        ep.setProperty(propertyName, value);
        helper.putProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH, ep);
        if (save) {
            try {
                Project p = ProjectManager.getDefault().findProject(mProject.getAntProjectHelper().getProjectDirectory());
                ProjectManager.getDefault().saveProject(p);
            }catch (IOException ie) {
                ie.printStackTrace();
            }
        }
    }

   public String getProjectProperty(String propertyName) {
        AntProjectHelper helper =  mProject.getAntProjectHelper();
        EditableProperties ep = helper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH);
        return ep.getProperty(propertyName);
    }
}
