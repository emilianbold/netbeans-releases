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

package org.netbeans.modules.compapp.projects.jbi.jeese.actions;

import java.awt.Dialog;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.modules.compapp.javaee.util.JavaEEVerifierMBeanProxy;
import org.netbeans.modules.compapp.javaee.util.JavaEEVerifierReportItem;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;
import org.netbeans.modules.compapp.projects.jbi.jeese.ui.AppVerifierPnl;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.JbiProjectProperties;
import org.netbeans.modules.compapp.projects.jbi.ui.customizer.VisualClassPathItem;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/**
 *
 * @author gpatil
 */
public class AppVerifierAction extends NodeAction {
    private String name = "";//NOI18N
        
    public AppVerifierAction() {
        init();
    }
    
    @Override
    public boolean asynchronous(){
        return false;
    }
    
    private void init() {
        ResourceBundle rb = NbBundle.getBundle(this.getClass());
        name = rb.getString("nameVerifyResource");//NOI18N
    }
    
    public boolean enable(Project project) {
        return true;
    }
    
    protected void performAction(Node[] activatedNodes) {
        Node theNode = activatedNodes[ 0 ];
        
        Lookup lookup = theNode.getLookup();
        JbiProject jbiProject = (JbiProject)lookup.lookup( JbiProject.class );

        VisualClassPathItem vcpi = (VisualClassPathItem)lookup.lookup(
                VisualClassPathItem.class );

        if ( jbiProject != null && vcpi != null ) {
            ResourceBundle rb = NbBundle.getBundle(this.getClass());
            String msgVerifyResult = rb.getString("titleVerifyResult");//NOI18N
            //SA location ${basedir}/${dist.jar}
            FileObject fo = jbiProject.getProjectDirectory();
            try {
                
                List l = (List) jbiProject.getProjectProperties().get(JbiProjectProperties.DIST_JAR);
                String saLocation = ((VisualClassPathItem) l.get(0)).getEvaluated();
                File root = FileUtil.toFile(fo);
                File saZip = new File(root, saLocation);
                if ((saZip == null) || (!saZip.exists())){
                    String msg = NbBundle.getMessage(AppVerifierAction.class, "msg_build_prj");//NOI18N
                    throw new Exception(msg);
                }
                
                URI[] uris = ((AntArtifact) vcpi.getObject()).getArtifactLocations();
                String jarLoc = uris[0].toString();
                int idx = jarLoc.lastIndexOf("/"); //NOI18N
                if (idx == -1){
                    idx = jarLoc.lastIndexOf("\\");//NOI18N
                }
                String suJar = "";//NOI18N
                if (idx != -1){
                    suJar = jarLoc.substring(idx + 1);
                }

                List<JavaEEVerifierReportItem> ri = new ArrayList<JavaEEVerifierReportItem>();
                    

                ri = JavaEEVerifierMBeanProxy.verifyApplication(
                        jbiProject.getAntProjectHelper(), saZip.getAbsolutePath(), suJar);
                if ((ri == null) || (ri.size() <= 0)){
                    String msg = NbBundle.getMessage(AppVerifierAction.class, 
                            "msg_no_verifier_results"); //NOI18N
                    throw new Exception(msg);
                }
                AppVerifierPnl pnl = new AppVerifierPnl(ri);            
                DialogDescriptor dd = new DialogDescriptor(pnl, 
                        " " + msgVerifyResult + vcpi.getProjectName(), true, //NOI18N
                        new Object[] {DialogDescriptor.CLOSED_OPTION}, 
                        DialogDescriptor.CLOSED_OPTION, 
                        DialogDescriptor.DEFAULT_ALIGN, null, null);

                Dialog dlg = DialogDisplayer.getDefault().createDialog( dd );

                dlg.setVisible( true );                
            } catch (Exception ex) {
                NotifyDescriptor nd = new NotifyDescriptor.Message(ex.getMessage());
                DialogDisplayer.getDefault().notify(nd);
                Exceptions.printStackTrace(ex);
            }                         
        }
    }
        
    public boolean enable(Node[] nodes) {
        return true;
    }
    
    public HelpCtx getHelpCtx() {
        return null;
    }
    
    public String getName() {
        return name;
    }
}