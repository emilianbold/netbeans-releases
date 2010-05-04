/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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
 * Contributor(s): theanuradha@netbeans.org
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */
package org.netbeans.modules.maven.actions.scm;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import org.apache.maven.model.Scm;
import org.apache.maven.project.MavenProject;
import org.netbeans.modules.maven.actions.scm.ui.CheckoutUI;
import org.netbeans.modules.maven.api.execute.RunConfig;
import org.netbeans.modules.maven.api.execute.RunUtils;

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 * @deprecated Replaced by actions in Artifact viewer window, see #164992
 * @author Anuradha G
 */
public class CheckoutAction extends AbstractAction implements LookupListener {

    private Lookup lookup;
    private Lookup.Result<MavenProject> result;

    public CheckoutAction(Lookup lkp) {
        this.lookup = lkp;
        putValue(NAME, NbBundle.getMessage(CheckoutAction.class, "LBL_Checkout"));
        //TODO proper icon
        putValue(SMALL_ICON, ImageUtilities.image2Icon(ImageUtilities.loadImage("org/netbeans/modules/maven/actions/scm/update.png", true))); //NOI18N
        putValue("iconBase", "org/netbeans/modules/maven/actions/scm/update.png"); //NOI18N
        result = lookup.lookupResult(MavenProject.class);
        setEnabled(getScm() != null);
        result.addLookupListener(this);
    }

    private Scm getScm() {
        Iterator<? extends MavenProject> prj = result.allInstances().iterator();
        if (!prj.hasNext()) {
            return null;
        }
        MavenProject project = prj.next();
        return project.getScm();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Iterator<? extends MavenProject> prj = result.allInstances().iterator();
        if (!prj.hasNext()) {
            return;
        }
        MavenProject project = prj.next();

        CheckoutUI checkoutUI = new CheckoutUI(project);
        DialogDescriptor dd = new DialogDescriptor(checkoutUI,  NbBundle.getMessage(CheckoutAction.class, "LBL_Checkout"));
        dd.setClosingOptions(new Object[]{
            checkoutUI.getCheckoutButton(),
            DialogDescriptor.CANCEL_OPTION
        });
        dd.setOptions(new Object[]{
            checkoutUI.getCheckoutButton(),
            DialogDescriptor.CANCEL_OPTION
        });
        Object ret = DialogDisplayer.getDefault().notify(dd);
        if (checkoutUI.getCheckoutButton() == ret) {
            final RunConfig rc = checkoutUI.getRunConfig();
            if (!rc.getExecutionDirectory().exists()) {
                rc.getExecutionDirectory().mkdirs();
            }
            final File checkoutDir = checkoutUI.getCheckoutDirectory();
            ExecutorTask task = RunUtils.executeMaven(rc);
            task.addTaskListener(new TaskListener() {
                @Override
                public void taskFinished(Task task) {
                    FileObject fo = FileUtil.toFileObject(checkoutDir);
                    if (fo != null) {
                        try {
                            Project prj = ProjectManager.getDefault().findProject(fo);
                            if (prj != null) {
                                OpenProjects.getDefault().open(new Project[] {prj}, false);
                            }
                        } catch (IOException ex) {
                            Exceptions.printStackTrace(ex);
                        } catch (IllegalArgumentException ex) {
                            Exceptions.printStackTrace(ex);
                        }
                    }
                }
            });
        }
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                setEnabled(getScm() != null);
            }
        });
    }
}
