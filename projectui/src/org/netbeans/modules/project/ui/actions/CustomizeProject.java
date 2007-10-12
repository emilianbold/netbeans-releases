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

package org.netbeans.modules.project.ui.actions;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.swing.Action;
import javax.swing.JMenuItem;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.ui.ExitDialog;
import org.netbeans.spi.project.support.ProjectOperations;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.cookies.SaveCookie;
import org.openide.loaders.DataObject;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.Presenter;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;

/** Action for invoking project customizer
 */
public class CustomizeProject extends ProjectAction implements Presenter.Popup {

    private static final String namePattern = NbBundle.getMessage( CustomizeProject.class, "LBL_CustomizeProjectAction_Name" ); // NOI18N
    private static final String namePatternPopup = NbBundle.getMessage( CustomizeProject.class, "LBL_CustomizeProjectAction_Popup_Name" ); // NOI18N
        
    public CustomizeProject() {
        this( null );
    }
    
    public CustomizeProject( Lookup context ) {
        super( (String)null, namePattern, null, context );
        refresh( getLookup() );
    }
            
    
   
    protected void refresh( Lookup context ) {
     
        super.refresh( context );
        
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, null );
                            
        if ( projects.length != 1 || projects[0].getLookup().lookup( CustomizerProvider.class ) == null ) {
            setEnabled( false );
            // setDisplayName( ActionsUtil.formatProjectSensitiveName( namePattern, new Project[0] ) );
        }
        else { 
            setEnabled( true );
            // setDisplayName( ActionsUtil.formatProjectSensitiveName( namePattern, projects ) );
        }
        
        
    }   
    
    public void actionPerformed( Lookup context ) {
    
        Project[] projects = ActionsUtil.getProjectsFromLookup( context, null );
        
        if ( projects.length == 1 ) {
            CustomizerProvider cp = projects[0].getLookup().lookup( CustomizerProvider.class );
            if ( cp != null ) {
                if (!DataObject.getRegistry().getModifiedSet().isEmpty()) {
                    // #50992: danger! Project properties dialog may try to write to the same config files.
                    
                    //#92011 - reducing the frequency of the dialog popping up.
                    Set<DataObject> candidates = new HashSet<DataObject>();
                    List<FileObject> metadataFiles = ProjectOperations.getMetadataFiles(projects[0]);
                                          
                    for (DataObject dobj : DataObject.getRegistry().getModifiedSet()) {
                        // only consider files from our project
                        if (projects[0] == FileOwnerQuery.getOwner(dobj.getPrimaryFile())) {
                            // now check if it's metadata or data - not 100% bulletproof, but should reduce the probability significantly
                            for (FileObject df : metadataFiles) {
                                if (df.equals(dobj.getPrimaryFile()) || 
                                        (df.isFolder() && FileUtil.isParentOf(df, dobj.getPrimaryFile()))) {
                                    candidates.add(dobj);
                                    break;
                                }
                            }
                        }
                    }
                    if (!candidates.isEmpty()) {
                        String saveAll = NbBundle.getMessage(CustomizeProject.class, "CustomizeProject.saveAll");
                        Object ret = DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                                NbBundle.getMessage(CustomizeProject.class, "CustomizeProject.save_modified_files"), 
                                NbBundle.getMessage(CustomizeProject.class, "CustomizeProject.save_modified_title"),
                                NotifyDescriptor.OK_CANCEL_OPTION,
                                NotifyDescriptor.WARNING_MESSAGE,
                                new Object[] {
                                    saveAll,
                                    NotifyDescriptor.CANCEL_OPTION
                                }, 
                                saveAll));
                        if (ret != saveAll) {
                            return;
                        } else {
                            for (DataObject dobj : candidates) {
                                ExitDialog.doSave(dobj);
                            }
                        }
                    }
                }
                cp.showCustomizer();
            }
        }
        
    }
    
    public Action createContextAwareInstance( Lookup actionContext ) {
        return new CustomizeProject( actionContext );
    }
    
    
    // Implementation of Presenter.Popup ---------------------------------------
    
    public JMenuItem getPopupPresenter() {
        JMenuItem popupPresenter = new JMenuItem( this );
        popupPresenter.setText( namePatternPopup );
        popupPresenter.setIcon( null );

        return popupPresenter;
    }
    
}
