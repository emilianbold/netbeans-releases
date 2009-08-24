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

package org.netbeans.modules.project.ui;

import java.awt.EventQueue;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Various hacks that should be solved better later.
 */
public class Hacks {

    private Hacks() {}
    
    private static final String BUILD_NUMBER = System.getProperty("netbeans.buildnumber"); // NOI18N

    static final RequestProcessor RP = new RequestProcessor("Project UI"); // NOI18N
    
    /**
     * Show name of project corresponding to selection in Main Window title bar.
     * @author Jesse Glick
     */
    static void keepCurrentProjectNameUpdated() {
        final TopComponent.Registry r = TopComponent.getRegistry();
        final RequestProcessor.Task task = RP.create(new Runnable() {
            public void run() {
                Node[] sel = r.getActivatedNodes();
                Set<Project> projects = new HashSet<Project>();
                boolean noProjectsOpen = OpenProjectList.getDefault().getOpenProjects().length == 0;
                for (int i = 0; i < sel.length; i++) {
                    Lookup l = sel[i].getLookup();
                    Project p = l.lookup(Project.class);
                    if (p != null) {
                        projects.add(p);
                        if (noProjectsOpen) {
                            Logger.getLogger(Hacks.class.getName())./*XXX for now*/fine("Activated node selection " + Arrays.toString(sel) +
                                    " contains nonopen project " + p + " though none are open; leak? activated TC: " + r.getActivated() +
                                    " current nodes: " + Arrays.toString(r.getCurrentNodes()) + " (report in issue #102805)");
                        }
                    } else {
                        DataObject d = l.lookup(DataObject.class);
                        if (d != null) {
                            FileObject f = d.getPrimaryFile();
                            p = FileOwnerQuery.getOwner(f);
                            if (p != null) {
                                projects.add(p);
                            }
                        }
                    }
                }
                final String pname;
                if (projects.size() == 1) {
                    Project p = projects.iterator().next();
                    pname = ProjectUtils.getInformation(p).getDisplayName();
                    assert pname != null : p;
                } else if (projects.isEmpty()) {
                    pname = null;
                } else {
                    pname = NbBundle.getMessage(Hacks.class, "LBL_MultipleProjects");
                }
                Project p = OpenProjectList.getDefault().getMainProject();
                final String mname = p != null? 
                    ProjectUtils.getInformation(p).getDisplayName():
                    NbBundle.getMessage(Hacks.class, "LBL_NoMainProject");
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        // depends on exported keys in core/windows
                        String format = NbBundle.getBundle("org.netbeans.core.windows.view.ui.Bundle").
                                getString(pname != null ? "CTL_MainWindow_Title" : "CTL_MainWindow_Title_No_Project");
                        String title = pname != null?
                            // Note that currently mname is ignored.
                            MessageFormat.format(format, BUILD_NUMBER, pname, mname) :
                            MessageFormat.format(format, BUILD_NUMBER, mname);
                        WindowManager.getDefault().getMainWindow().setTitle(title);
                    }
                });
            }
        });
        r.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent ev) {
                if (TopComponent.Registry.PROP_ACTIVATED_NODES.equals(ev.getPropertyName())) {
                    task.schedule(200);
                }
            }
        });
    }
    
    
    /** Forces reload of panels in TemplateWizard. Public method updateState doesn't re-read
     * the new panels from new iterator.
     * Note: it should be solved better either fixing TemplateWizard or implement
     * whole NewFileWizard (w/o TemplateWizard).
     *
     * @param tw instance of TemplateWizard (thus NewFileWizard)
     * @param dobj template
     */    
    static void reloadPanelsInWizard (final TemplateWizard tw, final DataObject dobj) {
        try {
            Class<?> twClazz = Class.forName(
                "org.openide.loaders.TemplateWizard", true, // NOI18N
                Thread.currentThread().getContextClassLoader());
            if (twClazz != null) {
                Method reloadPanels = twClazz.getDeclaredMethod("reload", DataObject.class); // NOI18N
                reloadPanels.setAccessible (true);
                reloadPanels.invoke(tw, dobj);
            }
        } catch (Exception e) {
            // XXX
            e.printStackTrace();
        }
    }
}
