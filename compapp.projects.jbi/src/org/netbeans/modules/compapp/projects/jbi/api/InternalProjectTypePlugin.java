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
package org.netbeans.modules.compapp.projects.jbi.api;

import org.netbeans.api.project.Project;
import java.util.List;
import java.awt.*;

/**
 * Service Provider Interface for internal JBI projects.
 * Port types are generic.
 * 
 * This interface is still being defined and may change at any moment.
 * 
 * @author jsandusky
 */
public interface InternalProjectTypePlugin {

    /**
     * Name for this JBI project type.
     * @return display name
     */
    String getPluginName();

    /**
     * Resource path to the icon that corresponds to this JBI project type.
     * @return icon
     */
    String getIconFileBase();

    /**
     * Category name for the JBI project type.
     * JBI project types that correspond to the same category name will be
     * grouped together (i.e. for example, on a palette).
     *
     * use null, if it should not be added into any category
     *
     * @return category name
     */
    String getCategoryName();

    /**
     * Obtains the WizardIterator that can show a new project
     * wizard to the user (if necessary), and then create the project.
     * @return the wizard iterator
     */
    InternalProjectTypePluginWizardIterator getWizardIterator();

    /**
     * Opens whatever editor that corresponds to the JBI project type.
     * The user may need to rebuild the composite application project in order
     * for any external edits to be applied.
     * @param project the project object to open an editor for
     */
    void openEditor(Project project);


    // 11/08/07, extension for generic plug-in projects, e.g., ear-link

    /**
     * Return the JBI target component name of this project type.
     *
     * @return the name of SU target
     */
    String getJbiTargetName();

    /**
     * get the list of action performer from the plug-in project type
     *
     * @return  a list of action performer
     */
    List<JbiProjectActionPerformer>  getProjectActions();

    /**
     * can a project be created to represent this source object?
     *
     * @param source the source object
     * @return true if the object is associated with the project type
     */
    boolean isAcceptableProjectSource(Object source);

    /**
     * Has project properties customizer
     *
     * @return true if has project properties customizer
     */
    boolean hasCustomizer();

    /**
     * Get the project properties customizer
     *
     * @param project the project instance
     * @return the project properties customizer
     */
    Component getCustomizer(Project project);
}
