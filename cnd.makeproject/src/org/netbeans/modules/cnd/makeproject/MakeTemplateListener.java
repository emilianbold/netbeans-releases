/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.makeproject;

import java.io.File;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.utils.CndPathUtilitities;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectOptions;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.OperationEvent;
import org.openide.loaders.OperationListener;
import org.openide.util.Utilities;


/*
 * see issue #64393
 */
public class MakeTemplateListener implements OperationListener {

    private static final ErrorManager ERR = ErrorManager.getDefault().getInstance(MakeTemplateListener.class.getName());

    @Override
    public void operationPostCreate(OperationEvent operationEvent) {
    }

    @Override
    public void operationCopy(OperationEvent.Copy copy) {
    }

    @Override
    public void operationMove(OperationEvent.Move move) {
    }

    @Override
    public void operationDelete(OperationEvent operationEvent) {
    }

    @Override
    public void operationRename(OperationEvent.Rename rename) {
    }

    @Override
    public void operationCreateShadow(OperationEvent.Copy copy) {
    }

    private MakeConfigurationDescriptor getMakeConfigurationDescriptor(Project p) {
        ConfigurationDescriptorProvider pdp = p.getLookup().lookup(ConfigurationDescriptorProvider.class);

        if (pdp == null) {
            return null;
        }

        return pdp.getConfigurationDescriptor();
    }

    @Override
    public void operationCreateFromTemplate(OperationEvent.Copy copy) {
        Folder folder = Utilities.actionsGlobalContext().lookup(Folder.class);
        Project p = Utilities.actionsGlobalContext().lookup(Project.class);

        if (folder == null || p == null) {
            //maybe a file belonging into a project is selected. Try:
            DataObject od = Utilities.actionsGlobalContext().lookup(DataObject.class);

            if (od == null) {
                //no file:
                return;
            }

            FileObject file = od.getPrimaryFile();

            p = FileOwnerQuery.getOwner(file);

            if (p == null) {
                //no project:
                return;
            }

            File f = FileUtil.toFile(file);

            if (f == null) {
                //not a physical file:
                return;
            }

            //check if the project is a Makefile project:
            MakeConfigurationDescriptor makeConfigurationDescriptor = getMakeConfigurationDescriptor(p);

            if (makeConfigurationDescriptor == null) {
                //no:
                return;
            }

            Item i = makeConfigurationDescriptor.findProjectItemByPath(f.getAbsolutePath());

            if (i == null) {
                //no item, does not really belong into this project:
                return;
            }

            //found:
            folder = i.getFolder();
        }

        if (folder.isDiskFolder()) {
            return; // item is added via Folder.fileDataCreated event
        }
        MakeConfigurationDescriptor makeConfigurationDescriptor = getMakeConfigurationDescriptor(p);

        assert makeConfigurationDescriptor != null;

        FileObject file = copy.getObject().getPrimaryFile();
        Project owner = FileOwnerQuery.getOwner(file);

        if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
            ERR.log(ErrorManager.INFORMATIONAL, "processing file=" + file); // NOI18N
            ERR.log(ErrorManager.INFORMATIONAL, "FileUtil.toFile(file.getPrimaryFile())=" + FileUtil.toFile(file)); // NOI18N
            ERR.log(ErrorManager.INFORMATIONAL, "into folder = " + folder); // NOI18N
            ERR.log(ErrorManager.INFORMATIONAL, "in project = " + p.getProjectDirectory()); // NOI18N
        }

        if (owner != null && owner.getProjectDirectory() == p.getProjectDirectory()) {
            File ioFile = FileUtil.toFile(file);
            if (ioFile.isDirectory()) {
                return;
            } // don't add directories.
            if (!makeConfigurationDescriptor.okToChange()) {
                return;
            }
            String itemPath;
            if (MakeProjectOptions.getPathMode() == MakeProjectOptions.REL_OR_ABS) {
                itemPath = CndPathUtilitities.toAbsoluteOrRelativePath(makeConfigurationDescriptor.getBaseDir(), ioFile.getPath());
            } else if (MakeProjectOptions.getPathMode() == MakeProjectOptions.REL) {
                itemPath = CndPathUtilitities.toRelativePath(makeConfigurationDescriptor.getBaseDir(), ioFile.getPath());
            } else {
                itemPath = ioFile.getPath();
            }
            itemPath = CndPathUtilitities.normalize(itemPath);
            Item item = new Item(itemPath);

            folder.addItemAction(item);

            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "folder: " + folder + ", added: " + file); // NOI18N
            }
        } else {
            if (ERR.isLoggable(ErrorManager.INFORMATIONAL)) {
                ERR.log(ErrorManager.INFORMATIONAL, "not adding: " + file + " because it is not owned by this project"); // NOI18N
            }
        }
    }
}
