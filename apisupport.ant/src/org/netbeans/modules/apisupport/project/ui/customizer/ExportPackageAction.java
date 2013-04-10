/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.apisupport.project.ui.customizer;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.SortedSet;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.apisupport.project.ApisupportAntUtils;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.openide.ErrorManager;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.awt.DynamicMenuContent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ContextAwareAction;
import org.openide.util.Lookup;
import org.openide.util.Mutex;
import org.openide.util.MutexException;
import org.openide.util.NbBundle.Messages;

/**
 * Submenu which permits the user to export or unexport appropriate package.
 * Must be locate in NetBeans Module Project.
 */
@ActionID(
        category = "Project",
        id = "org.netbeans.modules.apisupport.project.ExportPackageAction")
@ActionRegistration(
        displayName = "#CTL_ExportPackageAction", lazy = false)
@ActionReferences({
    @ActionReference(path = "Projects/package/Actions", position = 100),
})
@Messages({"CTL_UnexportPackageAction=Unexport Package","CTL_ExportPackageAction=Export Package"})
public final class ExportPackageAction extends AbstractAction implements ContextAwareAction{

    @Override
    public void actionPerformed(ActionEvent ev) {
        //well, since someone can assign a shortcut ti the action, the invokation is unvaiodable, make it noop        
        //assert false : "Action should never be called without a context";
    }

    @Override
    public Action createContextAwareInstance(Lookup actionContext) {
        Collection<FileObject> selectedPackages = (Collection<FileObject>) actionContext.lookupAll(FileObject.class);
        Project project = FileOwnerQuery.getOwner(selectedPackages.iterator().hasNext()?selectedPackages.iterator().next():null);
        NbModuleProject nbmProject = null;
        if((nbmProject = project.getLookup().lookup(NbModuleProject.class)) != null)
        {
            Collection<String> packages = new ArrayList<String>();
            SortedSet<String> availablePublicPackages = ApisupportAntUtils.scanProjectForPackageNames(FileUtil.toFile(nbmProject.getProjectDirectory()), false);
            final SingleModuleProperties properties = SingleModuleProperties.getInstance(nbmProject);
            String packageNameIter = "";
            boolean export = false;
            for (Iterator<FileObject> it = selectedPackages.iterator(); it.hasNext();) {
                FileObject packageIter = it.next();
                if(!availablePublicPackages.contains(packageNameIter = packageIter.getPath().substring(nbmProject.getSourceDirectory().getPath().length()+1).replace('/', '.'))) {
                    continue;
                }
                packages.add(packageNameIter);
                if(!properties.getPublicPackagesModel().getSelectedPackages().contains(packageNameIter) && !export) {
                    export = true;
                }
            }
            return new ContextAction(!packages.isEmpty(), nbmProject, packages, properties, export);
        }
        return new ContextAction(false);
    }

    /**
     * The particular instance of this action for a given package(s).
     */
    private static final class ContextAction extends AbstractAction {

        private NbModuleProject nbmProject;
        
        private final SingleModuleProperties properties;
        
        private Collection<String> packages;
        
        private boolean export;
        
        public ContextAction(boolean enabled) {
            this(enabled, null, null, null, true);
        }
        
        public ContextAction(boolean enabled, NbModuleProject nbmProject, Collection<String> packages, SingleModuleProperties properties, boolean export) {
            super(export?Bundle.CTL_ExportPackageAction():Bundle.CTL_UnexportPackageAction());
            this.nbmProject = nbmProject;
            this.packages = packages;
            this.properties = properties;
            this.export = export;
            this.putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
            this.setEnabled(enabled);
        }
        
        @Override
        public void actionPerformed(ActionEvent evt) {
            CustomizerComponentFactory.PublicPackagesTableModel tableModel = properties.getPublicPackagesModel();
            for(String packageIter:this.packages) {
                for(int i = 0; i < tableModel.getRowCount(); i++) {
                    if(tableModel.getValueAt(i, 1).equals(packageIter)) {
                        tableModel.setValueAt(this.export, i, 0);
                        break;
                    }
                }
            }
            try {
            ProjectManager.mutex().writeAccess(new Mutex.ExceptionAction<Void>() {
                @Override public Void run() throws IOException {
                    properties.storeProperties();
                    ProjectManager.getDefault().saveProject(nbmProject);
                    return null;
                }
            });
            } catch (MutexException e) {
                ErrorManager.getDefault().notify((IOException)e.getException());
            }
        }
        
    }
}