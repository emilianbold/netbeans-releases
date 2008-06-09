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
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.projectimport.eclipse.core;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.RequestProcessor;

/**
 *
 * @author david
 */
public class ProjectOpenHookImpl extends ProjectOpenedHook{

    private UpgradableProject upgradable;
    private Project project;
    
    public ProjectOpenHookImpl(Project project, UpgradableProject upgradable) {
        this.upgradable = upgradable;
        this.project = project;
    }
    
    @Override
    protected void projectOpened() {
        // do not execute this during project opening - postponed for 5sec right now
        RequestProcessor.getDefault().post(new Runnable() {
            public void run() {
                if (!upgradable.isUpgradable()) {
                    return;
                }
                if (!upgradable.isEclipseProjectReachable()) {
                    // perhaps notify user that eclipse projetc is not avialable and let them fix it
                    DialogDisplayer.getDefault().notify(
                        // TODO
                        new NotifyDescriptor.Message("Eclipse project files cannot be reach. Do you want to resolve them? TBD."));
                } else {
                    if (upgradable.isUpgradable() && !upgradable.isUpToDate()) {
                        Object answer = DialogDisplayer.getDefault().notify(
                            // TODO
                            new NotifyDescriptor.Confirmation("Eclipse project has changed. Do you want to update NetBeans project accordingly?", 
                            FileUtil.getFileDisplayName(project.getProjectDirectory())));
                        if (answer == NotifyDescriptor.YES_OPTION) {
                            try {
                                upgradable.update();
                            } catch (IOException ex) {
                                Exceptions.printStackTrace(ex);
                            }
                        }
                    }
                }
            }
        }, 5000);
    }

    @Override
    protected void projectClosed() {
    }

}
