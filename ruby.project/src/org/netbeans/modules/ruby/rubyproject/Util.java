/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2008 Sun Microsystems, Inc. All rights reserved.
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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
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

package org.netbeans.modules.ruby.rubyproject;

import javax.swing.JComboBox;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.ruby.platform.RubyPlatform;
import org.netbeans.modules.ruby.platform.PlatformComponentFactory;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

/**
 * Miscellaneous helper methods for Ruby project types.
 */
public final class Util {

    private static final String LAST_PLATFORM_ID = "projectPanelLastPlatformID"; // NOI18N

    private Util() {
    }

    /**
     * Tries to infer currently best suitable {@link RubyBaseProject Ruby
     * project} based on the current user's context.
     * 
     * @return might be <tt>null</tt>
     */
    public static RubyBaseProject inferRubyProject() {
        // try current context firstly
        Node[] activatedNodes = WindowManager.getDefault().getRegistry().getActivatedNodes();

        if (activatedNodes != null) {
            for (Node n : activatedNodes) {
                RubyBaseProject result = lookupRubyBaseProject(n.getLookup());
                if (result != null) {
                    return result;
                }
            }
        }

        // next try main project
        OpenProjects projects = OpenProjects.getDefault();
        Project mainProject = projects.getMainProject();
        if (mainProject != null) {
            RubyBaseProject result = lookupRubyBaseProject(mainProject.getLookup());
            if (result != null) {
                return result;
            }
        }

        // next try other opened projects
        for (Project project : projects.getOpenProjects()) {
            RubyBaseProject result = lookupRubyBaseProject(project.getLookup());
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    private static RubyBaseProject lookupRubyBaseProject(final Lookup lookup) {
        // try directly
        RubyBaseProject result = lookup.lookup(RubyBaseProject.class);
        if (result != null) {
            return result;
        }
        // try through Project instance
        Project project = lookup.lookup(Project.class);
        if (project != null) {
            result = project.getLookup().lookup(RubyBaseProject.class);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    public static void preselectWizardPlatform(final JComboBox platforms) {
        org.netbeans.modules.ruby.platform.Util.preselectPlatform(platforms, LAST_PLATFORM_ID);
    }

    public static void storeWizardPlatform(JComboBox platforms) {
        RubyPlatform selectedPlatform = PlatformComponentFactory.getPlatform(platforms);
        if (selectedPlatform != null) {
            org.netbeans.modules.ruby.platform.Util.getPreferences().put(LAST_PLATFORM_ID, selectedPlatform.getID());
        }
    }
    
    /**
     * Strips extension from the given filename and return the result. Returns
     * given filename if there is no extension.
     */
    public static String stripExtension(final String fileName, final String ext) {
        int extIndex = fileName.lastIndexOf('.');
        String extension = extIndex == -1 ? "" : fileName.substring(extIndex);
        return ext.equals(extension) ? fileName.substring(0, extIndex) : fileName;
    }
    
}
