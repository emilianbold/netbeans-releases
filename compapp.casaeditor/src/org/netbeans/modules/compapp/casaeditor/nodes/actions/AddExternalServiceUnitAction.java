/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.compapp.casaeditor.nodes.actions;

import org.openide.util.actions.NodeAction;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.nodes.Node;
import org.netbeans.modules.compapp.casaeditor.model.casa.CasaWrapperModel;
import org.netbeans.modules.compapp.casaeditor.CasaDataNode;
import org.netbeans.modules.compapp.casaeditor.CasaDataObject;
import org.netbeans.modules.compapp.casaeditor.CasaDataEditorSupport;
import org.netbeans.modules.compapp.casaeditor.nodes.CasaNode;

import javax.swing.SwingUtilities;
import javax.swing.Action;

/**
 * Add a reference to external Service Unit to the CompApp project.
 *
 * User: tli
 * Date: Aug 1, 2007
 * To change this template use File | Settings | File Templates.
 */
public class AddExternalServiceUnitAction extends NodeAction {

    protected boolean enable(Node[] activatedNodes) {
        return true;
    }

    protected boolean asynchronous() {
        return false;
    }

    public String getName() {
        return NbBundle.getMessage(LoadWSDLPortsAction.class, "LBL_AddExtSUAction_Name"); // NOI18N
    }

    public HelpCtx getHelpCtx() {
        return HelpCtx.DEFAULT_HELP;
    }

    public Action getAction() {
        return this;
    }

    public void performAction(Node[] activatedNodes) {
        if (activatedNodes.length < 1) {
            return;
        }
        CasaWrapperModel cmodel = null;
        if (activatedNodes[0] instanceof CasaDataNode) {
            final CasaDataNode node = ((CasaDataNode) activatedNodes[0]);
            CasaDataObject obj = (CasaDataObject) node.getDataObject();
            CasaDataEditorSupport es = obj.getLookup().lookup(CasaDataEditorSupport.class);
            if (es != null) {
                cmodel = es.getModel();
            }
        } else if (activatedNodes[0] instanceof CasaNode) {
            final CasaNode node = ((CasaNode) activatedNodes[0]);
            cmodel = node.getModel();
        }

        if (cmodel == null) {
            return;
        }

        final CasaWrapperModel model = cmodel;
        SwingUtilities.invokeLater(new Runnable(){
            public void run(){
                model.addServiceEngineServiceUnitFromPalette(false, 40, 40);
            }
        });
    }

}
