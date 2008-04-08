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

package org.netbeans.modules.j2ee.ejbjarproject;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.util.Exceptions;

/**
 * A helper class to easily read and write properties.
 * @author Andrei Badea
 */
public final class PropertyHelper {
    // TODO: AB: maybe in the future add methods to evaluate properties

    private Project project;
    private UpdateHelper updateHelper;

    /** Creates a new instance of PropertyHelper */
    public PropertyHelper(Project project, UpdateHelper updateHelper) {
        this.project = project;
        this.updateHelper = updateHelper;
    }
    
    /** 
     * Returns the property as a string
     * @param path the property path
     * @param key the property name
     * @returns the property value or null if it was not defined
     */
    public String getProperty(String path, String key) {
        return getProperties(path).getProperty(key);
    }
    
    /** 
     * Sets the property as a string. It puts the changed properties to
     * the UpdateHelper. The caller should hold ProjectManager.mutex() write access.
     * @param path the property path
     * @param key the property name
     * @param value the property value
     */
    public void setProperty(String path, String key, String value) {
        assert ProjectManager.mutex().isWriteAccess() : "You must have write access to ProjectManager.mutex(). You can also use @see #saveProperty if you only need to save one property";
        
        EditableProperties ep = getProperties(path);
        ep.setProperty(key, value);
        putProperties(path, ep);
    }
    
    /** 
     * Convenience method to set a single property and save the project.
     * Acquires ProjectManager.mutex() write access.
     * @param path the property path
     * @param key the property name
     * @param value the property value
     */
    public void saveProperty(final String path, final String key, final String value) {
        ProjectManager.mutex().writeAccess(new Runnable() {
            public void run() {
                setProperty(path, key, value);
                save();
            }
        });
    }
    
    /** 
     * Convenience method to save the project.
     */
    public void save() {
        assert ProjectManager.mutex().isWriteAccess() : "You must have write access to ProjectManager.mutex().";
        
        try {
            ProjectManager.getDefault().saveProject(project);
        }
        catch (IOException e) {
            Exceptions.printStackTrace(e);
        }
    }
    
    private EditableProperties getProperties(String path) {
        return updateHelper.getProperties(path);
    }
    
    private void putProperties(String path, EditableProperties ep) {
        updateHelper.putProperties(path, ep);
    }
}
