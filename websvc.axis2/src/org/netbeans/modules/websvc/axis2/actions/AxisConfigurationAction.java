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
package org.netbeans.modules.websvc.axis2.actions;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.websvc.axis2.Axis2ModelProvider;
import org.netbeans.modules.websvc.axis2.AxisUtils;
import org.netbeans.modules.websvc.axis2.TransformerUtils;
import org.netbeans.modules.websvc.axis2.config.model.Axis2ComponentFactory;
import org.netbeans.modules.websvc.axis2.config.model.Axis2Model;
import org.netbeans.modules.websvc.axis2.config.model.Libraries;
import org.netbeans.modules.websvc.axis2.config.model.LibraryRef;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;

public class AxisConfigurationAction extends NodeAction  {
    
    public String getName() {
        return NbBundle.getMessage(AxisConfigurationAction.class, "LBL_AxisConfigAction");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
        
    @Override
    protected boolean asynchronous() {
        return true;
    }
    
    protected boolean enable(Node[] activatedNodes) {
        return activatedNodes!=null && activatedNodes.length == 1;
    }
    
    protected void performAction(Node[] activatedNodes) {

        Project project = activatedNodes[0].getLookup().lookup(Project.class);
        LibrarySelectionPanel configPanel = new LibrarySelectionPanel(project);
        DialogDescriptor dialog = new DialogDescriptor(configPanel, NbBundle.getMessage(AxisConfigurationAction.class,"LBL_Add_Libraries"));
        DialogDisplayer.getDefault().notify(dialog);
        if (dialog.getValue() == DialogDescriptor.OK_OPTION) {
            Axis2ModelProvider axis2ModelProvider = project.getLookup().lookup(Axis2ModelProvider.class);
            Axis2Model axis2Model = axis2ModelProvider.getAxis2Model();
            if (axis2Model != null) {
                List<URL> selected = configPanel.getSelectedLibraries();
                for (URL url:selected) {
                    try {
                        File f = new File(url.toURI());
                        if (!f.exists()) selected.remove(url);                  
                    } catch (URISyntaxException ex) {
                        ex.printStackTrace();
                        selected.remove(url);
                    }
                }
                boolean axisModelChanged = false;
                axis2Model.startTransaction();
                if (selected.size() > 0) {                   
                    Libraries libs = axis2Model.getRootComponent().getLibraries();
                    if (libs == null) {
                        libs = axis2Model.getFactory().createLibraries();
                        for (URL url:selected) {
                            LibraryRef ref = axis2Model.getFactory().createLibraryRef();
                            ref.setNameAttr(AxisUtils.getJarName(url));
                            libs.addLibraryRef(ref);
                        }
                        axis2Model.getRootComponent().setLibraries(libs);
                        axisModelChanged = true;
                    } else {                        
                        if (mergeLibraries(axis2Model.getFactory(), libs, selected)) {
                            axisModelChanged = true;
                        }
                    }
                } else {
                    if (axis2Model.getRootComponent().getLibraries() != null) {
                        axis2Model.getRootComponent().setLibraries(null);
                        axisModelChanged = true;
                    }
                }
                axis2Model.endTransaction();

                if (axisModelChanged) {
                    // save axis2.xml
                    FileObject axis2Folder = AxisUtils.getNbprojectFolder(project.getProjectDirectory());
                    if (axis2Folder != null) {
                        FileObject axis2Fo = axis2Folder.getFileObject("axis2.xml"); //NOI18N
                        if (axis2Fo != null) {
                            try {
                                DataObject dObj = DataObject.find(axis2Fo);
                                if (dObj != null) {
                                    SaveCookie save = dObj.getCookie(SaveCookie.class);
                                    if (save != null) save.save();
                                }
                            } catch (IOException ex) {

                            }
                        }              
                    }
                    try {
                        TransformerUtils.transform(project.getProjectDirectory());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        }
    }
    
    private boolean mergeLibraries(Axis2ComponentFactory factory, Libraries libs, List<URL> selected) {
        boolean modelChanged = false;
        List<LibraryRef> oldRefs = libs.getLibraryRefs();
        Set<LibraryRef> common = new HashSet<LibraryRef>();
        for (LibraryRef oldRef:oldRefs) {
            for (URL url:selected) {
                if (AxisUtils.getJarName(url).equals(oldRef.getNameAttr())) {
                    common.add(oldRef);
                    selected.remove(url);
                    break;
                }
            }
        }
        
        if (common.size() != oldRefs.size() || selected.size() > 0) {
            // removing old libraries;
            for (LibraryRef oldRef:oldRefs) {
                if (!common.contains(oldRef)) {               
                    libs.removeLibraryRef(oldRef);
                }
            }
            // adding new libraries;
            for (URL url:selected) {
                String jarName = AxisUtils.getJarName(url);
                LibraryRef newRef = factory.createLibraryRef();
                newRef.setNameAttr(jarName);
                libs.addLibraryRef(newRef);
            }
            modelChanged = true;
        }
        
        return modelChanged;
    }

}

