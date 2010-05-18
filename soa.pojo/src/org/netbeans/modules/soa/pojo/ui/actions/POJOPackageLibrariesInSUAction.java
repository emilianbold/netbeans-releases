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
package org.netbeans.modules.soa.pojo.ui.actions;

import org.netbeans.api.project.Project;
import org.netbeans.modules.soa.pojo.ui.POJONode;
import org.netbeans.modules.soa.pojo.ui.POJOsRootNode;
import org.netbeans.modules.soa.pojo.util.GeneratorUtil;
import org.netbeans.modules.soa.pojo.wizards.POJOHelper;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.lookup.AbstractLookup;

/**
 *
 * @author Sreenivasan Genipudi
 */
public class POJOPackageLibrariesInSUAction extends NodeAction {

    public void performAction(Node[] nodes) {
        POJOsRootNode pojoNode =
                nodes[0].getLookup().lookup(POJOsRootNode.class);

        if (pojoNode != null) {
            Project project = pojoNode.getChildren().getNodes()[0].getLookup().lookup(AbstractLookup.class).lookup(POJONode.class).getProject();

            POJOHelper.saveProjectProperty(project, GeneratorUtil.PROJECT_PROPERTY_PACKAGE_ALL, GeneratorUtil.CONST_TRUE);
            if (POJOHelper.getProjectProperty(project, GeneratorUtil.PROJECT_PROPERTY_PACKAGE_ALL).equals(GeneratorUtil.CONST_TRUE)) {
                NotifyDescriptor d = new NotifyDescriptor.Message(NbBundle.getMessage(
                        this.getClass(), "MSG_AllPackaging_Enabled"), NotifyDescriptor.INFORMATION_MESSAGE); //NOi18N
                DialogDisplayer.getDefault().notify(d);
            } else {
                NotifyDescriptor d = new NotifyDescriptor.Message(
                        NbBundle.getMessage(this.getClass(), "MSG_Failed_AllPackaging_Enabled"), NotifyDescriptor.ERROR_MESSAGE); //NOI18N
                DialogDisplayer.getDefault().notify(d);

            }
        }
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public String getName() {
        return NbBundle.getMessage(this.getClass(), "LBL_EnableAllPackaging");//NOI18N
    }

    @Override
    protected boolean asynchronous() {
        return false;
    }

    @Override
    protected boolean enable(Node[] node) {
        boolean bResult = true;
        POJOsRootNode pojoNode = node[0].getLookup().lookup(POJOsRootNode.class);

        if (pojoNode != null) {
            Project project = pojoNode.getChildren().getNodes()[0].getLookup().lookup(AbstractLookup.class).lookup(POJONode.class).getProject();
            String propValue = POJOHelper.getProjectProperty(project, GeneratorUtil.PROJECT_PROPERTY_PACKAGE_ALL);
            if (propValue == null) {
                bResult = false;
            } else if (propValue.equals(GeneratorUtil.CONST_TRUE)) {
                bResult = false;
            }
        }

        return bResult;
    }
}
