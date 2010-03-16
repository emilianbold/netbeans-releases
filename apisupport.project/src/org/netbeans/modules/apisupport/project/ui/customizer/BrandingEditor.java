/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.Dialog;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.project.Project;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.openide.util.NbBundle;

/**
 * Creates a multi-view TopComponent from <code>AbstractBrandingPanel</code>s.
 *
 * @see AbstractBrandingPanel
 * 
 * @author S. Aubrecht
 */
public class BrandingEditor {

    private static final Map<Project, Dialog> project2dialog = new HashMap<Project, Dialog>(10);

    /**
     * Open branding editor for given suite project.
     * @param suite
     */
    public static void open( SuiteProject suite ) {
        SuiteProperties properties = new SuiteProperties(suite, suite.getHelper(), suite.getEvaluator(), SuiteUtils.getSubProjects(suite));
        BasicBrandingModel model = properties.getBrandingModel();
        open( NbBundle.getMessage(BrandingEditor.class, "Title_BrandingEditor", properties.getProjectDisplayName()), suite, model, true );
    }

    /**
     * Open branding editor for given generic project.
     * @param displayName Branding editor's display name.
     * @param p Project to be branded.
     * @param model Branding model.
     * @param contextAvailable True if the given project knows which platform
     * app it belongs and the platform jars/projects are available, false otherwise.
     */
    public static void open( String displayName, final Project p, BasicBrandingModel model, boolean contextAvailable ) {
        synchronized( project2dialog ) {
            Dialog dlg = project2dialog.get(p);
            if( null == dlg ) {
                BrandingEditorPanel editor = new BrandingEditorPanel(displayName, p, model, contextAvailable);
                dlg = editor.open();
                project2dialog.put(p, dlg);
                dlg.addWindowListener( new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        synchronized( project2dialog ) {
                            project2dialog.remove(p);
                        }
                    }
                });
            } else {
                dlg.setVisible(true);
                dlg.requestFocusInWindow();
            }
        }
    }


    private BrandingEditor() {
    }
}
