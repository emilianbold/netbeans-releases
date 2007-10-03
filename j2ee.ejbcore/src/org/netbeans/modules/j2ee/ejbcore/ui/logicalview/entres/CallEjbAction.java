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

package org.netbeans.modules.j2ee.ejbcore.ui.logicalview.entres;

import java.io.IOException;
import javax.lang.model.element.TypeElement;
import javax.swing.Action;
import org.netbeans.api.java.source.ElementHandle;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.j2ee.deployment.devmodules.api.Deployment;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eeModule;
import org.netbeans.modules.j2ee.deployment.devmodules.api.J2eePlatform;
import org.netbeans.modules.j2ee.deployment.devmodules.spi.J2eeModuleProvider;
import org.netbeans.modules.j2ee.ejbcore._RetoucheUtil;
import org.openide.filesystems.FileObject;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.Lookup;
import org.openide.util.actions.NodeAction;

/**
 * Provide action for calling another EJB
 * @author Chris Webster
 * @author Martin Adamek
 */
public class CallEjbAction extends NodeAction {
    
    protected void performAction(Node[] nodes) {
        try {
            ElementHandle<TypeElement> elementHandle = _RetoucheUtil.getJavaClassFromNode(nodes[0]);
            FileObject fileObject = nodes[0].getLookup().lookup(FileObject.class);
            CallEjbDialog callEjbDialog = new CallEjbDialog();
            callEjbDialog.open(fileObject, elementHandle.getQualifiedName(), NbBundle.getMessage(CallEjbAction.class, "LBL_CallEjbAction")); //NOI18N
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
    }

    public String getName() {
        return NbBundle.getMessage(CallEjbAction.class, "LBL_CallEjbAction");
    }
    
    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
        // If you will provide context help then use:
        // return new HelpCtx(CallEjbAction.class);
    }
    
    protected boolean asynchronous() {
        return false;
    }
    
    protected boolean enable(Node[] nodes) {
        if (nodes == null || nodes.length != 1) {
            return false;
        }
        ElementHandle<TypeElement> elementHandle = null;
        try {
            elementHandle = _RetoucheUtil.getJavaClassFromNode(nodes[0]);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
            return false;
        }
        if (elementHandle == null) {
            return false;
        }
        FileObject srcFile = nodes[0].getLookup().lookup(FileObject.class);
        Project project = FileOwnerQuery.getOwner(srcFile);
        J2eeModuleProvider j2eeModuleProvider = project.getLookup ().lookup (J2eeModuleProvider.class);
        if (j2eeModuleProvider != null) {
            String serverInstanceId = j2eeModuleProvider.getServerInstanceID();
            if (serverInstanceId == null) {
                return true;
            }
            J2eePlatform platform = Deployment.getDefault().getJ2eePlatform(serverInstanceId);
            if (platform == null) {
                return true;
            }
            if (!platform.getSupportedModuleTypes().contains(J2eeModule.EJB)) {
                return false;
            }
        }
        try {
            return !_RetoucheUtil.isInterface(srcFile, elementHandle);
        } catch (IOException ioe) {
            Exceptions.printStackTrace(ioe);
        }
        return false;
    }
    
    /** Perform extra initialization of this action's singleton.
     * PLEASE do not use constructors for this purpose!
     * protected void initialize() {
     * super.initialize();
     * putProperty(Action.SHORT_DESCRIPTION, NbBundle.getMessage(CallEjbAction.class, "HINT_Action"));
     * }
     */

    public Action createContextAwareInstance(Lookup actionContext) {
        boolean enable = enable(actionContext.lookup(new Lookup.Template<Node>(Node.class)).allInstances().toArray(new Node[0]));
        return enable ? this : null;
    }
    
}
