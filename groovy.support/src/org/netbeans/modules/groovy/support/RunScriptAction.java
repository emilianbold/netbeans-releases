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

package org.netbeans.modules.groovy.support;

import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;

/** 
 * Run the script action
 *
 * @author Petr Hamernik
 */
public class RunScriptAction extends NodeAction {

    public RunScriptAction() {
        putValue("noIconInMenu", Boolean.TRUE); //NOI18N
    }

    /** Invoke runScript method on the dataObject
     */
    protected void performAction(final Node[] activatedNodes) {
        for (Node node : activatedNodes) {
            DataObject dataObject = node.getLookup().lookup(DataObject.class);
            if (dataObject != null) {
                new ScriptExecSupport(dataObject.getPrimaryFile()).runScript();
            }
        }
    }
    
    boolean isCustomScript(DataObject dataObject) {
        
        /* we need to figure out whether this is a plain script or
           some special case, for example a script located in 
           $PROJECT_HOME/script should be run as a grails target
           rather than a standalone script. */

        FileObject nodeFile = dataObject.getPrimaryFile();
        Project project = FileOwnerQuery.getOwner(nodeFile);

        if (project != null) {
            FileObject parentDir = nodeFile.getParent();
            if (parentDir.isFolder() && parentDir.getName().equals("scripts")) {
                FileObject prjDir = project.getProjectDirectory();
                
                if (prjDir != null) {
                    FileObject scriptsDir = prjDir.getFileObject("scripts");
                    
                    if (scriptsDir != null) {
                        if (scriptsDir.getPath().equals(parentDir.getPath())) {
                            return true;
                        }
                    }
                }
            }
        }
        
        return false;
    }
    
    
    @Override
    protected boolean asynchronous() {
        return true;
    }

    public String getName() {
        return NbBundle.getMessage(RunScriptAction.class, "CTL_ExecScriptAction");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx (RunScriptAction.class);
    }

    /**
     * @return the resource name for the icon, e.g. <code>com/mycom/mymodule/myIcon.gif</code>; or <code>null</code> to have no icon (make a text label)
     */
    @Override
    protected String iconResource() {
        return "org/netbeans/modules/groovy/support/resources/runSingle.png"; // NOI18N
    }

    @Override
    protected boolean enable(Node[] activatedNodes) {
        if (activatedNodes.length == 0) {
            return false;
        }
        DataObject dataObject = activatedNodes[0].getLookup().lookup(DataObject.class);
        if (dataObject == null) {
            return false;
        }
        
        if(isCustomScript(dataObject))
            return false;
        
        return "text/x-groovy".equals(dataObject.getPrimaryFile().getMIMEType()); // NOI18N
    }

}
