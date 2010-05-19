/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javacard.apdufile;

import java.io.File;
import java.util.Properties;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javacard.spi.ProjectKind;
import org.netbeans.spi.actions.Single;
import org.netbeans.spi.project.support.ant.GeneratedFilesHelper;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;

/**
 *
 * @author Tim
 */
public class SendAction extends Single<DataObject> {
    public SendAction() {
        super (DataObject.class, NbBundle.getMessage(SendAction.class,
                "ACTION_SEND_APDU"), null); //NOI18N
    }

    public static FileObject findBuildXml(Project project) {
        return project.getProjectDirectory().getFileObject(
                GeneratedFilesHelper.BUILD_XML_PATH);
    }

    @Override
    protected void actionPerformed(DataObject dataObject) {
        try {
            Project owner = FileOwnerQuery.getOwner(dataObject.getPrimaryFile());
            ProjectKind kind = owner.getLookup().lookup(ProjectKind.class);
            if (kind == null || !kind.isApplet()) {
                NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle.getMessage(SendAction.class,
                        "ERR_NO_PROJECT"), NotifyDescriptor.ERROR_MESSAGE); //NOI18N
                DialogDisplayer.getDefault().notify(d);
                return;
            }
            FileObject buildFo = findBuildXml(owner);
            if (buildFo == null || !buildFo.isValid()) {
                //The build.xml was deleted after the isActionEnabled was called
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(SendAction.class,
                        "ERR_NO_BUILD_SCRIPT"), NotifyDescriptor.ERROR_MESSAGE); //NOI18N
                DialogDisplayer.getDefault().notify(nd);
                return;
            }

            Properties p = new Properties();
            File file = FileUtil.toFile(dataObject.getPrimaryFile());
            if (file == null) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(NbBundle.getMessage(SendAction.class,
                        "ERR_NOT_REGULAR_FILE", dataObject.getName()), //NOI18N
                        NotifyDescriptor.ERROR_MESSAGE);
                DialogDisplayer.getDefault().notify(nd);
                return;
            }
            p.setProperty("apdu.script.file", FileUtil.toFile( //NOI18N
                    dataObject.getPrimaryFile()).getAbsolutePath());
            ActionUtils.runTarget(buildFo, new String[] {"--run-apdutool--"}, p); //NOI18N
        } catch (Exception ex) {
            Exceptions.printStackTrace(ex);
        }
    }

}
