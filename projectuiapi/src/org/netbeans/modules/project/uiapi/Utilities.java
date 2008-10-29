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

package org.netbeans.modules.project.uiapi;

import java.util.HashMap;
import java.util.Map;
import org.netbeans.spi.project.ui.support.ProjectCustomizer;
import org.openide.util.Lookup;

/**
 * Way of getting implementations of UI components defined in projects/projectui.
 * @author Petr Hrebejk, Jesse Glick
 */
public class Utilities {

    private static final Map<ProjectCustomizer.Category,CategoryChangeSupport> CATEGORIES = new HashMap<ProjectCustomizer.Category,CategoryChangeSupport>();

    private Utilities() {}

    /** Gets action factory from the global Lookup.
     */
    public static ActionsFactory getActionsFactory() {
        ActionsFactory instance = Lookup.getDefault().lookup(ActionsFactory.class);
        assert instance != null : "Need to have " + ActionsFactory.class.getName() + " instance in the default lookup";
        return instance;
    }

    /** Gets BuildSupportImpl from the global Lookup.
     */
    public static BuildExecutionSupportImplementation getBuildExecutionSupportImplementation() {
        BuildExecutionSupportImplementation instance = Lookup.getDefault().lookup(BuildExecutionSupportImplementation.class);
        assert instance != null : "Need to have " + BuildExecutionSupportImplementation.class.getName() + " instance in the default lookup";
        return instance;
    }
    
    /** Gets the projectChooser factory from the global Lookup
     */
    public static ProjectChooserFactory getProjectChooserFactory() {
        ProjectChooserFactory instance = Lookup.getDefault().lookup(ProjectChooserFactory.class);
        assert instance != null : "Need to have " + ProjectChooserFactory.class.getName() + " instance in the default lookup";
        return instance;
    }
    
    /** Gets an object the OpenProjects can delegate to
     */
    public static OpenProjectsTrampoline getOpenProjectsTrampoline() {
        OpenProjectsTrampoline instance = Lookup.getDefault().lookup(OpenProjectsTrampoline.class);
        assert instance != null : "Need to have " + OpenProjectsTrampoline.class.getName() + " instance in the default lookup";
        return instance;
    }
    
    public static CategoryChangeSupport getCategoryChangeSupport(ProjectCustomizer.Category category) {
        CategoryChangeSupport cw = Utilities.CATEGORIES.get(category);
        return cw == null ? CategoryChangeSupport.NULL_INSTANCE : cw;
    }
    
    public static void putCategoryChangeSupport(
            ProjectCustomizer.Category category, CategoryChangeSupport wrapper) {
        Utilities.CATEGORIES.put(category, wrapper);
    }
    
    public static void removeCategoryChangeSupport(ProjectCustomizer.Category category) {
        Utilities.CATEGORIES.remove(category);
    }
    
}
