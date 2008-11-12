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
package org.netbeans.modules.maven.jaxws.actions;

import org.netbeans.modules.websvc.api.support.AddOperationCookie;
import org.openide.filesystems.FileObject;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.util.actions.NodeAction;

public class AddOperationAction extends NodeAction  {
    
    public String getName() {
        return NbBundle.getMessage(AddOperationAction.class, "LBL_OperationAction");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }
        
    protected boolean asynchronous() {
        return false;
    }
    
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length != 1) return false;
        FileObject implClassFo = activatedNodes[0].getLookup().lookup(FileObject.class);
        return implClassFo != null;
    }
    
    protected void performAction(Node[] activatedNodes) {
        
        FileObject implClassFo = activatedNodes[0].getLookup().lookup(FileObject.class);
        AddOperationCookie addOperationCookie = new JaxWsAddOperation(implClassFo);
        addOperationCookie.addOperation(implClassFo);
//        
//        Project prj = FileOwnerQuery.getOwner(implClassFo);
//        NbMavenProject mp = prj.getLookup().lookup(NbMavenProject.class);
//        System.out.println("mp = "+mp);
//        if (mp != null) {
//            //if jaxws-plugin not in project at all, add to pom.xml!
//            String[] filepaths = PluginPropertyUtils.getPluginPropertyList(prj, "org.codehaus.mojo", "jaxws-maven-plugin", "wsdlFiles", "wsdlFile",
//                    "wsimport");
//            System.out.println("filePath = "+filepaths);
//            if (filepaths != null) {
//                
//            } else {
//                String dirpath = PluginPropertyUtils.getPluginProperty(prj, "org.codehaus.mojo", "jaxws-maven-plugin", "wsdlDirectory", 
//                        "wsimport");
//                if (dirpath == null) {
//                    dirpath = "src/wsdl";
//                }
//                File dir = FileUtilities.resolveFilePath(FileUtil.toFile(prj.getProjectDirectory()), dirpath);
//                System.out.println("dir = "+dir+":"+dir.exists());
//                if (!dir.exists()) dir.mkdirs();
//            }
//        } 
        
    }
}

